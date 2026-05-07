# 统计框架设计方案

**版本**: v1.0  
**状态**: ✅ 设计已锁定（Design Final / Sign-off）  
**最后更新**: 2024 年

> ⚠️ **重要提示**：本文档已完成架构评审并通过，设计已锁定。如需修改，请提交设计变更申请。

## 📋 概述

本文档阐述 MyHub 应用的统一统计框架设计方案。该框架旨在支持国内外主流统计服务，提供统一的接口，并能够灵活切换和组合使用不同的统计 SDK。

## 🎯 设计目标

1. **支持国内外统计服务**：同时支持国内（友盟、神策等）和国外（Firebase Analytics、Google Analytics 等）统计平台
2. **统一接口**：提供统一的 API，业务代码无需关心底层统计 SDK 的实现细节
3. **灵活配置**：支持运行时动态切换统计服务，支持多统计服务并行上报
4. **跨平台支持**：支持 Android、iOS、Desktop、Web 等多个平台
5. **易于扩展**：采用适配器模式，便于添加新的统计服务
6. **与现有架构集成**：无缝集成到现有的 Koin DI 和模块化架构中

## 🔍 调研结果

### 国内统计服务

#### 1. 友盟+ (Umeng+)

- **优势**：
    - 国内市场份额最大，覆盖率高
    - 提供完整的移动应用统计分析
    - 支持 Android、iOS 双平台
    - 提供用户行为分析、错误统计等功能
- **劣势**：
    - 主要面向国内市场，海外支持有限
    - SDK 体积相对较大
- **适用场景**：主要面向国内用户的应用

#### 2. 神策数据 (Sensors Analytics)

- **优势**：
    - 提供私有化部署方案
    - 数据安全性和隐私保护更好
    - 支持实时数据分析和用户画像
- **劣势**：
    - 需要服务器资源支持私有化部署
    - 配置相对复杂
- **适用场景**：对数据安全要求高的企业应用

#### 3. 腾讯移动分析 (MTA)

- **优势**：
    - 腾讯生态支持
    - 与微信小程序集成方便
- **劣势**：
    - 功能相对简单
    - 主要面向腾讯生态
- **适用场景**：腾讯生态内的应用

### 国外统计服务

#### 1. Firebase Analytics

- **优势**：
    - Google 官方产品，全球覆盖
    - 与 Firebase 生态深度集成
    - 免费使用，功能强大
    - 支持实时分析和 A/B 测试
    - 提供 Kotlin Multiplatform 支持（通过 expect/actual）
- **劣势**：
    - 国内访问可能受限
    - 需要 Google Play Services（Android）
- **适用场景**：面向海外用户的应用，或需要 Firebase 生态的应用

#### 2. Google Analytics (GA4)

- **优势**：
    - 业界标准，功能全面
    - 强大的数据分析和可视化
    - 支持 Web、移动应用多平台
- **劣势**：
    - 国内访问受限
    - 配置相对复杂
- **适用场景**：Web 应用或面向海外用户的应用

#### 3. Mixpanel

- **优势**：
    - 专注于用户行为分析
    - 实时数据更新
    - 强大的事件追踪功能
- **劣势**：
    - 免费版有事件数量限制
    - 国内访问可能受限
- **适用场景**：需要深度用户行为分析的应用

#### 4. Amplitude

- **优势**：
    - 用户行为分析功能强大
    - 提供产品分析工具
    - 免费版功能丰富
- **劣势**：
    - 国内访问可能受限
- **适用场景**：需要产品分析和用户行为洞察的应用

### 总结

| 统计服务               | 国内支持  | 国外支持  | 跨平台           | 推荐场景     |
|--------------------|-------|-------|---------------|----------|
| 友盟+                | ✅ 优秀  | ⚠️ 有限 | ✅ Android/iOS | 国内应用     |
| 神策数据               | ✅ 优秀  | ✅ 支持  | ✅ 多平台         | 企业应用     |
| Firebase Analytics | ⚠️ 受限 | ✅ 优秀  | ✅ 多平台         | 海外应用     |
| Google Analytics   | ⚠️ 受限 | ✅ 优秀  | ✅ 多平台         | Web/海外应用 |
| Mixpanel           | ⚠️ 受限 | ✅ 优秀  | ✅ 多平台         | 用户行为分析   |
| Amplitude          | ⚠️ 受限 | ✅ 优秀  | ✅ 多平台         | 产品分析     |

## 🏗️ 架构设计

### 整体架构

```text
┌─────────────────────────────────────────────────────────┐
│                   业务层 (ComposeApp)                    │
│  - ViewModel                                            │
│  - UI Components                                         │
│  - Feature Modules                                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ 使用统一接口
                     │
┌────────────────────▼────────────────────────────────────┐
│           核心统计接口层 (core:analytics)                │
│  ┌──────────────────────────────────────────────────┐  │
│  │         AnalyticsService (统一接口)               │  │
│  │  - logEvent(event: AnalyticsEvent)                │  │
│  │  - setUserProperty(key: String, value: Any?)     │  │
│  │  - setUserId(userId: String?)                    │  │
│  │  - setScreen(screenName: String)                 │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │         AnalyticsManager (管理器)                 │  │
│  │  - 管理多个 AnalyticsProvider                     │  │
│  │  - 支持并行上报                                   │  │
│  │  - 支持动态切换                                   │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐      ┌────────▼────────┐
│  适配器层       │      │   配置层        │
│                │      │                │
│ - UmengAdapter │      │ - AnalyticsConfig│
│ - FirebaseAdapter│     │ - ProviderConfig│
│ - MixpanelAdapter│    │ - RegionConfig  │
│ - CustomAdapter │     └─────────────────┘
└───────┬────────┘
        │
┌───────▼────────┐
│  平台实现层     │
│                │
│ - Android      │
│ - iOS          │
│ - JVM          │
│ - JS/WASM      │
└────────────────┘
```

