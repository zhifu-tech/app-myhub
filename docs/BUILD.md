# MyHub 工程构建文档

## 构建系统

- **构建工具**: Gradle
- **构建语言**: Kotlin DSL (build.gradle.kts)
- **Kotlin 版本**: 2.3.0
- **AGP 版本**: 9.1.0-alpha03
- **Compose Multiplatform**: 1.9.3

## 支持的平台

### 1. Android

- **模块**: `androidApp`
- **最小 SDK**: 24
- **目标 SDK**: 36
- **编译 SDK**: 36

### 2. iOS

- **模块**: `composeApp` (Framework) + `iosApp` (Xcode 项目)
- **部署目标**: iOS 15.0+
- **构建方式**: CocoaPods + Xcode

### 3. Desktop (JVM)

- **模块**: `composeApp`
- **目标格式**: DMG (macOS), MSI (Windows), DEB (Linux)
- **主类**: `tech.zhifu.app.myhub.MainKt`

### 4. Web

- **模块**: `composeApp`
- **目标**:
  - JavaScript (JS)
  - WebAssembly (WASM)

### 5. Server (JVM)

- **模块**: `server`
- **主类**: `tech.zhifu.app.myhub.ApplicationKt`
- **框架**: Ktor

## 编译入口

### 根构建配置

- `build.gradle.kts` - 根构建脚本，声明插件
- `settings.gradle.kts` - 项目结构定义，包含所有模块

### 平台构建配置

- `composeApp/build.gradle.kts` - 多平台应用构建配置
- `androidApp/build.gradle.kts` - Android 应用构建配置
- `server/build.gradle.kts` - 服务器构建配置

### 统一管理脚本

- `scripts/run.sh` - 统一构建和运行脚本

## 构建命令

### 构建所有模块

```bash
./gradlew build
```

### Android

```bash
# 构建 Debug APK
./gradlew :androidApp:assembleDebug

# 安装到设备
./gradlew :androidApp:installDebug

# 使用脚本
./scripts/run.sh android
```

### Desktop

```bash
# 运行应用
./gradlew :composeApp:runDistributable

# 打包分发
./gradlew :composeApp:packageDistributionForCurrentOS

# 使用脚本
./scripts/run.sh desktop
```

### Web

```bash
# JavaScript 开发模式
./gradlew :composeApp:jsBrowserDevelopmentRun

# JavaScript 生产模式
./gradlew :composeApp:jsBrowserProductionRun

# WebAssembly 开发模式
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# WebAssembly 生产模式
./gradlew :composeApp:wasmJsBrowserProductionRun

# 使用脚本
./scripts/run.sh js
./scripts/run.sh wasmJs
```

### iOS

```bash
# 使用脚本（推荐）
./scripts/run.sh ios

# 安装 CocoaPods 依赖
./scripts/run.sh pod install
```

### Server

```bash
# 运行服务器
./gradlew :server:run

# 使用脚本
./scripts/run.sh server
```

## 平台按需加载

### 问题
一次性加载所有平台会导致 IDE 响应速度变慢。

### 解决方案
通过配置控制哪些平台被加载到工程中。

### 配置方式

#### 方式1: gradle.properties（推荐用于默认配置）

```properties
# 启用的平台（用逗号分隔）
enabledPlatforms=android
```

#### 方式2: 命令行参数（推荐用于临时切换）

```bash
# 只加载 Android 平台
./gradlew build -PenabledPlatforms=android

# 加载多个平台
./gradlew build -PenabledPlatforms=android,desktop

# 使用脚本
./scripts/run.sh android -PenabledPlatforms=android
```

### 平台标识

- `android` - Android 平台
- `ios` - iOS 平台
- `desktop` - Desktop (JVM) 平台
- `js` - JavaScript 平台
- `wasmJs` - WebAssembly 平台
- `server` - Server (JVM) 平台

### 注意事项

1. **核心模块始终加载**：所有 `core:*`、`feature:*`、`component:*` 模块始终加载
2. **默认行为**：如果未设置 `enabledPlatforms`，加载所有平台（保持向后兼容）
3. **IDE 同步**：修改配置后需要重新同步 Gradle 项目

详细说明请参考 [平台按需加载方案](PLATFORM_LOADING.md)

## 构建变体

### 变体参数

- `-PappEnv=dev|prod` - 环境（开发/生产）
- `-PappTier=free|premium` - 版本（免费/付费）
- `-PappChannel=googlePlay|umeng|...` - 渠道

### 示例

```bash
# 生产环境，付费版，Google Play 渠道
./gradlew build -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# 使用脚本
./scripts/run.sh desktop -PappEnv=prod -PappTier=premium -PappChannel=googlePlay
```

## 项目模块结构

### 应用模块

- `composeApp` - 多平台 UI 应用
- `androidApp` - Android 应用入口
- `server` - 服务器应用

### 核心模块

- `core:logger` - 日志
- `core:platform` - 平台抽象
- `core:platform-compose` - Compose 平台组件
- `core:navigation` - 导航
- `core:network` - 网络
- `core:analytics` - 统计
- `core:app-build-config` - 构建配置
- `core:datastore-*` - 数据存储模块套件

### 功能模块

- `feature:dashboard` - 仪表板
- `feature:profile` - 个人资料
- `feature:settings` - 设置
- `feature:card-detail` - 卡片详情

### 组件模块

- `component:card` - 卡片组件
- `component:mixed` - 混合组件
