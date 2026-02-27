# MyHub 工程产品级优化评审（性能重点）

> 评审日期：2026-02-26  
> 范围：`composeApp` / `feature` / `datastore` / `core` / 构建链路（KMP）

## 1) 评审方法

- 代码走查：启动链路、列表渲染、数据层流、同步与网络、构建配置。
- 证据来源：仓库现状代码 + Kotlin Multiplatform 官方文档（kotlinlang.org / JetBrains 官方文档）。
- 输出方式：按产品影响优先级（P0/P1/P2）给出可执行优化项。

## 2) KMP 官方最佳实践基线（对照依据）

1. 分层 Source Set：推荐使用默认层级模板，避免手工关系导致复杂度和构建成本升高。  
   https://kotlinlang.org/docs/multiplatform/multiplatform-hierarchy.html
2. iOS 集成方式需要统一（Direct/CocoaPods/XCFramework），避免混合策略带来构建和链接不确定性。  
   https://kotlinlang.org/docs/multiplatform/multiplatform-ios-integration-overview.html
3. Kotlin Gradle 构建性能：启用并稳定使用 configuration cache / build cache / 增量编译。  
   https://kotlinlang.org/docs/gradle-compilation-and-caches.html  
   https://kotlinlang.org/docs/gradle-best-practices.html
4. Kotlin/Native 编译速度建议：避免继续使用会关闭缓存的 `kotlin.native.cacheKind=none`。  
   https://kotlinlang.org/docs/native-improving-compilation-time.html
5. Apple Framework 构建建议：framework 输出策略（dynamic/static）应清晰一致。  
   https://kotlinlang.org/docs/apple-framework.html

## 3) 产品级优化清单

## P0（立即处理，直接影响体验/稳定性/成本）

### P0-1 启动与全局层存在协程作用域生命周期泄漏风险

- 代码证据
  - `rememberAppState` 在 Composable 内 `remember { CoroutineScope(...) }`，未见显式取消：
    - `composeApp/src/commonMain/kotlin/tech/zhifu/app/myhub/AppState.kt:32-49`
  - Koin 为 ViewModel 依赖提供 `factory<CoroutineScope> { CoroutineScope(Dispatchers.Default) }`，无 Job 管理：
    - `composeApp/src/commonMain/kotlin/tech/zhifu/app/myhub/di/Koin.kt:73-75`
- 问题
  - 会产生不可控后台协程；页面/对象销毁后仍可能工作，导致 CPU、内存和 I/O 持续消耗。
- 优化建议
  - 统一注入 `AppScope(SupervisorJob + Dispatchers.Default)` 与“页面级 scope”（可 cancel）。
  - `rememberAppState` 改为 `rememberCoroutineScope()` 或 `DisposableEffect` 中显式 `cancel()`。
- 依据
  - KMP/Gradle 官方强调构建与运行时可控性；对长期运行项目，结构化并发是基础稳定性要求。
- 优先级
  - **P0**（先修）

### P0-2 首页数据流是“全量拉取 + 内存分页”，列表增大后会明显退化

- 代码证据
  - `CardStoreSourceOfTruth` 对 `ByUser` 先 `observeCards(userId)` 全量，再 `drop/take`：
    - `datastore/repository-client/.../CardStoreSourceOfTruth.kt:41-46`
  - 本地数据源 `observeCards` 查询用户所有 card + tags，再组合映射：
    - `datastore/datasource-local/.../LocalCardDataSourceImpl.kt:227-258`
  - Dashboard 又二次 `sortedByDescending + take(20)`：
    - `feature/dashboard/.../DashboardViewModel.kt:135-140`
- 问题
  - 数据规模增长后，首页渲染和刷新会随总卡片量线性上涨，影响首屏与滚动。
- 优化建议
  - 下推分页到 SQL（`LIMIT/OFFSET` + 必要索引命中），流中只传“当前页”。
  - Dashboard 直接消费分页响应，不做二次全量排序。
- 依据
  - KMP 分层结构建议强调在正确 source set/层做职责分离；数据分页应在数据层完成，避免 UI 层承担全量计算。
- 优先级
  - **P0**

### P0-3 Dashboard 加载更多触发方式可能重复触发，造成冗余请求

- 代码证据
  - 列表尾部 `LaunchedEffect(Unit) { viewModel.loadMoreCards() }` 位于可反复进入组合的 item 中：
    - `feature/dashboard/.../DashboardScreen.kt:328-343`
- 问题
  - 在状态抖动或列表结构变化下，可能重复触发 loadMore，造成请求风暴或无效状态切换。
- 优化建议
  - 改为基于 `LazyGridState` 滚动阈值触发，并加请求去重（原子 in-flight 标记）。
  - 统一分页状态机：`Idle/Loading/End/Error`。
- 优先级
  - **P0**

### P0-4 文本编辑链路每次输入都做 HTML 转换并高频日志，CPU 消耗偏高

- 代码证据
  - `snapshotFlow { textState.annotatedString } -> map { textState.toHtml() }`，每次输入触发；并记录 debug 日志：
    - `feature/capture/.../CaptureScreen.kt:1276-1290`
- 问题
  - 编辑器输入路径是高频热点，复杂转换 + 日志会直接影响打字流畅度。
- 优化建议
  - 增加节流/去抖（如 150~300ms），并在 release 关闭这类内容日志。
  - 仅在提交/失焦时执行完整 HTML 序列化。
