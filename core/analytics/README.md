# Core Analytics Module

本模块用于**规范**和**实现** MyHub 应用的统计基础设施（Analytics Infra），为各功能模块**提供统一、跨平台的统计服务能力**。它基于 **Kotlin Multiplatform expect/actual 机制和渠道框架**，实现了**统一统计接口**、**多 Provider 支持**、**类型安全事件值**、**事件缓冲**、**隐私合规**等特性，并提供了面向 KMP 场景的**统一统计抽象接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目中集成和使用。

**重要说明**：`core/analytics` 模块是一个**混合（Mixed）模块**，它包含多个统计相关的功能：统一统计接口、多 Provider 管理、渠道框架、类型安全、隐私合规、事件缓冲等。

## 📋 功能特性

- ✅ **统一接口**：提供 `AnalyticsService` 统一接口，业务代码无需关心底层 SDK
- ✅ **多 Provider 支持**：支持同时使用多个统计服务（Firebase、Umeng、Console、File）
- ✅ **渠道框架**：支持通过构建变体（Build Variant）选择不同的统计服务
- ✅ **类型安全**：使用 `AnalyticsValue` 替代 `Any`，避免跨平台兼容性问题
- ✅ **事件缓冲**：Provider 初始化期间的事件自动缓冲，避免丢失
- ✅ **隐私合规**：内置 `AnalyticsConsent` 支持 GDPR、CCPA 等隐私法规
- ✅ **跨平台支持**：支持 Android、iOS、Desktop、Web 等多个平台

## 核心组件

### 1. AnalyticsService 接口

统一的统计服务接口，业务代码通过此接口进行统计上报：

- **`logEvent(event: AnalyticsEvent)`**：记录单个事件
- **`logEvents(events: List<AnalyticsEvent>)`**：批量记录事件（用于高频事件场景）
- **`setUserProperty(key: String, value: AnalyticsValue?)`**：设置用户属性
- **`setUserId(userId: String?)`**：设置用户 ID
- **`setScreen(screenName: String, screenClass: String?)`**：设置当前屏幕
- **`reset()`**：重置用户数据（登出时调用）

### 2. AnalyticsManager 类

统计管理器，管理多个统计服务提供商，提供统一的统计接口：

- **`initialize()`**：初始化所有启用的 Provider
- **多 Provider 管理**：支持同时使用多个统计服务（Firebase、Umeng 等）
- **隐私合规检查**：自动检查用户隐私同意状态
- **Debug 模式支持**：Debug 模式下自动注入 ConsoleProvider
- **错误处理**：Provider 初始化失败不影响其他 Provider

### 3. AnalyticsValue 类型

类型安全的统计值类型，避免使用 `Any` 导致的跨平台兼容性问题：

- **`AnalyticsValue.Str(value: String)`**：字符串类型
- **`AnalyticsValue.Num(value: Double)`**：数字类型（浮点数）
- **`AnalyticsValue.Int(value: Long)`**：整数类型
- **`AnalyticsValue.Bool(value: Boolean)`**：布尔类型
- **`AnalyticsValue.from(value: Any?)`**：从任意值转换为类型安全的 AnalyticsValue

### 4. AnalyticsEvent 数据类

统计事件模型，包含事件名称、参数等信息：

- **`name: String`**：事件名称（建议使用 `AnalyticsEvents` 常量）
- **`parameters: Map<String, AnalyticsValue>`**：事件参数（使用类型安全的 AnalyticsValue）
- **`value: Double?`**：事件值（可选）
- **`currency: String?`**：货币单位（可选，用于电商场景）
- **`screenView()`**：预定义的屏幕浏览事件
- **`userLogin()`**：预定义的用户登录事件
- **`purchase()`**：预定义的购买事件

### 5. AnalyticsProviderFactory 类

统计服务提供商工厂，使用 Registry 模式管理 Provider：

- **`register(type: ProviderType, creator: ProviderCreator)`**：注册 Provider 创建器
- **`create(config: ProviderConfig)`**：创建 Provider 实例
- **自动注册**：平台特定的 Provider 通过 `AnalyticsProviderRegistrar` 自动注册
- **默认 Provider**：自动注册 ConsoleProvider（所有平台支持）

## 使用示例

```kotlin
// 1. 配置 Koin，包含统计模块
import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.analytics.di.analyticsModule

startKoin {
    modules(
        analyticsModule()  // 平台特定的配置已自动处理
    )
}

// 2. 初始化统计服务（在应用启动时）
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.analytics.AnalyticsManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyApplication : KoinComponent {
    private val analyticsManager: AnalyticsManager by inject()
    
    fun onCreate() {
        // 异步初始化统计服务
        lifecycleScope.launch {
            analyticsManager.initialize()
        }
    }
}

// 3. 在业务代码中使用统计服务
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.analytics.AnalyticsEvents

class DashboardViewModel(
    private val analyticsService: AnalyticsService
) : ViewModel() {

    fun onScreenShown() {
        // 设置当前屏幕
        analyticsService.setScreen("Dashboard", "DashboardScreen")
        
        // 记录屏幕浏览事件
        analyticsService.logEvent(
            AnalyticsEvent.screenView("Dashboard", "DashboardScreen")
        )
    }

    fun onCardCreated(card: Card) {
        // 记录自定义事件
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

    fun onUserLogin(method: String) {
        // 设置用户 ID
        analyticsService.setUserId("user123")
        
        // 记录登录事件
        analyticsService.logEvent(
            AnalyticsEvent.userLogin(method)
        )
    }

    fun onPurchase(items: List<PurchaseItem>, total: Double) {
        // 记录购买事件
        analyticsService.logEvent(
            AnalyticsEvent.purchase(
                value = total,
                currency = "CNY",
                items = items
            )
        )
    }
}

// 4. 使用类型安全的 AnalyticsValue
val event = AnalyticsEvent(
    name = "custom_event",
    parameters = mapOf(
        "string_param" to AnalyticsValue.Str("value"),
        "int_param" to AnalyticsValue.Int(100L),
        "double_param" to AnalyticsValue.Num(99.99),
        "bool_param" to AnalyticsValue.Bool(true)
    )
)

// 5. 从任意值转换为类型安全的 AnalyticsValue
val value = AnalyticsValue.from(123)  // 返回 AnalyticsValue.Int(123L)
val value2 = AnalyticsValue.from("text")  // 返回 AnalyticsValue.Str("text")
```

## 文档

- [MyHub 统计框架模块方案设计](./docs/myhub-analytics-infra-v1.0.md)
