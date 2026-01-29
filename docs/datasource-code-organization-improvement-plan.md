# Datasource 代码组织改进计划

> 从资深工程师视角，对当前 datasource-local / datasource-remote 的代码组织做一次梳理，给出**合理性评估**与**易于维护**的改进计划。不改变「DataSource = 本地 DB 入口 + 远程网络入口」的职责，仅从包结构、命名、依赖边界、可测试性等方面提出可落地建议。
>
> 相关：[Datasource 使用梳理及与 Store 的关系](./datasource-usage-and-store.md)

---

## 1. 当前结构概览

### 1.1 模块与包

```
datastore/
├── datasource-local/
│   └── src/commonMain/kotlin/.../datasource/
│       ├── LocalCardDataSource.kt          # 接口
│       ├── LocalUserDataSource.kt         # 接口（已统一命名）
│       ├── LocalSyncDataSource.kt
│       ├── ...
│       ├── di/LocalDataSourceModule.kt
│       └── impl/
│           ├── LocalCardDataSourceImpl.kt
│           ├── LocalUserDataSourceImpl.kt
│           └── ...
│
├── datasource-remote/
│   └── src/commonMain/kotlin/.../datasource/
│       ├── RemoteCardDataSource.kt
│       ├── ...
│       ├── di/RemoteDataSourceModule.kt
│       └── impl/
│           ├── RemoteCardDataSourceImpl.kt
│           ├── TokenRefreshProviderAdapter.kt
│           └── ...
```

### 1.2 依赖关系

- **datasource-local**：依赖 database-client、model（domain）等；不依赖 remote 或 repository。
- **datasource-remote**：依赖 model（domain、dto）、network、exception 等；不依赖 local 或 repository。
- **repository-client**：依赖 datasource-local、datasource-remote，以及 store5/cache5/core5。

当前分层清晰：DataSource 处于「技术实现层」，Repository + Store 在上层，符合预期。

---

## 2. 合理性评估

| 维度          | 现状                                                           | 评价                     |
|-------------|--------------------------------------------------------------|------------------------|
| **职责单一**    | Local = DB 入口，Remote = 网络入口                                  | ✅ 合理                   |
| **接口与实现分离** | 接口在 `datasource/`，实现在 `impl/`                                | ✅ 利于替换与测试              |
| **按领域拆分**   | Card / User / Tag / Collection / Template / Sync / Auth 等分文件 | ✅ 易定位、易扩展              |
| **DI 集中**   | 各模块有 `di/XXXDataSourceModule`                                | ✅ 绑定清晰                 |
| **命名一致**    | 已统一为 `LocalUserDataSource`（文件与接口）                          | ✅ 已修正                   |
| **错误与模型**   | Remote 使用 ApiException、NetworkException；DTO 在 model-dto      | ✅ 边界清晰，可考虑在文档中明确「谁抛什么」 |

整体结构**合理**，改进以「一致性、可维护性、可测试性」为主，不做大拆大合。

---

## 3. 改进计划

### 3.1 命名与拼写统一（优先级：高）

**问题**：`LocalUserDatasource` 与其它 `Local*DataSource` 不一致，易在搜索、重构时漏改。

**已执行**：

- 已将文件 `LocalUserDatasource.kt` 重命名为 `LocalUserDataSource.kt`；接口名已是 `LocalUserDataSource`，无需改代码引用。
- 建议在 README 或贡献规范中约定：所有 DataSource 接口/文件名统一为 `*DataSource`（驼峰 DataSource）。

---

### 3.2 接口与实现的包归属（优先级：低）

**现状**：接口与实现都在同一「逻辑包」下（`datasource` 与 `datasource.impl`），仅用目录区分。

**建议**：

- **保持现状即可**。Kotlin 多平台下同模块内 `impl` 子包足够表达「实现细节」，无需拆成独立 api/impl 模块（否则模块数会翻倍，收益有限）。
- 若未来某类 DataSource 需要「多实现、可插拔」（如 LocalCardDataSource 有 SQLDelight 版与 Memory 版），可考虑为该领域单独抽出 `datasource-xxx-api` 与 `datasource-xxx-impl`，当前规模下**不推荐**提前拆。

---

### 3.3 Remote 层错误类型与文档（优先级：中）

**现状**：Remote 实现中统一使用 `ApiException`、`NetworkException` 等，调用方（Fetcher/Updater）需 catch 并处理或上抛。

**建议**：

- **代码**：保持现有异常体系，不在 DataSource 层引入 Result 包装（避免与 Store 的 FetcherResult 重复语义）。
- **文档**：在「远程数据源模块方案设计」或本改进计划中增加一小节：**「Remote DataSource 异常约定」**，写明：
    - 哪些方法在什么情况下抛 `ApiException`（如 4xx/5xx 响应）。
    - 哪些情况抛 `NetworkException`（如超时、连接失败）。
    - **null 约定（必写项）**：哪些方法在何种情况下返回 `null`（如 `getCardById` 的 404 → `null`），需在文档中明确约定并举例，便于 Fetcher 层统一选用 `FetcherResult` 或异常处理。

**收益**：调用方（Store Fetcher/Updater）和测试用例有据可依，减少误用。

---

### 3.4 Local 层「可观察」能力与 Store 的配合（优先级：高）

与 [Store 使用问题与纠正说明](./store-usage-issues.md) 第 2 节（SourceOfTruth reader 语义）对应，需在 Local 层补齐可观察 API。

**问题**：Store 的 SourceOfTruth 需要 `reader(key): Flow<Output?>` 具备「可持续观察」语义；当前部分 Key（如列表 ByUser）使用的是单次 `getCards` 再 emit，无法随 DB 变更持续推送。

**建议**：

