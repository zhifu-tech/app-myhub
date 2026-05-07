# MyHub

一个基于 Kotlin Multiplatform 和 Compose Multiplatform 的跨平台应用，支持 Android、iOS、Desktop、Web 等多个平台。

当前工程核心业务是 `AI Capture`：把文本、图片、视频和推理过程整理成可发布草稿，并支持复核、发布、本地恢复和多端同步。

## 📱 支持的平台

- **Android** - 原生 Android 应用
- **iOS** - 原生 iOS 应用
- **Desktop** - JVM 桌面应用（支持 Windows、macOS、Linux）
- **Web** - 浏览器应用（支持 WASM）

## 🛠️ 技术栈

### 核心框架

- **Kotlin Multiplatform** - 跨平台开发框架
- **Compose Multiplatform** - 声明式 UI 框架
- **Ktor** - 异步 Web 框架（用于服务器模块和网络请求）
- **SQLDelight** - 类型安全的 SQL 查询构建器（本地存储）
- **Koin** - 轻量级依赖注入框架
- **Gradle** - 构建工具
- **FileKit** - 跨平台文件目录与媒体落盘支持

### 其他技术

- **kotlinx.serialization** - 序列化库
- **kotlinx.datetime** - 跨平台日期时间处理
- **kotlin-logging** - 日志库
- **Compose Resources** - 多语言资源支持

## ✨ 当前能力

`feature/ai` 是当前工程的主功能模块，负责 AI Capture 的完整交互链路。

更详细的功能范围、交互说明和存储约束，请见 [feature/ai/README.md](feature/ai/README.md)。

当前 UI 已针对高频消息更新做了轻量动画和刷新合并，目标是保持流畅并尽量避免闪烁。

## 开发工具

1. **IntelliJ IDEA / Android Studio**
2. **Xcode**（iOS 或 macOS 上以 Designed for iPad 方式运行时需要）
3. 建议使用支持当前 Gradle / Kotlin Multiplatform 组合的较新版本 IDE

## 📦 项目结构

```shell
app-myhub/
├── androidApp/          # Android 原生应用配置
├── build-logic/         # 构建逻辑模块（Gradle 插件和配置）
├── composeApp/          # 主应用模块（Compose Multiplatform）
├── component/           # 组件模块
│   ├── card-mixed/      # 混合卡片组件模块
│   ├── media/           # 媒体组件模块
│   └── static/          # 静态占位组件模块
├── core/                # 核心模块套件
│   ├── analytics/       # 统一统计框架模块
│   ├── app-build-config/# 应用构建配置模块
│   ├── cache/           # 缓存模块
│   ├── logger/          # 日志模块
│   ├── navigation/      # 导航模块
│   ├── network/         # 网络模块
│   ├── network-test/    # 网络测试模块
│   ├── platform/        # 平台抽象模块
│   ├── saving/          # 保存能力模块
│   ├── settings/        # 全局设置模块
│   ├── sharing/         # 分享能力模块
│   └── startup/         # 启动与初始化模块
├── datastore/           # 数据层模块套件
│   ├── bootstrap/       # 初始化与引导模块
│   ├── database/        # 数据库 Schema 定义
│   ├── database-client/ # 客户端数据库配置
│   ├── database-server/ # 服务端数据库配置
│   ├── database-test/   # 数据库测试模块
│   ├── datasource-local/# 本地数据源实现
│   ├── datasource-remote/# 远程数据源实现
│   ├── file-storage/    # 文件存储模块
│   ├── model/           # 数据模型定义
│   ├── model-dto/       # 数据传输模型定义
│   ├── operations/      # 数据操作模块
│   ├── repository-client-api/  # 客户端 Repository 接口
│   ├── repository-client/      # 客户端 Repository 实现
│   ├── repository-server-api/   # 服务端 Repository 接口
│   └── sync/            # 同步模块
├── docs/                # 仓库级文档与规范
├── feature/             # 功能模块
│   ├── ai/              # AI Capture 主功能模块
│   ├── auth/            # 认证与登录模块
│   ├── dashboard/       # 仪表板功能模块
│   ├── dashboard-api/   # 仪表板 API 模块
│   ├── mixed/           # 混合功能模块
│   ├── mixed-api/       # 混合功能 API 模块
│   ├── preview/         # 预览功能模块
│   ├── settings/        # 设置功能模块
│   └── settings-api/    # 设置 API 模块
├── iosApp/              # iOS 原生应用配置
├── server/              # 服务器模块（Ktor）
├── ui/                  # UI 基础模块
│   ├── design/          # 设计系统与平台 UI 适配
│   └── state/           # UI 状态管理模块
└── webApp/              # Web 运行时配置与资源
```

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Android Studio** 或 **IntelliJ IDEA**
- **Xcode**（仅 iOS 开发需要）
- **Gradle 8.x / 9.x**

