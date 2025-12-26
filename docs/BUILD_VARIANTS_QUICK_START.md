# 构建变体快速开始指南

## ✅ 已配置的变体

项目现在支持 **4 种构建变体**：

| 变体 | 环境 | 版本 | 应用 ID | API URL |
|------|------|------|---------|---------|
| `devFree` | 开发 | 免费 | `tech.zhifu.app.myhub.dev.free` | `https://dev-api.myhub.app` |
| `devPremium` | 开发 | 付费 | `tech.zhifu.app.myhub.dev.premium` | `https://dev-api.myhub.app` |
| `prodFree` | 生产 | 免费 | `tech.zhifu.app.myhub.free` | `https://api.myhub.app` |
| `prodPremium` | 生产 | 付费 | `tech.zhifu.app.myhub.premium` | `https://api.myhub.app` |

## 🚀 快速开始

### Android 平台

#### 构建所有变体
```bash
./gradlew :androidApp:assemble
```

#### 构建特定变体
```bash
# 开发环境 + 免费版 Debug
./gradlew :androidApp:assembleDevFreeDebug

# 开发环境 + 付费版 Debug
./gradlew :androidApp:assembleDevPremiumDebug

# 生产环境 + 免费版 Release
./gradlew :androidApp:assembleProdFreeRelease

# 生产环境 + 付费版 Release
./gradlew :androidApp:assembleProdPremiumRelease
```

#### 安装到设备
```bash
# 安装开发环境 + 免费版
./gradlew :androidApp:installDevFreeDebug

# 安装生产环境 + 付费版
./gradlew :androidApp:installProdPremiumRelease
```

### 查看所有可用任务
```bash
./gradlew :androidApp:tasks --all | grep -i "assemble\|install"
```

## 📱 效果展示

### 在应用中查看变体信息

1. **应用标题栏**：会根据变体显示不同的应用名称
   - `devFree`: "MyHub Dev (Free)"
   - `devPremium`: "MyHub Dev (Premium)"
   - `prodFree`: "MyHub (Free)"
   - `prodPremium`: "MyHub Premium"

2. **开发环境**：在开发环境变体中，标题栏下方会显示变体描述
   - "开发环境 - 免费版"
   - "开发环境 - 付费版"

3. **控制台日志**：开发环境启动时会打印完整的配置信息

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

// 获取环境描述
val description = BuildConfigUsage.getEnvironmentDescription()
```

## 🔍 验证变体

### 方法 1：查看应用名称
运行应用后，查看标题栏中的应用名称，应该显示对应变体的名称。

### 方法 2：查看控制台日志
开发环境变体启动时会在控制台打印配置信息：
```
=== App Build Config ===
Environment: DEVELOPMENT
Version Type: FREE
API Base URL: https://dev-api.myhub.app
App Name: MyHub Dev (Free)
Application ID Suffix: .dev.free
Enable Logging: true
Enable Debug Features: true
========================
```

### 方法 3：检查应用 ID
在 Android 设备上，可以通过以下方式检查：
```bash
adb shell pm list packages | grep myhub
```

应该能看到不同的应用 ID，例如：
- `tech.zhifu.app.myhub.dev.free`
- `tech.zhifu.app.myhub.dev.premium`
- `tech.zhifu.app.myhub.free`
- `tech.zhifu.app.myhub.premium`

## 📝 注意事项

1. **Source Sets 映射**：变体特定的代码需要放在对应的 source sets 目录中
2. **默认变体**：如果没有指定，Gradle 会使用默认配置
3. **同时安装**：不同变体可以同时安装在同一个设备上（因为应用 ID 不同）

## 🎯 下一步

1. 在变体特定的 source sets 中添加变体特定的代码
2. 根据变体配置 API 端点和功能开关
3. 为不同变体配置不同的资源文件（图标、字符串等）

