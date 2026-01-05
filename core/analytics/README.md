# core:analytics

统一统计框架模块，提供跨平台的统计服务抽象和实现。

## 📋 功能特性

- ✅ **统一接口**：提供 `AnalyticsService` 统一接口，业务代码无需关心底层 SDK
- ✅ **多 Provider 支持**：支持同时使用多个统计服务（友盟、Firebase 等）
- ✅ **类型安全**：使用 `AnalyticsValue` 替代 `Any`，避免跨平台兼容性问题
- ✅ **事件缓冲**：Provider 初始化期间的事件自动缓冲，避免丢失
- ✅ **隐私合规**：内置 `AnalyticsConsent` 支持 GDPR、CCPA 等隐私法规
- ✅ **跨平台支持**：支持 Android、iOS、Desktop、Web 等多个平台
- ✅ **易于扩展**：采用 Registry 模式，便于添加新的统计服务

## 🎯 快速开始

### 1. 在 Koin 中配置

```kotlin
import tech.zhifu.app.myhub.analytics.di.analyticsModule
import tech.zhifu.app.myhub.analytics.di.AppCoroutineScope

fun initKoin() {
    val appScope = AppCoroutineScope()

    val koinApplication = startKoin {
        modules(
            single<AppCoroutineScope> { appScope },
            analyticsModule(
                config = {
                    AnalyticsConfig(
                        region = Region.DOMESTIC,
                        enabled = true,
                        debugMode = BuildConfig.DEBUG,
                        providers = listOf(
                            ProviderConfig(
                                type = ProviderType.CONSOLE,
                                enabled = true
                            )
                        )
                    )
                }
                // 注意：平台特定的 Provider 注册器已内置在 analyticsModule 中
                // 无需手动传入 registrar 参数
            )
        )
    }

    // 初始化统计服务
    appScope.launch {
        delay(100)
        koinApplication.koin.get<AnalyticsManager>().initialize()
    }
}
```

**注意**：平台特定的 Provider 注册器（如 `JvmAnalyticsRegistrar`）已通过 `expect/actual` 机制内置在 `core:analytics` 模块中，应用层无需关心平台特定的实现细节。

### 2. 在业务代码中使用

```kotlin
class DashboardViewModel(
    private val analyticsService: AnalyticsService
) : ViewModel() {

    fun onScreenShown() {
        analyticsService.setScreen("Dashboard")
        analyticsService.logEvent(
            AnalyticsEvent.screenView("Dashboard", "DashboardScreen")
        )
    }

    fun onCardCreated(card: Card) {
        analyticsService.logEvent(
            AnalyticsEvent(
                name = AnalyticsEvents.CARD_CREATED,
                parameters = mapOf(
                    "card_id" to AnalyticsValue.Str(card.id),
                    "card_type" to AnalyticsValue.Str(card.type.name)
                )
            )
        )
    }
}
```

## 📦 已实现的 Provider

### ConsoleProvider

- **平台**：JVM、JS、WASM
- **用途**：测试和 Desktop 平台调试
- **输出**：控制台输出

### FileProvider

- **平台**：JVM（Desktop）
- **用途**：QA 测试、自动化测试、内部分析
- **输出**：JSON 或 CSV 文件
- **配置**：

```kotlin
  ProviderConfig(
      type = ProviderType.FILE,
      enabled = true,
      customParams = mapOf(
          "outputDir" to "./analytics",
          "format" to "JSON" // 或 "CSV"
      )
  )
```

## 🔗 相关模块

- `core:platform` - 平台抽象模块
- `core:logger` - 日志模块

---

## 📋 项目进度

### ✅ 已完成事项

#### 阶段一：核心框架搭建 ✅

- ✅ 创建 `core:analytics` 模块
- ✅ 定义核心接口（AnalyticsService、AnalyticsProvider）
- ✅ 实现 AnalyticsManager
- ✅ 实现 AnalyticsConfig 和配置管理
- ✅ 创建 Koin DI 模块
- ✅ 实现 ConsoleProvider（用于测试和 Desktop）
- ✅ 实现 FileProvider（用于 Desktop QA）
- ✅ 编写单元测试（30+ 测试用例，100% 通过率）
- ✅ 平台特定的 Provider 注册器内置（expect/actual 机制）

#### 核心功能 ✅

- ✅ AnalyticsValue 类型安全机制
- ✅ AnalyticsEvents 事件命名规范
- ✅ BaseAnalyticsProvider 事件缓冲机制
- ✅ AnalyticsConsent 隐私合规支持
- ✅ AnalyticsProviderFactory Registry 模式
- ✅ AppCoroutineScope 应用级协程作用域
- ✅ Debug 模式自动注入 ConsoleProvider
- ✅ 批量事件接口（logEvents）
- ✅ 平台特定注册器自动发现机制（expect/actual）

### 🚧 待办事项

#### 阶段二：国内统计服务集成（2-3 周）

- ⏳ 友盟适配器实现

  - ⏳ Android 平台实现
  - ⏳ iOS 平台实现
  - ⏳ 集成测试

- ⏳ 神策数据适配器实现（可选）

  - ⏳ 根据实际需求决定是否实现

#### 阶段三：海外统计服务集成（2-3 周）

- ⏳ Firebase Analytics 适配器实现

  - ⏳ Android 平台实现
  - ⏳ iOS 平台实现
  - ⏳ Web 平台实现（JS/WASM）

- ⏳ Google Analytics 适配器实现（可选）

  - ⏳ Web 平台实现

#### 阶段四：集成和优化（1-2 周）

- ✅ 集成到应用

  - ✅ 在 composeApp 中集成 ConsoleProvider
  - ✅ 平台特定注册器内置到 core:analytics 模块
  - ⏳ 在各个 Feature 模块中添加统计埋点

- ⏳ 性能优化

  - ⏳ 异步上报优化
  - ⏳ 批量上报支持
  - ⏳ 网络异常处理

- ⏳ 文档完善

  - ⏳ 使用文档
  - ⏳ API 文档
  - ⏳ 最佳实践指南

### 📝 下一步计划

1. **优先实现 FirebaseProvider(Android)** - 作为生产级样板
2. ✅ **在 composeApp 中集成 ConsoleProvider** - 已完成，已验证埋点体验
3. **实现友盟适配器** - 支持国内用户
4. **添加集成测试** - 验证端到端流程
5. **实现 Android/iOS 平台特定的 Provider 注册器** - 为 Firebase/Umeng 等 SDK 做准备

---

## 📚 详细文档

- [统计框架设计方案](../../docs/ANALYTICS_FRAMEWORK_DESIGN.md) - 完整的设计方案文档（v1.0，已锁定）
- [测试总结](docs/TEST_SUMMARY.md) - 单元测试总结和最佳实践