### 核心设计模式

#### 1. 适配器模式 (Adapter Pattern)

- **AnalyticsProvider**：定义统计服务提供商的统一接口
- **具体适配器**：实现不同统计 SDK 的适配（UmengAdapter、FirebaseAdapter 等）
- **优势**：业务代码与具体 SDK 解耦，易于切换和扩展

#### 2. 策略模式 (Strategy Pattern)

- **AnalyticsManager**：根据配置选择不同的统计策略
- **支持多策略并行**：可以同时使用多个统计服务
- **支持动态切换**：根据地区、环境等条件动态选择

#### 3. 工厂模式 (Factory Pattern)

- **AnalyticsProviderFactory**：根据配置创建对应的适配器实例
- **支持延迟初始化**：按需创建适配器，减少启动时间

## 📦 模块设计

### 模块结构

```text
core/analytics/
├── build.gradle.kts
├── README.md
└── src/
    ├── commonMain/
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       ├── AnalyticsEvent.kt              # 统计事件模型
    │       ├── AnalyticsService.kt            # 统一统计接口
    │       ├── AnalyticsProvider.kt           # 统计服务提供商接口
    │       ├── AnalyticsManager.kt            # 统计管理器
    │       ├── AnalyticsConfig.kt            # 统计配置
    │       ├── Region.kt                     # 地区枚举
    │       ├── provider/
    │       │   └── ConsoleProvider.kt         # 控制台输出（所有平台）
    │       └── di/
    │           └── AnalyticsModule.kt        # Koin DI 模块
    │
    ├── androidMain/
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       ├── provider/
    │       │   ├── UmengProvider.kt           # 友盟适配器
    │       │   ├── FirebaseProvider.kt       # Firebase 适配器
    │       │   └── ...
    │       └── AnalyticsManager.android.kt  # Android 特定实现
    │
    ├── iosMain/
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       ├── provider/
    │       │   ├── UmengProvider.kt           # 友盟适配器
    │       │   ├── FirebaseProvider.kt       # Firebase 适配器
    │       │   └── ...
    │       └── AnalyticsManager.ios.kt       # iOS 特定实现
    │
    ├── jvmMain/
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           └── FileProvider.kt            # Desktop 文件输出（CSV/JSON）
    │
    ├── jsMain/
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           └── GoogleAnalyticsProvider.kt # Web GA4 适配器
    │
    ├── wasmJsMain/
    │   └── kotlin/tech/zhifu/app/myhub/analytics/
    │       └── provider/
    │           └── GoogleAnalyticsProvider.kt # Web GA4 适配器
    │
    └── commonTest/
        └── kotlin/tech/zhifu/app/myhub/analytics/
            ├── AnalyticsEventTest.kt
            ├── AnalyticsManagerTest.kt
            └── MockProvider.kt               # 测试用 Mock 适配器
```

### 核心接口定义

#### 1. AnalyticsValue（类型安全的统计值）

```kotlin
/**
 * 类型安全的统计值类型
 *
 * 原因：避免使用 Any 导致的跨平台兼容性问题
 * - JS/WASM 不支持所有 Any 类型
 * - Firebase 只接受 String / Long / Double / Boolean
 * - iOS SDK 也有类型限制
 */
sealed interface AnalyticsValue {
    data class Str(val value: String) : AnalyticsValue
    data class Num(val value: Double) : AnalyticsValue
    data class Int(val value: Long) : AnalyticsValue
    data class Bool(val value: Boolean) : AnalyticsValue

    companion object {
        fun from(value: Any?): AnalyticsValue? = when (value) {
            is String -> Str(value)
            is Double -> Num(value)
            is Float -> Num(value.toDouble())
            is Long -> Int(value)
            is Int -> Int(value.toLong())
            is Short -> Int(value.toLong())
            is Byte -> Int(value.toLong())
            is Boolean -> Bool(value)
            null -> null
            else -> Str(value.toString()) // 兜底：转换为字符串
        }
    }
}
```

#### 2. AnalyticsEvents（事件命名规范）

```kotlin
/**
 * 统计事件命名规范
 * 统一管理事件名称，避免字符串散落，便于维护和重构
 */
object AnalyticsEvents {
    // 页面浏览事件
    const val SCREEN_VIEW = "screen_view"

    // 用户相关事件
    const val USER_LOGIN = "user_login"
    const val USER_LOGOUT = "user_logout"
    const val USER_REGISTER = "user_register"

    // 业务事件（示例）
    const val CARD_CREATED = "card_created"
    const val CARD_UPDATED = "card_updated"
    const val CARD_DELETED = "card_deleted"

    // 电商事件（示例）
    const val PURCHASE = "purchase"
    const val ADD_TO_CART = "add_to_cart"
    const val REMOVE_FROM_CART = "remove_from_cart"

    // 可以按模块继续扩展...
}
```

#### 3. AnalyticsEvent（统计事件）

