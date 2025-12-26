# 构建变体配置说明

## 📋 概述

项目支持两种维度的构建变体：
1. **环境维度**：开发环境（dev） / 生产环境（prod）
2. **版本维度**：免费版（free） / 付费版（premium）

组合后共有 **4 种变体**：
- `devFree` - 开发环境 + 免费版
- `devPremium` - 开发环境 + 付费版
- `prodFree` - 生产环境 + 免费版
- `prodPremium` - 生产环境 + 付费版

## 🏗️ 架构设计

### 1. 跨平台变体支持（Source Sets）

使用 Kotlin Multiplatform 的 Source Sets 机制实现跨平台变体：

```
composeApp/src/
├── commonMain/          # 公共代码
├── devFreeMain/         # 开发+免费变体公共代码
├── devPremiumMain/      # 开发+付费变体公共代码
├── prodFreeMain/        # 生产+免费变体公共代码
├── prodPremiumMain/     # 生产+付费变体公共代码
├── androidMain/         # Android 平台公共代码
├── androidDevFreeMain/  # Android 开发+免费变体
├── androidDevPremiumMain/ # Android 开发+付费变体
├── androidProdFreeMain/ # Android 生产+免费变体
├── androidProdPremiumMain/ # Android 生产+付费变体
├── iosMain/             # iOS 平台公共代码
├── iosDevFreeMain/      # iOS 开发+免费变体
├── iosDevPremiumMain/   # iOS 开发+付费变体
├── iosProdFreeMain/     # iOS 生产+免费变体
├── iosProdPremiumMain/  # iOS 生产+付费变体
├── jvmMain/             # JVM 平台公共代码
└── jsMain/              # JS 平台公共代码
```

### 2. Android 平台变体支持（Product Flavors）

Android 平台使用 `productFlavors` 实现变体：

```kotlin
flavorDimensions += "environment"
flavorDimensions += "version"

productFlavors {
    create("dev") { dimension = "environment" }
    create("prod") { dimension = "environment" }
    create("free") { dimension = "version" }
    create("premium") { dimension = "version" }
}
```

## 🔧 配置说明

### AppBuildConfig

使用 `expect`/`actual` 模式定义变体配置：

```kotlin
// commonMain
expect object AppBuildConfig {
    val environment: BuildEnvironment
    val versionType: VersionType
    val apiBaseUrl: String
    val appName: String
    // ...
}

// devFreeMain
actual object AppBuildConfig {
    actual val environment = BuildEnvironment.DEVELOPMENT
    actual val versionType = VersionType.FREE
    actual val apiBaseUrl = "https://dev-api.myhub.app"
    // ...
}
```

## 🚀 使用方法

### Android 平台

#### 构建特定变体

```bash
# 开发环境 + 免费版
./gradlew :androidApp:assembleDevFreeDebug
./gradlew :androidApp:assembleDevFreeRelease

# 开发环境 + 付费版
./gradlew :androidApp:assembleDevPremiumDebug
./gradlew :androidApp:assembleDevPremiumRelease

# 生产环境 + 免费版
./gradlew :androidApp:assembleProdFreeDebug
./gradlew :androidApp:assembleProdFreeRelease

# 生产环境 + 付费版
./gradlew :androidApp:assembleProdPremiumDebug
./gradlew :androidApp:assembleProdPremiumRelease
```

#### 安装到设备

```bash
./gradlew :androidApp:installDevFreeDebug
./gradlew :androidApp:installProdPremiumRelease
```

### 在代码中使用

```kotlin
import tech.zhifu.app.myhub.config.AppBuildConfig
import tech.zhifu.app.myhub.config.BuildConfigUsage

// 获取 API URL
val apiUrl = AppBuildConfig.apiBaseUrl

// 检查环境
if (AppBuildConfig.environment == BuildEnvironment.DEVELOPMENT) {
    // 开发环境特定逻辑
}

// 检查版本类型
if (AppBuildConfig.versionType == VersionType.PREMIUM) {
    // 付费版特定功能
}

// 使用辅助函数
val description = BuildConfigUsage.getEnvironmentDescription()
```

## 📦 变体配置详情

### devFree（开发环境 + 免费版）
- **API URL**: `https://dev-api.myhub.app`
- **应用名称**: `MyHub Dev (Free)`
- **应用 ID 后缀**: `.dev.free`
- **日志**: 启用
- **调试功能**: 启用

### devPremium（开发环境 + 付费版）
- **API URL**: `https://dev-api.myhub.app`
- **应用名称**: `MyHub Dev (Premium)`
- **应用 ID 后缀**: `.dev.premium`
- **日志**: 启用
- **调试功能**: 启用

### prodFree（生产环境 + 免费版）
- **API URL**: `https://api.myhub.app`
- **应用名称**: `MyHub (Free)`
- **应用 ID 后缀**: `.free`
- **日志**: 禁用
- **调试功能**: 禁用

### prodPremium（生产环境 + 付费版）
- **API URL**: `https://api.myhub.app`
- **应用名称**: `MyHub Premium`
- **应用 ID 后缀**: `.premium`
- **日志**: 禁用
- **调试功能**: 禁用

## 🔍 验证变体

### 检查当前变体

在应用启动时打印配置信息：

```kotlin
fun main() {
    println("Environment: ${AppBuildConfig.environment}")
    println("Version: ${AppBuildConfig.versionType}")
    println("API URL: ${AppBuildConfig.apiBaseUrl}")
    println("App Name: ${AppBuildConfig.appName}")
}
```

### Android 清单文件

Android 变体会自动生成不同的应用 ID，可以在同一设备上同时安装多个变体进行测试。

## 📝 注意事项

1. **Source Sets 优先级**：变体特定的 source sets 会覆盖 commonMain 中的代码
2. **依赖管理**：变体特定的依赖需要在对应的 source sets 中声明
3. **资源文件**：变体特定的资源文件可以放在对应的 source sets 目录中
4. **测试**：每个变体都可以有独立的测试代码

## 🎯 最佳实践

1. **使用 expect/actual**：对于变体特定的配置，使用 expect/actual 模式
2. **环境变量**：敏感信息（如 API Key）可以通过环境变量或构建配置注入
3. **功能开关**：使用 `AppBuildConfig.enableDebugFeatures` 控制调试功能的显示
4. **日志管理**：根据 `AppBuildConfig.enableLogging` 控制日志输出

