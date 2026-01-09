# core:analytics

统一统计框架模块，提供跨平台的统计服务抽象和实现。

## 📋 功能特性

- ✅ **统一接口**：提供 `AnalyticsService` 统一接口，业务代码无需关心底层 SDK
- ✅ **多 Provider 支持**：支持同时使用多个统计服务（Firebase、Umeng 等）
- ✅ **渠道框架**：支持通过构建变体（Build Variant）选择不同的统计服务
- ✅ **类型安全**：使用 `AnalyticsValue` 替代 `Any`，避免跨平台兼容性问题
- ✅ **事件缓冲**：Provider 初始化期间的事件自动缓冲，避免丢失
- ✅ **隐私合规**：内置 `AnalyticsConsent` 支持 GDPR、CCPA 等隐私法规
- ✅ **跨平台支持**：支持 Android、iOS、Desktop、Web 等多个平台
- ✅ **易于扩展**：采用 Registry 模式，便于添加新的统计服务

## 🎯 快速开始

### 1. 在 Koin 中配置

```kotlin
import tech.zhifu.app.myhub.analytics.di.analyticsModule

fun initKoin() {
    startKoin {
        modules(
            analyticsModule()  // 平台特定的配置已自动处理
        )
    }

    // 初始化统计服务
    GlobalScope.launch {
        delay(100)
        getKoin().get<AnalyticsManager>().initialize()
    }
}
```

**注意**：平台特定的 Provider 注册器已通过 `expect/actual` 机制和渠道变体系统内置在 `core:analytics` 模块中，应用层无需关心平台特定的实现细节。

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

- **平台**：所有平台（Android、iOS、JVM、JS、WASM）
- **用途**：所有平台的测试和调试环境
- **输出**：通过 logger 输出到控制台
- **自动注册**：Debug 模式下自动注册

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

### FirebaseProvider

- **平台**：Android、iOS、Web (JS/WASM)
- **渠道**：`googlePlay`
- **用途**：Google Play 应用商店和海外市场
- **配置**：
  - Android: 通过 `google-services.json` 自动配置
  - iOS: 通过 `GoogleService-Info.plist` 自动配置
  - Web: 通过 `FirebaseOptions` 配置

### UmengProvider

- **平台**：Android、iOS
- **渠道**：`umeng`
- **用途**：国内市场和友盟统计
- **配置**：
  - Android: 需要 `appKey` 和 `channel`
  - iOS: 需要 `appKey` 和 `channel`，使用 cinterop 调用原生 SDK

## 🏗️ 架构设计

### 渠道框架

框架采用**平台 + 渠道**的双维度变体系统，支持在不同平台和渠道下注册不同的 Analytics Provider。

#### 目录结构

```
core/analytics/src/
├── commonMain/                    # 所有平台和渠道共享的代码
│   ├── AnalyticsProvider.kt       # Provider 接口定义
│   ├── AnalyticsManager.kt        # 核心管理器
│   ├── AnalyticsModule.kt         # Koin DI 模块
│   └── provider/
│       ├── CommonAnalyticsRegistrar.kt  # 通用注册器（ConsoleProvider）
│       └── ConsoleProvider.kt     # 控制台输出 Provider
│
├── {platform}Main/                # 平台标准代码
│   ├── androidMain/                # Android 平台标准代码
│   ├── iosMain/                   # iOS 平台标准代码
│   ├── jsMain/                    # JS 平台标准代码
│   └── jvmMain/                   # JVM 平台标准代码
│
├── {platform}{Channel}Main/       # 平台 + 渠道组合代码（互斥）
│   ├── androidGooglePlayMain/     # Android + Google Play
│   ├── androidUmengMain/          # Android + Umeng
│   ├── iosGooglePlayMain/         # iOS + Google Play
│   ├── iosUmengMain/              # iOS + Umeng
│   └── jsGooglePlayMain/          # JS + Google Play
```

#### 渠道配置

通过构建参数选择渠道：