```kotlin
/**
 * 统计事件模型
 */
data class AnalyticsEvent(
    /** 事件名称（建议使用 AnalyticsEvents 常量） */
    val name: String,
    /** 事件参数（使用类型安全的 AnalyticsValue） */
    val parameters: Map<String, AnalyticsValue> = emptyMap(),
    /** 事件值（可选） */
    val value: Double? = null,
    /** 货币单位（可选，用于电商场景） */
    val currency: String? = null
) {
    companion object {
        // 预定义常用事件
        fun screenView(screenName: String, screenClass: String? = null) = AnalyticsEvent(
            name = AnalyticsEvents.SCREEN_VIEW,
            parameters = buildMap {
                put("screen_name", AnalyticsValue.Str(screenName))
                screenClass?.let { put("screen_class", AnalyticsValue.Str(it)) }
            }
        )

        fun userLogin(method: String) = AnalyticsEvent(
            name = AnalyticsEvents.USER_LOGIN,
            parameters = mapOf("method" to AnalyticsValue.Str(method))
        )

        fun purchase(
            value: Double,
            currency: String,
            items: List<PurchaseItem>
        ) = AnalyticsEvent(
            name = AnalyticsEvents.PURCHASE,
            value = value,
            currency = currency,
            parameters = mapOf(
                "items_count" to AnalyticsValue.Int(items.size.toLong())
            )
        )
    }
}

/**
 * 购买项（用于电商场景）
 */
data class PurchaseItem(
    val itemId: String,
    val itemName: String,
    val category: String? = null,
    val quantity: Int = 1,
    val price: Double
)
```

#### 4. AnalyticsService（统一统计接口）

```kotlin
/**
 * 统一统计服务接口
 * 业务代码通过此接口进行统计上报
 */
interface AnalyticsService {
    /**
     * 记录事件
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * 批量记录事件（用于高频事件场景）
     * 某些 Provider 可能支持批量上报以提高性能
     */
    fun logEvents(events: List<AnalyticsEvent>) {
        events.forEach { logEvent(it) }
    }

    /**
     * 设置用户属性
     */
    fun setUserProperty(key: String, value: AnalyticsValue?)

    /**
     * 设置用户 ID
     */
    fun setUserId(userId: String?)

    /**
     * 设置当前屏幕
     */
    fun setScreen(screenName: String, screenClass: String? = null)

    /**
     * 重置用户数据（登出时调用）
     */
    fun reset()
}
```

#### 5. AnalyticsConsent（隐私合规接口）

```kotlin
/**
 * 统计服务隐私合规接口
 * 用于处理 GDPR、CCPA 等隐私法规要求
 */
interface AnalyticsConsent {
    /**
     * 是否允许统计
     */
    fun isAnalyticsAllowed(): Boolean

    /**
     * 是否允许个性化统计
     */
    fun isPersonalizationAllowed(): Boolean

    /**
     * 更新用户同意状态
     */
    fun updateConsent(
        analyticsAllowed: Boolean,
        personalizationAllowed: Boolean = false
    )
}

/**
 * 默认实现：始终允许（用于测试或不需要合规的场景）
 */
class DefaultAnalyticsConsent(
    private var analyticsAllowed: Boolean = true,
    private var personalizationAllowed: Boolean = false
) : AnalyticsConsent {
    override fun isAnalyticsAllowed() = analyticsAllowed
    override fun isPersonalizationAllowed() = personalizationAllowed

    override fun updateConsent(
        analyticsAllowed: Boolean,
        personalizationAllowed: Boolean
    ) {
        this.analyticsAllowed = analyticsAllowed
        this.personalizationAllowed = personalizationAllowed
    }
}
```

#### 6. AnalyticsProvider（统计服务提供商接口）

```kotlin
/**
 * 统计服务提供商接口
 * 各个统计 SDK 的适配器实现此接口
 */
interface AnalyticsProvider {
    /** 提供商名称 */
    val name: String

    /** 是否已初始化 */
    val isInitialized: Boolean

    /**
     * Provider 就绪状态（StateFlow）
     * 用于监听初始化完成状态，避免竞态条件
     */
    val isReady: StateFlow<Boolean>

    /** 支持的平台 */
    val supportedPlatforms: Set<Platform>

    /** 支持的地区 */
    val supportedRegions: Set<Region>

    /**
     * 初始化统计服务
     */
    suspend fun initialize(config: ProviderConfig)

    /**
     * 记录事件
     *
     * 注意：如果 Provider 未就绪，事件会被缓冲
     * 初始化完成后会自动上报缓冲的事件
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * 设置用户属性
     */
    fun setUserProperty(key: String, value: AnalyticsValue?)

    /**
     * 设置用户 ID
     */
    fun setUserId(userId: String?)

    /**
     * 设置当前屏幕
     */
    fun setScreen(screenName: String, screenClass: String? = null)

    /**
     * 重置用户数据
     */
    fun reset()
}

/**
 * Provider 基础实现（抽象类）
 * 提供事件缓冲机制，避免初始化期间的竞态条件
 */
abstract class BaseAnalyticsProvider : AnalyticsProvider {
    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val eventBuffer = mutableListOf<AnalyticsEvent>()
    private val bufferLock = Mutex()

    /**
     * Provider 内部协程作用域
     * 用于非阻塞的事件缓冲操作
     */
    private val providerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    override val isInitialized: Boolean
        get() = _isReady.value

    /**
     * 子类在初始化完成后调用此方法
     */
    protected suspend fun markAsReady() {
        _isReady.value = true
        // 上报缓冲的事件
        flushBufferedEvents()
    }

    override fun logEvent(event: AnalyticsEvent) {
        if (_isReady.value) {
            // 已就绪，直接上报
            doLogEvent(event)
        } else {
            // 未就绪，非阻塞加入缓冲队列
            // 使用 providerScope 避免在主线程使用 runBlocking
            providerScope.launch {
                bufferLock.withLock {
                    eventBuffer.add(event)
                }
            }
        }
    }

    /**
     * 子类实现具体的上报逻辑
     */
    protected abstract fun doLogEvent(event: AnalyticsEvent)

    /**
     * 上报缓冲的事件
     */
    private suspend fun flushBufferedEvents() {
        val events = bufferLock.withLock {
            eventBuffer.toList().also { eventBuffer.clear() }
        }
        events.forEach { doLogEvent(it) }
    }

    /**
     * 清理资源（可选，用于测试或应用退出）
     */
    open fun cleanup() {
        providerScope.cancel()
    }
}

/**
 * 平台枚举
 */
enum class Platform {
    ANDROID, IOS, JVM, JS, WASM
}

/**
 * 地区枚举
 */
enum class Region {
    DOMESTIC,  // 国内
    OVERSEAS   // 海外
}
```