- **在 Local*DataSource 接口上**：对「需要被 Store 观察」的实体，提供 `observe*` 风格 API（如已有 `observeCard`，补充 `observeCards(userId)` 或等价按列表维度的 Flow）。
- **实现**：基于 SQLDelight 的 `asFlow()` / 查询观察能力实现，避免轮询。
- **SourceOfTruth**：将此类 reader 改为使用 `observe*`，不再用单次 get + 单次 emit。

**完成标准**：所有被 Store SOT 使用的列表类 Key 的 reader 均改为 observe*，且无单次 get + 单次 emit 的写法；本地写入后 stream 能收到新数据。

**收益**：本地写入/更新能通过 Store 的 stream 自动反映到 UI，符合 Store5 设计，也减少「刷新按钮」依赖。

---

### 3.5 测试策略与可测试性（优先级：中）

**现状**：DataSource 实现依赖真实 DB 与 HttpClient，单元测试需要 mock 或使用 in-memory 实现。

**建议**：

- **接口层**：保持接口精简、入参出参明确，便于编写 Fake 实现（如 `FakeLocalCardDataSource`、`FakeRemoteCardDataSource`）供 Repository/Store 测试使用。
- **实现层**：
    - Local：可考虑在 test 源集使用 SQLDelight 的 in-memory driver，对重点 CRUD 做集成测试。
    - Remote：使用 Ktor 的 MockEngine 或契约测试，避免直连真实后端。
- **文档**：在 datasource 各模块 README 或本改进计划中，简短说明「推荐用 Fake 做上层测试，用 MockEngine/in-memory DB 做 DataSource 层测试」，避免后续加测试时重复造轮子。

---

### 3.6 依赖与可见性（优先级：低）

**现状**：datasource-local 仅被 repository-client 等少数模块依赖；datasource-remote 同理。接口为 public，实现多为 internal 或同模块可见。

**建议**：

- **接口**：继续对外暴露；实现类若不需要被其他模块引用，可用 `internal` 修饰，减少误用。当前若已是「仅 DI 引用实现类」，可维持现状。
- **避免反向依赖**：确保 datasource-* 不依赖 repository、feature 等上层模块；若有新需求（如新 API），应通过扩展接口或新方法在 datasource 内完成，不在 DataSource 层引用 Store/Repository 类型。

---

### 3.3.1 Remote DataSource 异常约定（已落档）

- **ApiException**：HTTP 4xx/5xx 或服务端返回错误时，由 Remote 实现抛出（如 `RemoteCardDataSourceImpl` 中 `when (response.status) { ... else -> throw ApiException(...) }`）。
- **NetworkException**：网络层异常（超时、连接失败、解析失败等）在 catch 中包装为 `NetworkException` 后抛出。
- **null 约定**：读单条接口在「资源不存在」时返回 `null`，不抛异常。例如：
  - `RemoteCardDataSource.getCardById(id)`：HTTP 404 或空体时返回 `null`。
  - `RemoteUserDataSource.getUser(id)`：无用户时由实现决定返回 `null` 或抛异常，需在具体接口注释中写明。
- Fetcher 层应据此选择：对返回 `null` 的接口用 `Fetcher.ofResult` 转为 `FetcherResult.Error.Message` 或保持 `FetcherResult.Data(null)` 的语义；对抛异常的接口在 Fetcher 内 try/catch 转为 `FetcherResult.Error.Exception(e)` 或上抛。

### 3.5.1 测试策略说明（已落档）

- **上层（Repository / Store）**：推荐使用 **Fake** 实现（如 `FakeLocalCardDataSource`、`FakeRemoteCardDataSource`）注入，避免依赖真实 DB/网络，测试业务与缓存逻辑。
- **DataSource 实现层**：
  - **Local**：在 test 源集使用 SQLDelight 的 **in-memory driver**，对重点 CRUD 与 observe* 做集成测试。
  - **Remote**：使用 Ktor 的 **MockEngine** 或契约测试，固定请求/响应，避免直连真实后端。
- 新增 DataSource 时，优先在对应模块 README 或本改进计划中注明「是否提供 Fake」「推荐测试方式」，便于后续维护。

---

## 4. 实施顺序建议

| 阶段 | 内容                                                      | 预估                    | 完成标准 | 状态 |
|----|---------------------------------------------------------|-----------------------|---------|------|
| 1  | 命名统一：LocalUserDatasource → LocalUserDataSource          | 0.5d                  | 接口与引用处已统一为 LocalUserDataSource。 | ✅ 已完成：文件已重命名为 LocalUserDataSource.kt，接口名已是 LocalUserDataSource。 |
| 2  | Local 层补充 observe* API 并用于 SourceOfTruth               | 1–2d                  | 列表类 Key 的 reader 均用 observe*，本地写入后 stream 能收到新数据。 | ✅ 已完成：Card/Collection 使用 observeCards/observeCollections+分页；Template 新增 observeTemplates 并用于 SOT。 |
| 3  | 文档：Remote 异常约定 + 测试策略说明                                 | 0.5d                  | 异常约定含 ApiException/NetworkException/null 约定；测试策略说明已写入文档。 | ✅ 已完成：见 3.3.1、3.5.1。 |
| 4  | 可选：为常用 DataSource 提供 Fake 实现并用于 Repository/Store 测试     | 持续、按需                 | — | 未做 |

---

## 5. 小结

- **当前 DataSource 代码组织总体合理**：职责清晰、按领域拆分、接口与实现分离、依赖方向正确。
- **改进重点**：命名一致、Local 可观察能力与 Store 对齐、Remote 异常与测试策略文档化；其余为低优先级或可选。
- 实施时以「命名 + 可观察 API + 文档」为主，可在不破坏现有使用方式的前提下提升可维护性与可测试性。