```bash
# 使用 Google Play 渠道（Firebase）
./gradlew build -PappChannel=googlePlay

# 使用 Umeng 渠道
./gradlew build -PappChannel=umeng

# 默认渠道（仅 ConsoleProvider）
./gradlew build
```

或在 `gradle.properties` 中设置：

```properties
appChannel=umeng  # 或 googlePlay
```

#### 渠道互斥性

- `{platform}ChannelMain` 和 `{platform}{Channel}Main` 是互斥的
- 构建时只会注入一个渠道的代码
- 例如：`iosChannelMain` 和 `iosUmengMain` 不会同时存在

详细设计文档请参考：[渠道框架设计文档](docs/CHANNEL_FRAMEWORK_DESIGN.md)

## 🔧 iOS CInterop 配置

### Umeng iOS SDK 集成

iOS 平台使用 Kotlin/Native cinterop 调用 Umeng SDK：

#### 文件结构

```
src/iosUmengMain/
├── cinterop/
│   └── tech/zhifu/app/myhub/analytics/provider/
│       └── umeng.def              # cinterop 定义文件
└── kotlin/
    └── tech/zhifu/app/myhub/analytics/provider/
        ├── UmengProvider.kt       # Umeng Provider 实现
        └── IosAnalyticsRegistrar.kt  # iOS 注册器
```

#### CInterop 定义文件

`umeng.def` 定义了与 Umeng SDK 的接口：

```
language = Objective-C
headers = UMConfigure.h MobClick.h
linkerOpts = -framework UMCommon
```

#### 配置说明

在 `build.gradle.kts` 中配置了 cinterop：

```kotlin
iosTargets().forEach { iosTarget ->
    iosTarget.compilations.getByName("main") {
        if (project.isChannelUmeng()) {
            cinterops {
                val umeng by creating {
                    defFile(project.file("src/iosUmengMain/cinterop/.../umeng.def"))
                    packageName("tech.zhifu.app.myhub.analytics.provider.umeng")
                    // ... 框架路径配置
                }
            }
        }
    }
}
```

#### IDE 索引问题

如果 IDE 显示 "Unresolved reference 'umeng'" 但可以正常编译：

1. **刷新 Gradle 项目**：在 Gradle 工具窗口点击刷新按钮
2. **重新构建**：`./gradlew clean :core:analytics:build -PappChannel=umeng`
3. **清理 IDE 缓存**：File > Invalidate Caches / Restart...

详细解决方案请参考：[IDE CInterop 索引问题](../../docs/IDE_CINTEROP_INDEXING.md)

#### CInterop Commonization

项目已启用 cinterop commonization，所有 iOS 架构共享同一个 cinterop 绑定：

```properties
# gradle.properties
kotlin.mpp.enableCInteropCommonization=true
```

详细说明请参考：[CInterop Commonization 说明](../../docs/CINTEROP_COMMONIZATION.md)

## 📋 项目进度

### ✅ 已完成事项

#### 阶段一：核心框架搭建 ✅

- ✅ 创建 `core:analytics` 模块
- ✅ 定义核心接口（AnalyticsService、AnalyticsProvider）
- ✅ 实现 AnalyticsManager
- ✅ 实现 AnalyticsConfig 和配置管理
- ✅ 创建 Koin DI 模块
- ✅ 实现 ConsoleProvider（所有平台默认支持）
- ✅ 实现 FileProvider（用于 Desktop QA）
- ✅ 编写单元测试（30+ 测试用例，100% 通过率）
- ✅ 平台特定的 Provider 注册器内置（expect/actual 机制）
- ✅ 所有平台默认注册 ConsoleProvider（Android、iOS、JVM、JS、WASM）

#### 阶段二：渠道框架实现 ✅

- ✅ 渠道框架设计（平台 + 渠道双维度变体系统）
- ✅ 源集注入机制（通过构建变体动态注入）
- ✅ 渠道互斥性保证（编译时互斥）
- ✅ 平台特定注册器（expect/actual + 渠道变体）