#### 7. AnalyticsConfig（统计配置）

```kotlin
/**
 * 统计配置
 */
data class AnalyticsConfig(
    /** 当前地区 */
    val region: Region,
    /** 是否启用统计 */
    val enabled: Boolean = true,
    /** 是否启用调试模式 */
    val debugMode: Boolean = false,
    /** 提供商配置列表 */
    val providers: List<ProviderConfig> = emptyList()
)

/**
 * 提供商配置
 */
data class ProviderConfig(
    /** 提供商类型 */
    val type: ProviderType,
    /** 是否启用 */
    val enabled: Boolean = true,
    /** 应用 Key（如友盟 AppKey、Firebase AppId） */
    val appKey: String? = null,
    /** 应用 Secret（如友盟 AppSecret） */
    val appSecret: String? = null,
    /** 其他自定义参数 */
    val customParams: Map<String, String> = emptyMap()
)

/**
 * 提供商类型
 */
enum class ProviderType {
    UMENG,           // 友盟+
    SENSORS,         // 神策数据
    FIREBASE,        // Firebase Analytics
    GOOGLE_ANALYTICS, // Google Analytics (GA4)
    MIXPANEL,        // Mixpanel
    AMPLITUDE,       // Amplitude
    CONSOLE          // 控制台输出（用于测试和 Desktop）
}
```

#### 8. AnalyticsProviderFactory（提供商工厂 - Registry 模式）

```kotlin
/**
 * 统计服务提供商工厂
 * 使用 Registry 模式，支持动态注册 Provider
 */
class AnalyticsProviderFactory(
    private val logger: Logger
) {
    /**
     * Provider 创建器类型
     */
    private typealias ProviderCreator = (ProviderConfig) -> AnalyticsProvider

    /**
     * Provider 注册表
     */
    private val creators = mutableMapOf<ProviderType, ProviderCreator>()

    init {
        // 注册默认 Provider
        registerDefaultProviders()
    }

    /**
     * 注册 Provider 创建器
     */
    fun register(type: ProviderType, creator: ProviderCreator) {
        creators[type] = creator
        logger.debug { "Registered analytics provider: $type" }
    }

    /**
     * 创建 Provider 实例
     */
    fun create(config: ProviderConfig): AnalyticsProvider {
        val creator = creators[config.type]
            ?: throw IllegalArgumentException("Unknown provider type: ${config.type}")

        return try {
            creator(config)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create provider: ${config.type}" }
            throw e
        }
    }

    /**
     * 注册默认 Provider
     */
    private fun registerDefaultProviders() {
        register(ProviderType.CONSOLE) { ConsoleProvider() }
        // 注意：平台特定的 Provider 需要通过 AnalyticsProviderRegistrar 注册
        // 参见各平台模块的注册实现
    }
}

/**
 * Provider 注册器接口
 * 各平台模块实现此接口，在应用启动时注册平台特定的 Provider
 */
interface AnalyticsProviderRegistrar {
    /**
     * 注册该平台支持的 Provider
     */
    fun register(factory: AnalyticsProviderFactory)
}
```

#### 9. AnalyticsManager（统计管理器）

```kotlin
/**
 * 统计管理器
 * 管理多个统计服务提供商，提供统一的统计接口
 */
class AnalyticsManager(
    private val config: AnalyticsConfig,
    private val consent: AnalyticsConsent,
    private val logger: Logger,
    private val providerFactory: AnalyticsProviderFactory
) : AnalyticsService {

    private val providers = mutableListOf<AnalyticsProvider>()

    /**
     * 初始化所有启用的提供商
     */
    suspend fun initialize() {
        if (!config.enabled) {
            logger.info { "Analytics is disabled" }
            return
        }

        // 检查隐私合规
        if (!consent.isAnalyticsAllowed()) {
            logger.info { "Analytics is not allowed by user consent" }
            return
        }

        // Debug 模式下自动注入 ConsoleProvider（如果未配置）
        if (config.debugMode && config.providers.none { it.type == ProviderType.CONSOLE }) {
            try {
                val consoleProvider = providerFactory.create(
                    ProviderConfig(type = ProviderType.CONSOLE, enabled = true)
                )
                consoleProvider.initialize(ProviderConfig(type = ProviderType.CONSOLE))
                providers.add(consoleProvider)
                logger.info { "Debug mode: ConsoleProvider auto-injected" }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to auto-inject ConsoleProvider in debug mode" }
            }
        }

        config.providers
            .filter { it.enabled }
            .forEach { providerConfig ->
                try {
                    val provider = providerFactory.create(providerConfig)
                    if (provider.supportedRegions.contains(config.region)) {
                        provider.initialize(providerConfig)
                        providers.add(provider)
                        logger.info { "Analytics provider initialized: ${provider.name}" }
                    } else {
                        logger.warn { "Provider ${provider.name} does not support region ${config.region}" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to initialize provider: ${providerConfig.type}" }
                }
            }
    }

    override fun logEvents(events: List<AnalyticsEvent>) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                // 如果 Provider 支持批量上报，可以优化
                events.forEach { provider.logEvent(it) }
            } catch (e: Exception) {
                logger.error(e) { "Failed to log events to ${provider.name}" }
            }
        }
    }

    override fun logEvent(event: AnalyticsEvent) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.logEvent(event)
            } catch (e: Exception) {
                logger.error(e) { "Failed to log event to ${provider.name}" }
            }
        }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.setUserProperty(key, value)
            } catch (e: Exception) {
                logger.error(e) { "Failed to set user property to ${provider.name}" }
            }
        }
    }

    override fun setUserId(userId: String?) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.setUserId(userId)
            } catch (e: Exception) {
                logger.error(e) { "Failed to set user ID to ${provider.name}" }
            }
        }
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        if (!config.enabled || !consent.isAnalyticsAllowed()) return

        providers.forEach { provider ->
            try {
                provider.setScreen(screenName, screenClass)
            } catch (e: Exception) {
                logger.error(e) { "Failed to set screen to ${provider.name}" }
            }
        }
    }

    override fun reset() {
        if (!config.enabled) return

        providers.forEach { provider ->
            try {
                provider.reset()
            } catch (e: Exception) {
                logger.error(e) { "Failed to reset ${provider.name}" }
            }
        }
    }
}
```