- 优先级
  - **P0**

## P1（高价值优化，提升体验与可维护性）

### P1-1 图片组件在列表中大量使用 `SubcomposeAsyncImage`，有额外子组合成本

- 代码证据
  - `component/card/.../CardPreview.kt:297`
  - `component/media/.../MediaThumbnail.kt:66`
  - `component/media/.../MediaPreviewDialog.kt:153`
- 问题
  - 对简单占位场景，`SubcomposeAsyncImage` 相比 `AsyncImage` 有更多组合开销。
- 优化建议
  - 列表与网格默认改 `AsyncImage`；仅保留少数复杂 loading/error slot 场景使用 `SubcomposeAsyncImage`。
- 优先级
  - **P1**

### P1-2 App 根节点存在重组高频日志与全局 ImageLoader 工厂重复设置

- 代码证据
  - 根 composable 中 debug 日志：`composeApp/.../App.kt:57,72,96`
  - `setSingletonImageLoaderFactory` 在 `AppContent` 内调用：`composeApp/.../App.kt:99-105`
- 问题
  - 根节点重组时重复执行全局设置与日志会造成不必要开销。
- 优化建议
  - 将 ImageLoader 工厂初始化移至应用启动单次执行（如平台入口/Koin init）。
  - 根 composable 日志降级或仅 debug 构建启用。
- 优先级
  - **P1**

### P1-3 Ktor 客户端缺少 timeout/retry/compression 基础策略且默认 INFO 日志

- 代码证据
  - 仅配置 `ContentNegotiation`、`DefaultRequest`、`Logging(INFO)`：
    - `core/network/.../KtorClientFactory.kt:29-49`
- 问题
  - 移动网络波动下，缺少超时和重试策略会拉长交互尾延迟；INFO 日志在生产会增 I/O。
- 优化建议
  - 增加 `HttpTimeout`、有界重试（幂等请求）、`ContentEncoding`。
  - 按 build variant 控制日志等级（prod 降到 `NONE`/`ERROR`）。
- 优先级
  - **P1**

### P1-4 iOS framework 输出策略不一致（同模块出现 static/dynamic 混用）

- 代码证据
  - `iosTarget.binaries.framework { isStatic = false }`：`composeApp/build.gradle.kts:35-38`
  - `cocoapods.framework { isStatic = true }`：`composeApp/build.gradle.kts:63-66`
- 问题
  - 易造成构建行为不一致、排查成本提升，影响 iOS CI 稳定与编译时间可预测性。
- 优化建议
  - 按 iOS 集成方式统一策略（CocoaPods 本地集成时建议保持单一 framework 策略）。
- 依据
  - KMP iOS 集成官方建议先明确单一路径再落地。
- 优先级
  - **P1**

## P2（治理与长期收益）

### P2-1 Kotlin/Native 编译缓存被关闭，影响本地 iOS 构建速度

- 代码证据
  - `kotlin.native.cacheKind.iosSimulatorArm64=none`：`gradle.properties:13`
- 问题
  - 关闭 target cache 会增加重复编译开销。
- 优化建议
  - 若不是临时 workaround，建议移除并重新验证构建稳定性。
- 依据
  - Kotlin 官方 2026-01 文档明确建议清理这类历史禁用项。  
    https://kotlinlang.org/docs/native-improving-compilation-time.html
- 优先级
  - **P2**

### P2-2 版本矩阵大量 alpha/rc，发布稳定性与性能波动风险较高

- 代码证据
  - `agp=9.1.0-alpha06`、`jb-compose=1.11.0-alpha01`、`mnf-store=5.1.0-alpha08` 等：
    - `gradle/libs.versions.toml:2,15,27`
- 问题
  - alpha 版本在构建速度/二进制大小/兼容性上波动更大，线上可预测性下降。
- 优化建议
  - 建立“生产稳定通道”版本锁定（stable/beta），alpha 用于独立实验分支。
- 优先级
  - **P2**

### P2-3 缺少 Compose 编译器指标闭环，优化决策难量化

- 现状
  - 未见 Compose compiler metrics/reports 配置。
- 问题
  - 难以识别不稳定参数、跳过率、重组热点。
- 优化建议
  - 在 CI 增加编译器报告产物，建立“重组热图 + 回归阈值”。
- 优先级
  - **P2**

## 4) 推荐执行顺序（两周落地版）

1. **第 1 周（P0）**：作用域治理、Dashboard 真分页、loadMore 状态机、Capture 输入链路节流。  
2. **第 2 周（P1）**：图片组件替换、根节点初始化下沉、网络客户端策略完善、iOS framework 策略统一。  
3. **并行治理（P2）**：恢复 Native 缓存、依赖稳定通道、Compose 指标上报。

## 5) 验收指标（建议）

- 首页首屏可交互时间（TTI）下降 >= 20%。
- Dashboard 下拉刷新 CPU 峰值下降 >= 25%。
- Capture 连续输入帧丢失率下降 >= 30%。
- iOS 本地增量构建时间下降 >= 15%。
- CI 构建波动（P95/P50）下降 >= 20%。

---

如果需要，我可以在下一步直接提交一版“P0 实施 patch”（先改协程作用域 + Dashboard 分页触发 + Capture 节流），并附上可跑的基准脚本。