#### 阶段三：Firebase 集成 ✅

- ✅ Firebase Android SDK 集成
- ✅ Firebase iOS SDK 集成
- ✅ Firebase Web SDK 集成（JS/WASM）
- ✅ FirebaseProvider 实现（Android、iOS、Web）
- ✅ 自动配置（google-services.json、GoogleService-Info.plist）

#### 阶段四：Umeng 集成 ✅

- ✅ Umeng Android SDK 集成
- ✅ Umeng iOS SDK 集成（cinterop）
- ✅ UmengProvider 实现（Android、iOS）
- ✅ iOS cinterop 配置和优化
- ✅ CInterop Commonization 启用

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

#### 阶段五：集成和优化（进行中）

- ✅ 集成到应用
  - ✅ 在 composeApp 中集成
  - ✅ 平台特定注册器内置到 core:analytics 模块
  - ⏳ 在各个 Feature 模块中添加统计埋点

- ⏳ 性能优化
  - ⏳ 异步上报优化
  - ⏳ 批量上报支持
  - ⏳ 网络异常处理

- ⏳ 文档完善
  - ✅ 使用文档（本 README）
  - ✅ 渠道框架设计文档
  - ✅ CInterop 相关文档
  - ⏳ API 文档
  - ⏳ 最佳实践指南

## 🔗 相关模块

- `core:platform` - 平台抽象模块
- `core:logger` - 日志模块
- `core:app-build-config` - 应用构建配置模块

## 📚 详细文档

- [统计框架设计方案](../../docs/ANALYTICS_FRAMEWORK_DESIGN.md) - 完整的设计方案文档（v1.0，已锁定）
- [渠道框架设计文档](docs/CHANNEL_FRAMEWORK_DESIGN.md) - 渠道变体系统详细设计
- [测试总结](docs/TEST_SUMMARY.md) - 单元测试总结和最佳实践
- [CInterop Commonization 说明](../../docs/CINTEROP_COMMONIZATION.md) - iOS cinterop 优化说明
- [IDE CInterop 索引问题](../../docs/IDE_CINTEROP_INDEXING.md) - IDE 索引问题解决方案

## 🛠️ 开发指南

### 添加新的 Provider

1. **实现 Provider 接口**：
   ```kotlin
   class MyProvider : BaseAnalyticsProvider() {
       override suspend fun initialize(config: ProviderConfig) { ... }
       override fun doLogEvent(event: AnalyticsEvent) { ... }
       // ... 其他方法
   }
   ```

2. **注册 Provider**：
   - 在平台特定的注册器中注册
   - 例如：`androidMain`、`iosMain`、`jsMain` 等

3. **配置 Provider**：
   - 在 `AnalyticsConfig` 中添加 `ProviderConfig`
   - 或在平台特定的 `analyticsPlatformModule()` 中配置

### 平台特定实现

- **Android**：`src/androidMain/` 或 `src/android{Channel}Main/`
- **iOS**：`src/iosMain/` 或 `src/ios{Channel}Main/`
- **Web**：`src/jsMain/` 或 `src/js{Channel}Main/`
- **Desktop**：`src/jvmMain/`

### 渠道特定实现

- **Google Play**：`src/{platform}GooglePlayMain/`
- **Umeng**：`src/{platform}UmengMain/`
- **默认渠道**：`src/{platform}ChannelMain/`

---

## 📝 更新日志

### v2.0 (当前版本)

- ✅ 实现渠道框架（平台 + 渠道双维度变体系统）
- ✅ 集成 Firebase Analytics（Android、iOS、Web）
- ✅ 集成 Umeng Analytics（Android、iOS）
- ✅ iOS cinterop 配置和优化
- ✅ 启用 CInterop Commonization

### v1.0

- ✅ 核心框架搭建
- ✅ ConsoleProvider 和 FileProvider 实现
- ✅ 基础功能完善