## 🔧 实现方案

### 1. 适配器实现示例

#### UmengProvider（友盟适配器）

```kotlin
/**
 * 友盟统计适配器（Android）
 */
class UmengProvider : BaseAnalyticsProvider() {
    override val name = "Umeng"
    override val supportedPlatforms = setOf(Platform.ANDROID, Platform.IOS)
    override val supportedRegions = setOf(Region.DOMESTIC)

    private var umengAgent: Any? = null // 友盟 SDK 实例

    override suspend fun initialize(config: ProviderConfig) {
        // 初始化友盟 SDK
        // 注意：实际实现需要使用 expect/actual 模式
        // Android: 使用友盟 Android SDK
        // iOS: 使用友盟 iOS SDK

        // 初始化完成后标记为就绪
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        // 转换为友盟事件格式并上报
        // 将 AnalyticsValue 转换为友盟 SDK 接受的类型
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        // 设置友盟用户属性
        // 将 AnalyticsValue 转换为友盟 SDK 接受的类型
    }

    override fun setUserId(userId: String?) {
        // 设置友盟用户 ID
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        // 设置友盟页面统计
    }

    override fun reset() {
        // 重置友盟用户数据
    }
}
```

#### FirebaseProvider（Firebase 适配器）

```kotlin
/**
 * Firebase Analytics 适配器
 */
class FirebaseProvider : BaseAnalyticsProvider() {
    override val name = "Firebase Analytics"
    override val supportedPlatforms = setOf(Platform.ANDROID, Platform.IOS, Platform.JS)
    override val supportedRegions = setOf(Region.OVERSEAS)

    private var firebaseAnalytics: Any? = null

    override suspend fun initialize(config: ProviderConfig) {
        // 初始化 Firebase Analytics
        // 使用 expect/actual 模式实现平台特定初始化

        // 初始化完成后标记为就绪
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        // 转换为 Firebase 事件格式并上报
        // Firebase 只接受 String / Long / Double / Boolean
        // AnalyticsValue 已经保证了类型安全
    }

    // ... 其他方法实现
}
```

#### ConsoleProvider（控制台输出 - 所有平台/测试）

```kotlin
/**
 * 控制台输出 Provider
 * 用于所有平台的测试和调试环境
 * 通过 logger 输出统计事件，便于开发和调试
 */
class ConsoleProvider(
    private val logger: Logger = logger("Analytics.ConsoleProvider")
) : BaseAnalyticsProvider() {
    override val name = "Console"
    override val supportedPlatforms = setOf(
        Platform.ANDROID,
        Platform.IOS,
        Platform.JVM,
        Platform.JS,
        Platform.WASM
    )
    override val supportedRegions = setOf(Region.DOMESTIC, Region.OVERSEAS)

    override suspend fun initialize(config: ProviderConfig) {
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        val params = buildString {
            append("Event: ${event.name}")
            if (event.parameters.isNotEmpty()) {
                append("\n  Parameters:")
                event.parameters.forEach { (key, value) ->
                    append("\n    $key: ${formatValue(value)}")
                }
            }
            event.value?.let { append("\n  value: $it") }
            event.currency?.let { append("\n  currency: $it") }
        }
        logger.info { params }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        logger.info { "UserProperty: $key = ${value?.let { formatValue(it) } ?: "null"}" }
    }

    override fun setUserId(userId: String?) {
        logger.info { "UserId: $userId" }
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        val screenInfo = buildString {
            append("Screen: $screenName")
            screenClass?.let { append(" (class: $it)") }
        }
        logger.info { screenInfo }
    }

    override fun reset() {
        logger.info { "Reset" }
    }

    private fun formatValue(value: AnalyticsValue): String = when (value) {
        is AnalyticsValue.Str -> value.value
        is AnalyticsValue.Num -> value.value.toString()
        is AnalyticsValue.Int -> value.value.toString()
        is AnalyticsValue.Bool -> value.value.toString()
    }
}
```

#### FileProvider（文件输出 - Desktop）