### 构建项目

```bash
# 构建所有平台
./gradlew build

# 构建特定平台
./gradlew :composeApp:assembleDebug          # Android
./gradlew :composeApp:packageReleaseDistributionForCurrentOS  # Desktop release installer
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Web
```

### 运行应用

#### 使用统一管理脚本（推荐）

项目提供了统一管理脚本 `run.sh`，支持使用 `-P` 格式参数（与 Gradle 对齐）：

```bash
# 运行桌面应用（使用默认值：debug, dev, free）
./run.sh desktop

# 生成桌面安装包（release 模式，生产环境，收费版，Google Play 渠道）
./run.sh desktop --release -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# 构建并安装 Android 应用
./run.sh android -PappEnv=prod -PappTier=premium

# 运行 Web 应用（Wasm JS，默认：debug）
./run.sh web

# 运行 Web 应用（Wasm JS，release 模式）
./run.sh web --release

# 运行 Web 应用（WebAssembly，默认：debug）
./run.sh wasmJs

# 运行 Web 应用（WebAssembly，release 模式）
./run.sh wasmJs --release

# 构建并打开 iOS 项目
./run.sh ios

# 运行服务器
./run.sh server
```

**注意：** 脚本使用 `-P` 格式参数，与 Gradle 命令的参数格式完全一致。使用 `./run.sh --help` 查看完整帮助信息。

#### 直接使用 Gradle 命令

#### Android

```bash
./gradlew :androidApp:installDebug -PappEnv=dev -PappTier=free
```

#### Desktop

```bash
./gradlew :composeApp:runDistributable -PappEnv=dev -PappTier=free
```

#### Web

```bash
# WebAssembly 版本
./gradlew :composeApp:wasmJsBrowserDevelopmentRun -PappEnv=dev -PappTier=free
./gradlew :composeApp:wasmJsBrowserProductionRun -PappEnv=prod -PappTier=premium
```

#### iOS

```bash
# 使用项目脚本（推荐）
./run.sh ios

# 安装 CocoaPods 依赖
./run.sh pod install -PappChannel=umeng -PappEnv=dev

# 指定渠道和环境
./run.sh ios -PappChannel=googlePlay -PappEnv=prod
```

⚠️ **重要**：

- 必须使用 `iosApp/iosApp.xcworkspace` 打开项目（不是 `.xcodeproj`）
- 首次运行前需要执行 `pod install` 安装依赖
- 在 macOS 上以 Designed for iPad 方式运行时，应用数据会落在 macOS 沙盒容器里，常见路径是 `~/Library/Containers/<bundle-id>/Data/Library/Application Support/app-data`
  - 详细说明请参考 [iosApp/README.md](iosApp/README.md)
- 当前 AI Capture 页面在宽屏下会使用双 panel 布局：左侧 section rail，右侧主内容区；窄屏下回退为单 panel。

### 运行服务器

```bash
# 使用脚本
./run.sh server

# 或直接使用 Gradle
./gradlew :server:run
```

## 📝 开发说明

### 模块说明

#### 应用模块

- **composeApp**: 主应用模块，包含所有平台的 UI 代码（Android、iOS、Desktop、Web）
- **androidApp**: Android 原生应用配置和入口点
- **iosApp**: iOS 原生应用配置和入口点

#### 核心模块

**基础模块**

- **core:platform**: 平台抽象模块（平台检测、系统属性访问）
- **core:navigation**: 导航模块
- **core:logger**: 统一日志模块（基于 kotlin-logging）
- **core:network**: 网络模块（Ktor Client 封装）
- **core:network-test**: 网络测试模块
- **core:analytics**: 统一统计框架模块（支持多 Provider）
- **core:app-build-config**: 应用构建配置模块
- **core:cache**: 缓存模块
- **core:saving**: 保存能力模块
- **core:settings**: 全局设置模块
- **core:sharing**: 分享能力模块
- **core:startup**: 启动与初始化模块

