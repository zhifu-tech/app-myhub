# Core App Build Config Module

本模块用于**规范**和**实现** MyHub 应用的应用构建配置基础设施（App Build Config Infra），为各功能模块**提供统一、类型安全的构建变体配置能力**。它基于 **Kotlin Multiplatform Source Sets** 和**委托模式**，实现了**多维度变体支持**、**编译时确定配置值**、**类型安全访问**等特性，并提供了面向 KMP 场景的**统一配置接口**，方便在 **Android、iOS、JVM、JS、WebAssembly** 等多端项目中集成和使用。

## 核心组件

### 1. AppBuildConfig

统一的配置入口对象，使用委托模式组合各个维度的配置：

- **统一访问**：提供单一的配置对象，隐藏底层实现细节
- **类型安全**：所有配置属性都有明确的类型，编译期检查
- **编译时确定**：配置值在编译时确定，运行时零开销
- `appEnv`：获取当前环境维度（"dev" 或 "prod"）
- `appTier`：获取当前级别维度（"free" 或 "premium"）
- `appChannel`：获取当前渠道维度（"channel"、"googlePlay"、"umeng" 等）

### 2. AppBuildEnvConfig

环境维度配置接口，定义开发环境和生产环境的配置属性：

- **环境区分**：支持开发环境（dev）和生产环境（prod）
- **功能开关**：提供日志开关、调试功能开关等配置
- `appEnv`：当前环境标识
- `enableLogging`：是否启用日志
- `enableDebugFeatures`：是否启用调试功能

### 3. AppBuildTierConfig

级别维度配置接口，定义免费版和高级版的配置属性：

- **版本区分**：支持免费版（free）和高级版（premium）
- **功能控制**：根据版本级别控制功能可用性
- `appTier`：当前版本级别标识

### 4. AppBuildChannelConfig

渠道维度配置接口，定义不同分发渠道的配置属性：

- **渠道区分**：支持多个分发渠道（默认渠道、Google Play、友盟等）
- **渠道标识**：提供渠道标识用于统计和分析
- `appChannel`：当前渠道标识

## 使用示例

```kotlin
import tech.zhifu.app.myhub.config.AppBuildConfig

// 1. 获取变体维度
val env = AppBuildConfig.appEnv        // "dev" 或 "prod"
val tier = AppBuildConfig.appTier      // "free" 或 "premium"
val channel = AppBuildConfig.appChannel // "channel"、"googlePlay"、"umeng" 等

// 2. 获取配置属性
val appName = AppBuildConfig.APP_NAME
val enableLogging = AppBuildConfig.enableLogging
val enableDebugFeatures = AppBuildConfig.enableDebugFeatures

// 3. 根据环境配置功能
if (AppBuildConfig.appEnv == "dev") {
    // 开发环境特定逻辑
    println("Running in development mode")
}

// 4. 根据级别启用功能
if (AppBuildConfig.appTier == "premium") {
    // 高级版特定功能
    showPremiumFeatures()
} else {
    // 免费版功能
    showUpgradePrompt()
}

// 5. 根据渠道配置统计
when (AppBuildConfig.appChannel) {
    "googlePlay" -> {
        // Google Play 渠道配置
        initializeGooglePlayAnalytics()
    }
    "umeng" -> {
        // 友盟渠道配置
        initializeUmengAnalytics()
    }
    else -> {
        // 默认渠道配置
        initializeDefaultAnalytics()
    }
}

// 6. 使用配置控制日志
if (AppBuildConfig.enableLogging) {
    logger.info { "Application started" }
}
```

## 构建变体配置

### 方式 1：通过命令行参数设置（推荐用于临时构建）

```bash
# 开发环境 + 免费版
./gradlew build -PappEnv=dev -PappTier=free

# 生产环境 + 高级版 + Google Play 渠道
./gradlew build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

### 方式 2：通过 gradle.properties 文件设置（推荐用于默认配置）

在项目根目录的 `gradle.properties` 文件中配置：

```properties
# 构建变体配置
appEnv=dev
appTier=free
appChannel=channel
```

**配置优先级**（从高到低）：
1. 命令行参数（`-PappEnv=prod`）
2. `gradle.properties` 文件（项目级配置）
3. `~/.gradle/gradle.properties` 文件（用户级配置）
4. 代码中的默认值（`dev`、`free`、`channel`）

## 文档

- [MyHub 应用构建配置模块方案设计](./docs/myhub-app-build-config-infra-v1.0.md)