```kotlin
/**
 * 文件输出 Provider
 * 用于 Desktop 平台，将事件写入文件（CSV/JSON）
 * 适用于 QA、自动化测试、内部分析
 */
class FileProvider(
    private val outputDir: String = "./analytics",
    private val format: FileFormat = FileFormat.JSON
) : BaseAnalyticsProvider() {
    override val name = "File"
    override val supportedPlatforms = setOf(Platform.JVM)
    override val supportedRegions = setOf(Region.DOMESTIC, Region.OVERSEAS)

    private val fileWriter by lazy {
        createFileWriter()
    }

    enum class FileFormat {
        JSON, CSV
    }

    override suspend fun initialize(config: ProviderConfig) {
        // 创建输出目录
        ensureOutputDir()
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        when (format) {
            FileFormat.JSON -> writeJsonEvent(event)
            FileFormat.CSV -> writeCsvEvent(event)
        }
    }

    private fun writeJsonEvent(event: AnalyticsEvent) {
        val json = buildJsonObject {
            put("timestamp", System.currentTimeMillis())
            put("event", event.name)
            putJsonObject("parameters") {
                event.parameters.forEach { (key, value) ->
                    put(key, value.toString())
                }
            }
            event.value?.let { put("value", it) }
            event.currency?.let { put("currency", it) }
        }
        fileWriter.appendLine(json.toString())
    }

    private fun writeCsvEvent(event: AnalyticsEvent) {
        // CSV 格式实现
    }

    private fun createFileWriter(): Appendable {
        val file = File(outputDir, "analytics_${System.currentTimeMillis()}.${format.name.lowercase()}")
        return file.bufferedWriter()
    }

    private fun ensureOutputDir() {
        File(outputDir).mkdirs()
    }

    // ... 其他方法实现
}
```

### 2. 配置管理

#### 配置文件示例

```kotlin
/**
 * 根据构建变体或运行时环境创建配置
 */
object AnalyticsConfigFactory {
    /**
     * 创建国内配置（使用友盟）
     */
    fun createDomesticConfig(): AnalyticsConfig {
        return AnalyticsConfig(
            region = Region.DOMESTIC,
            enabled = true,
            debugMode = BuildConfig.DEBUG,
            providers = listOf(
                ProviderConfig(
                    type = ProviderType.UMENG,
                    enabled = true,
                    appKey = "your_umeng_app_key",
                    appSecret = "your_umeng_app_secret"
                )
            )
        )
    }

    /**
     * 创建海外配置（使用 Firebase）
     */
    fun createOverseasConfig(): AnalyticsConfig {
        return AnalyticsConfig(
            region = Region.OVERSEAS,
            enabled = true,
            debugMode = BuildConfig.DEBUG,
            providers = listOf(
                ProviderConfig(
                    type = ProviderType.FIREBASE,
                    enabled = true,
                    appKey = "your_firebase_app_id"
                )
            )
        )
    }

    /**
     * 创建混合配置（同时使用多个服务）
     */
    fun createHybridConfig(): AnalyticsConfig {
        return AnalyticsConfig(
            region = Region.DOMESTIC, // 根据实际需求设置
            enabled = true,
            debugMode = BuildConfig.DEBUG,
            providers = listOf(
                ProviderConfig(
                    type = ProviderType.UMENG,
                    enabled = true,
                    appKey = "your_umeng_app_key"
                ),
                ProviderConfig(
                    type = ProviderType.FIREBASE,
                    enabled = true,
                    appKey = "your_firebase_app_id"
                )
            )
        )
    }

    /**
     * 根据地区自动选择配置
     */
    fun createAutoConfig(): AnalyticsConfig {
        val region = detectRegion() // 检测用户所在地区
        return when (region) {
            Region.DOMESTIC -> createDomesticConfig()
            Region.OVERSEAS -> createOverseasConfig()
        }
    }

    /**
     * 检测用户所在地区
     */
    private fun detectRegion(): Region {
        // 实现地区检测逻辑
        // 可以通过 IP、时区、语言设置等判断
        return Region.DOMESTIC // 默认值
    }
}
```

### 3. 平台 Provider 注册器实现示例

#### Android 平台注册器

```kotlin
// androidMain/kotlin/.../AndroidAnalyticsRegistrar.kt
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {
    private val commonRegistrar = CommonAnalyticsRegistrar()

    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider - 所有平台都支持）
        commonRegistrar.register(factory)

        // 注册 Android 特定的 Provider
        factory.register(ProviderType.UMENG) { config ->
            UmengProvider().apply {
                // Android 特定初始化
            }
        }

        factory.register(ProviderType.FIREBASE) { config ->
            FirebaseProvider().apply {
                // Android 特定初始化
            }
        }
    }
}
```

#### iOS 平台注册器

```kotlin
// iosMain/kotlin/.../IosAnalyticsRegistrar.kt
class IosAnalyticsRegistrar : AnalyticsProviderRegistrar {
    private val commonRegistrar = CommonAnalyticsRegistrar()

    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider - 所有平台都支持）
        commonRegistrar.register(factory)

        // 注册 iOS 特定的 Provider
        factory.register(ProviderType.UMENG) { config ->
            UmengProvider().apply {
                // iOS 特定初始化
            }
        }

        factory.register(ProviderType.FIREBASE) { config ->
            FirebaseProvider().apply {
                // iOS 特定初始化
            }
        }
    }
}
```

#### JVM 平台注册器（Desktop）

```kotlin
// jvmMain/kotlin/.../JvmAnalyticsRegistrar.kt
class JvmAnalyticsRegistrar : AnalyticsProviderRegistrar {
    private val commonRegistrar = CommonAnalyticsRegistrar()

    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider）
        commonRegistrar.register(factory)

        // 注册 JVM 平台特定的 FileProvider
        factory.register(ProviderType.FILE) { config ->
            val outputDir = config.customParams["outputDir"] ?: "./analytics"
            val format = when (config.customParams["format"]?.uppercase()) {
                "CSV" -> FileProvider.FileFormat.CSV
                else -> FileProvider.FileFormat.JSON
            }
            FileProvider(outputDir = outputDir, format = format)
        }
    }
}
```

#### JS/WASM 平台注册器（Web）

