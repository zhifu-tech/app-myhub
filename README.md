# MyHub

一个基于 Kotlin Multiplatform 和 Compose Multiplatform 的跨平台应用，支持 Android、iOS、Desktop、Web 等多个平台。

[//]: # (https://stitch.withgoogle.com/projects/3621022472859340515)

## 📱 支持的平台

- **Android** - 原生 Android 应用
- **iOS** - 原生 iOS 应用
- **Desktop** - JVM 桌面应用（支持 Windows、macOS、Linux）
- **Web** - 浏览器应用（支持 JS 和 WASM）

## 🛠️ 技术栈

- **Kotlin Multiplatform** - 跨平台开发框架
- **Compose Multiplatform** - 声明式 UI 框架
- **Ktor** - 异步 Web 框架（用于服务器模块和网络请求）
- **SQLDelight** - 类型安全的 SQL 查询构建器（本地存储）
- **Koin** - 轻量级依赖注入框架
- **Gradle** - 构建工具

## 📦 项目结构

```text
app-myhub/
├── composeApp/          # 主应用模块（Compose Multiplatform）
├── androidApp/          # Android 原生应用配置
├── server/              # 服务器模块（Ktor）
├── iosApp/              # iOS 原生应用配置
└── core/                # 核心模块套件
    ├── datastore-*      # 数据层模块（模型、数据库、数据源、仓库等）
    ├── local/           # 本地存储模块
    └── platform/        # 平台抽象模块
```

## 🚀 快速开始

### 环境要求

- **JDK 11+**
- **Android Studio** 或 **IntelliJ IDEA**
- **Xcode**（仅 iOS 开发需要）
- **Gradle 8.0+**

### 构建项目

```bash
# 构建所有平台
./gradlew build

# 构建特定平台
./gradlew :composeApp:assembleDebug          # Android
./gradlew :composeApp:packageDistributionForCurrentOS  # Desktop
./gradlew :composeApp:jsBrowserDevelopmentRun  # Web
```

### 运行应用

#### Android

```bash
./gradlew :composeApp:installDebug
```

#### Desktop

```bash
./gradlew :composeApp:runDistributable
```

#### Web

```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

#### iOS

在 Xcode 中打开 `iosApp/iosApp.xcodeproj` 并运行。

### 运行服务器

```bash
./gradlew :server:run
```

## 📝 开发说明

### 模块说明

#### 应用模块

- **composeApp**: 主应用模块，包含所有平台的 UI 代码（Android、iOS、Desktop、Web）
- **androidApp**: Android 原生应用配置和入口点
- **iosApp**: iOS 原生应用配置和入口点

#### 核心模块

- **core:datastore-model**: 数据模型定义（Card、Tag、Template、User 等）
- **core:datastore-database**: SQLDelight 数据库 Schema 定义
- **core:datastore-database-client**: 客户端数据库配置和驱动工厂
- **core:datastore-database-server**: 服务端数据库配置和驱动工厂
- **core:datastore-datasource-local**: 本地数据源实现（SQLDelight）
- **core:datastore-datasource-remote**: 远程数据源实现（Ktor Client）
- **core:datastore-repository**: Repository 接口定义
- **core:datastore-repository-client**: 客户端 Repository 实现（协调本地和远程数据源）
- **core:datastore-repository-server**: 服务端 Repository 实现（仅使用本地数据源）
- **core:local**: 本地文件存储模块
- **core:platform**: 平台抽象模块（常量定义、平台接口）

#### 服务器模块

- **server**: Ktor 服务器应用，提供 RESTful API 服务

更多关于数据层模块的详细信息，请参考 [core/datastore/README.md](core/datastore/README.md)。

### 📚 文档

- **[架构设计文档](docs/myhub_architecture.md)** - MyHub 整体架构设计
- **[待办事项](docs/myhub_todos.md)** - 项目整体待办事项
- **[FAQ](docs/FAQ.md)** - 常见问题解答

#### 模块文档

- [core:datastore 模块套件](core/datastore/README.md) - 数据层模块套件概述
- [core:datastore 架构设计](core/datastore/docs/datastore_architecture.md) - 详细的数据模型架构设计
- [core:local 模块](core/local/README.md) - 本地存储模块说明
- [server 模块](server/README.md) - 服务器模块说明和快速开始

#### 其他文档

- [构建变体说明](docs/BUILD_VARIANTS.md) - 构建变体配置说明
- [构建变体快速开始](docs/BUILD_VARIANTS_QUICK_START.md) - 构建变体快速开始指南
- [数据模块迁移](docs/DATA_MODULE_MIGRATION.md) - 数据模块迁移指南
- [Dashboard 迁移](DASHBOARD_MIGRATION.md) - Dashboard 模块迁移说明

## 📄 许可证

[待添加]

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