#### UI 基础模块

- **ui:design**: 设计系统与平台 UI 适配模块
- **ui:state**: UI 状态管理模块

**数据层模块**

- **datastore:bootstrap**: 数据层初始化与引导模块
- **datastore:model**: 数据模型定义（Card、Tag、Template、User 等）
- **datastore:model-dto**: 数据传输模型定义
- **datastore:database**: SQLDelight 数据库 Schema 定义
- **datastore:database-client**: 客户端数据库配置和驱动工厂
- **datastore:database-server**: 服务端数据库配置和驱动工厂
- **datastore:database-test**: 数据库测试模块
- **datastore:datasource-local**: 本地数据源实现（SQLDelight）
- **datastore:datasource-remote**: 远程数据源实现（Ktor Client）
- **datastore:file-storage**: 文件存储模块
- **datastore:operations**: 数据操作模块
- **datastore:repository-client-api**: 客户端 Repository 接口
- **datastore:repository-client**: 客户端 Repository 实现（协调本地和远程数据源）
- **datastore:repository-server-api**: 服务端 Repository 接口
- **datastore:sync**: 同步模块

#### 组件模块

- **component:card-mixed**: 混合卡片组件模块（支持多种卡片展示形态）
- **component:media**: 媒体组件模块
- **component:static**: 静态占位组件模块

#### 功能模块

- **feature:ai**: AI Capture 主功能模块，包含消息流、推理展示、媒体管理、发布和本地恢复
- **feature:auth**: 认证与登录功能模块
- **feature:dashboard**: 仪表板功能模块
- **feature:dashboard-api**: 仪表板 API 模块
- **feature:mixed**: 混合功能模块
- **feature:mixed-api**: 混合功能 API 模块
- **feature:preview**: 预览功能模块
- **feature:settings**: 设置功能模块
- **feature:settings-api**: 设置 API 模块

#### 服务器模块

- **server**: Ktor 服务器应用，提供 RESTful API 服务

更多关于数据层模块的详细信息，请参考 [datastore/README.md](datastore/README.md)。

### 📚 文档

- **[架构设计文档](docs/infra/myhub_architecture.md)** - MyHub 整体架构设计
- **[文档总览](docs/README.md)** - 仓库文档入口与整理规则
- **[FAQ](docs/faq/README.md)** - 常见问题解答

#### 模块文档

**核心模块**

- [core:platform 模块](core/platform/README.md) - 平台抽象模块说明
- [core:logger 模块](core/logger/README.md) - 日志模块说明
- [core:analytics 模块](core/analytics/README.md) - 统计框架模块说明
- [core:navigation 模块](core/navigation/README.md) - 导航模块说明
- [datastore 模块套件](datastore/README.md) - 数据层模块套件概述
- [datastore 架构设计](datastore/docs/myhub-datastore-architecture.md) - 详细的数据模型架构设计

**组件模块**

- [component:card-mixed 模块](component/card-mixed/README.md) - 卡片组件模块说明
- [component:media 模块](component/media/README.md) - 媒体组件模块说明
- [component:static 模块](component/static/README.md) - 静态占位组件模块说明

**UI 基础模块**

- [ui:design 模块](ui/design/README.md) - 设计系统与平台 UI 适配说明
- [ui:state 模块](ui/state/README.md) - UI 状态管理说明

**功能模块**

- [feature:ai 模块](feature/ai/README.md) - AI Capture 功能模块说明
- [feature:auth 模块](feature/auth/README.md) - 认证与登录功能模块说明
- [feature:dashboard 模块](feature/dashboard/README.md) - 仪表板功能模块说明
- [feature:settings 模块](feature/settings/README.md) - 设置功能模块说明

其他功能模块请直接查看对应目录下的模块入口或源码目录。

**服务器模块**

- [server 模块](server/README.md) - 服务器模块说明和快速开始

#### 其他文档

- [构建变体说明](build-logic/docs/BUILD_VARIANTS.md) - 构建变体配置说明
- [统计框架设计](core/analytics/docs/myhub-analytics-framework-design.md) - 统计框架设计方案

## 📄 许可证

[待添加]

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