```kotlin
// jsMain/kotlin/.../JsAnalyticsRegistrar.kt
class JsAnalyticsRegistrar : AnalyticsProviderRegistrar {
    private val commonRegistrar = CommonAnalyticsRegistrar()

    override fun register(factory: AnalyticsProviderFactory) {
        // 先注册通用 Provider（ConsoleProvider）
        commonRegistrar.register(factory)

        // 注册 Web 特定的 Provider
        factory.register(ProviderType.GOOGLE_ANALYTICS) { config ->
            GoogleAnalyticsProvider(config)
        }
    }
}
```

### 4. Koin 依赖注入集成

```kotlin
/**
 * Analytics Koin 模块
 */
fun analyticsModule(
    config: () -> AnalyticsConfig,
    consent: AnalyticsConsent = DefaultAnalyticsConsent(),
    registrar: AnalyticsProviderRegistrar? = null
): Module = module {
    single<AnalyticsConfig> { config() }

    single<AnalyticsConsent> { consent }

    single<AnalyticsProviderFactory> {
        val factory = AnalyticsProviderFactory(get())
        // 注册平台特定的 Provider
        registrar?.register(factory)
        factory
    }

    single<AnalyticsManager> {
        AnalyticsManager(
            config = get(),
            consent = get(),
            logger = get(),
            providerFactory = get()
        )
    }

    single<AnalyticsService> { get<AnalyticsManager>() }
}

/**
 * 应用级 CoroutineScope
 * 用于管理统计服务的初始化，避免使用 GlobalScope
 */
class AppCoroutineScope : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    fun cancel() {
        job.cancel()
    }
}
```

### 5. 使用示例

#### 在应用启动时初始化

```kotlin
// composeApp/src/commonMain/kotlin/.../App.kt
fun initKoin() {
    val appScope = AppCoroutineScope()

    // 创建平台特定的注册器
    val registrar = when {
        Platform.isAndroid() -> AndroidAnalyticsRegistrar()
        Platform.isIOS() -> IosAnalyticsRegistrar()
        else -> null
    }

    startKoin {
        modules(
            // ... 其他模块
            single<AppCoroutineScope> { appScope },
            analyticsModule(
                config = {
                    AnalyticsConfigFactory.createAutoConfig()
                },
                consent = DefaultAnalyticsConsent(
                    analyticsAllowed = true, // 从用户设置或隐私政策获取
                    personalizationAllowed = false
                ),
                registrar = registrar
            )
        )
    }

    // 初始化统计服务（使用应用级 Scope，而非 GlobalScope）
    appScope.launch {
        getKoin().get<AnalyticsManager>().initialize()
    }
}
```

#### 在业务代码中使用

```kotlin
// ViewModel 中使用
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

    fun onBatchCardCreated(cards: List<Card>) {
        // 批量上报示例
        val events = cards.map { card ->
            AnalyticsEvent(
                name = AnalyticsEvents.CARD_CREATED,
                parameters = mapOf(
                    "card_id" to AnalyticsValue.Str(card.id),
                    "card_type" to AnalyticsValue.Str(card.type.name)
                )
            )
        }
        analyticsService.logEvents(events)
    }

    fun onUserLogin(method: String) {
        analyticsService.logEvent(
            AnalyticsEvent.userLogin(method)
        )
    }
}

// Composable 中使用
@Composable
fun DashboardScreen(
    analyticsService: AnalyticsService = getKoin().get()
) {
    LaunchedEffect(Unit) {
        analyticsService.setScreen("Dashboard")
    }

    // ... UI 代码
}
```

## 📋 实施计划

### 阶段一：核心框架搭建（1-2 周）

1. **创建 core:analytics 模块**

    - 定义核心接口（AnalyticsService、AnalyticsProvider）
    - 实现 AnalyticsManager
    - 实现 AnalyticsConfig 和配置管理
    - 创建 Koin DI 模块

2. **实现基础适配器**

    - ConsoleProvider（用于所有平台的测试和调试，通过 logger 输出）
    - MockProvider（用于单元测试）

3. **编写单元测试**
    - AnalyticsManager 测试
    - AnalyticsEvent 测试
    - 配置管理测试

### 阶段二：国内统计服务集成（2-3 周）

1. **友盟适配器实现**

    - Android 平台实现
    - iOS 平台实现
    - 集成测试

2. **神策数据适配器实现**（可选）
    - 根据实际需求决定是否实现

### 阶段三：海外统计服务集成（2-3 周）

1. **Firebase Analytics 适配器实现**

    - Android 平台实现
    - iOS 平台实现
    - Web 平台实现（JS/WASM）

2. **Google Analytics 适配器实现**（可选）
    - Web 平台实现

### 阶段四：集成和优化（1-2 周）

1. **集成到应用**

    - 在 composeApp 中集成
    - 在各个 Feature 模块中添加统计埋点

2. **性能优化**

    - 异步上报优化
    - 批量上报支持
    - 网络异常处理

3. **文档完善**
    - 使用文档
    - API 文档
    - 最佳实践指南

## 🔴 关键改进点（架构评审反馈）

基于架构评审，以下改进点已纳入设计方案：

### 1. ✅ 类型安全：AnalyticsValue 替代 Any

**问题**：`Map<String, Any>` 在 KMP 中存在跨平台兼容性风险

- JS/WASM 不支持所有 Any 类型
- Firebase 只接受 String / Long / Double / Boolean
- iOS SDK 也有类型限制

**解决方案**：使用 `sealed interface AnalyticsValue` 定义类型安全的统计值

- 编译期类型检查
- 运行时类型安全
- 跨平台兼容性保证
- **已修复**：`AnalyticsService.setUserProperty` 统一使用 `AnalyticsValue?`

### 2. ✅ Provider 初始化竞态条件处理

**问题**：初始化完成前调用 `logEvent()` 会导致事件丢失

**解决方案**：

- `BaseAnalyticsProvider` 提供事件缓冲机制
- `isReady: StateFlow<Boolean>` 监听初始化状态
- 初始化期间的事件自动缓冲，完成后批量上报
- **已修复**：使用 `providerScope` 替代 `runBlocking`，避免主线程阻塞和 ANR 风险

### 3. ✅ 应用级 CoroutineScope 替代 GlobalScope

**问题**：`GlobalScope` 不可控、无法 cancel、不符合 structured concurrency

**解决方案**：

- `AppCoroutineScope` 应用级 Scope
- 使用 `SupervisorJob()` 管理生命周期
- 支持应用退出时正确取消

### 4. ✅ 隐私合规：ConsentManager 集成

**问题**：GDPR、CCPA 等隐私法规要求用户同意机制

**解决方案**：

- `AnalyticsConsent` 接口定义隐私合规
- `AnalyticsManager` 在所有统计操作前检查同意状态
- 支持动态更新用户同意状态

### 5. ✅ ProviderFactory Registry 模式 + 平台注册器

**问题**：避免 `when(type)` 巨型分支，提高可扩展性；KMP 下平台特定 Provider 注册时机不明确

**解决方案**：

- `AnalyticsProviderFactory` 使用 Registry 模式
- **新增**：`AnalyticsProviderRegistrar` 接口，各平台模块实现
- **新增**：`CommonAnalyticsRegistrar` 注册所有平台都支持的 Provider（如 ConsoleProvider）
- **新增**：所有平台（Android、iOS、JVM、JS、WASM）默认注册 ConsoleProvider
- **新增**：Android/iOS/JVM/JS/WASM 平台注册器实现
- 支持动态注册 Provider
- 支持插件式扩展（未来可支持 feature module）

### 6. ✅ Desktop 平台：FileProvider 支持

**优势**：

- QA 测试数据收集
- 自动化测试验证
- 内部分析和调试

**实现**：`FileProvider` 将事件写入 CSV/JSON 文件

### 7. ✅ Web/WASM 平台：仅支持 GA4 和 Console

**原因**：Web 世界统计模型与移动端不同，不要强行对齐

**方案**：Web 平台只实现 `GoogleAnalyticsProvider` 和 `ConsoleProvider`（ConsoleProvider 支持所有平台）

### 8. ✅ 事件命名规范：AnalyticsEvents 对象

**新增**：`AnalyticsEvents` 对象统一管理事件名称常量

- 避免字符串散落
- 便于维护和重构
- 支持 IDE 自动补全

### 9. ✅ Debug 模式自动注入 ConsoleProvider

**新增**：Debug 模式下自动注入 `ConsoleProvider`（如果未配置）

- 提升开发体验
- 方便本地调试
- 无需手动配置

### 10. ✅ 批量事件接口

**新增**：`logEvents(events: List<AnalyticsEvent>)` 方法

- 支持高频事件场景
- 为未来批量上报优化预留接口
- 提升性能潜力

## ⚠️ 注意事项

### 1. 平台特定实现

- 使用 `expect/actual` 机制实现平台特定的 SDK 调用
- Android 和 iOS 需要分别集成对应的原生 SDK
- Web 平台可能需要使用 JavaScript 互操作

### 2. 隐私合规

- ✅ **已实现**：`AnalyticsConsent` 接口提供用户同意机制
- ✅ **已实现**：`AnalyticsManager` 在所有操作前检查同意状态
- 遵守 GDPR、CCPA 等隐私法规
- 支持数据删除请求（通过 `reset()` 方法）

### 3. 性能考虑

- 统计上报不应阻塞主线程
- 使用协程进行异步上报
- 考虑批量上报以减少网络请求

### 4. 错误处理

- 统计上报失败不应影响应用功能
- 记录上报失败的日志
- 支持重试机制

### 5. 测试支持

- 提供 Mock 实现用于单元测试
- 提供测试模式，避免测试数据污染生产数据
- 支持本地测试环境

## 🔗 相关文档

- [项目架构文档](../../../docs/myhub_architecture.md)
- [网络模块文档](../../network/README.md)
- [日志模块文档](../../logger/README.md)
- [平台模块文档](../../platform/README.md)

## 📚 参考资料

- [友盟+ 官方文档](https://developer.umeng.com/docs/119267)
- [Firebase Analytics 文档](https://firebase.google.com/docs/analytics)
- [Google Analytics GA4 文档](https://developers.google.com/analytics/devguides/collection/ga4)
- [Kotlin Multiplatform 官方文档](https://kotlinlang.org/docs/multiplatform.html)

---

## 📝 版本历史

| 版本   | 日期     | 变更说明          | 状态    |
|------|--------|---------------|-------|
| v1.0 | 2024 年 | 初始设计完成，通过架构评审 | ✅ 已锁定 |

---

## ✅ 设计评审记录

**评审日期**: 2024 年  
**评审结论**: ✅ **通过**  
**评审意见**:

- 架构设计：⭐⭐⭐⭐⭐
- KMP 适配：⭐⭐⭐⭐⭐
- 工程可落地性：⭐⭐⭐⭐⭐
- 扩展性：⭐⭐⭐⭐⭐

**评审结论**: 该方案已达到「企业级 + KMP 官方友好 + 可长期演进」标准，可以正式定稿并进入实现阶段。

**评审记录**【由ChatGPT负责](https://chatgpt.com/share/695be661-70ec-8006-98a7-e0bf000faa4e)

---

**文档状态**: ✅ 设计已锁定，可进入实现阶段
