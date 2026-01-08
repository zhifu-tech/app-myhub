# MyHub

一个基于 Kotlin Multiplatform 和 Compose Multiplatform 的跨平台应用，支持 Android、iOS、Desktop、Web 等多个平台。

[//]: # "https://stitch.withgoogle.com/projects/3621022472859340515"

## 📱 支持的平台

- **Android** - 原生 Android 应用
- **iOS** - 原生 iOS 应用
- **Desktop** - JVM 桌面应用（支持 Windows、macOS、Linux）
- **Web** - 浏览器应用（支持 JS 和 WASM）

## 🛠️ 技术栈

### 核心框架

- **Kotlin Multiplatform** - 跨平台开发框架
- **Compose Multiplatform** - 声明式 UI 框架
- **Ktor** - 异步 Web 框架（用于服务器模块和网络请求）
- **SQLDelight** - 类型安全的 SQL 查询构建器（本地存储）
- **Koin** - 轻量级依赖注入框架
- **Gradle** - 构建工具

### 其他技术

- **kotlinx.serialization** - 序列化库
- **kotlinx.datetime** - 跨平台日期时间处理
- **kotlin-logging** - 日志库
- **Compose Resources** - 多语言资源支持

## 开发工具

1. **Jetbrain's Idea IntelliJ IDEA 2025.3.1 及以后的版本**
   （因为用到了不兼容的 apg 要求 gradle 9.1.0 以上，kotlin multiplatform 有依赖，AS 尚无该版本的插件）


## 📦 项目结构

```shell
app-myhub/
├── composeApp/          # 主应用模块（Compose Multiplatform）
├── androidApp/          # Android 原生应用配置
├── iosApp/              # iOS 原生应用配置
├── server/              # 服务器模块（Ktor）
├── build-logic/         # 构建逻辑模块（Gradle 插件和配置）
├── core/                # 核心模块套件
│   ├── analytics/       # 统一统计框架模块
│   ├── logger/          # 日志模块
│   ├── network/         # 网络模块
│   ├── platform/        # 平台抽象模块
│   ├── platform-compose/# 平台 Compose 支持模块
│   ├── app-build-config/# 应用构建配置模块
│   └── datastore-*      # 数据层模块套件
│       ├── datastore-model/              # 数据模型定义
│       ├── datastore-database/           # 数据库 Schema 定义
│       ├── datastore-database-client/    # 客户端数据库配置
│       ├── datastore-database-server/    # 服务端数据库配置
│       ├── datastore-datasource-local/   # 本地数据源实现
│       ├── datastore-datasource-remote/  # 远程数据源实现
│       ├── datastore-repository/         # Repository 接口定义
│       ├── datastore-repository-client/  # 客户端 Repository 实现
│       └── datastore-repository-server/ # 服务端 Repository 实现
├── component/           # 组件模块
│   ├── card/            # 卡片组件模块
│   └── mixed/           # 混合组件模块
└── feature/             # 功能模块
    ├── dashboard/       # 仪表板功能模块
    ├── profile/         # 个人资料功能模块
    └── settings/        # 设置功能模块
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

#### 使用统一管理脚本（推荐）

项目提供了统一管理脚本 `scripts/run.sh`，支持使用 `-P` 格式参数（与 Gradle 对齐）：

```bash
# 运行桌面应用（使用默认值：debug, dev, free）
./scripts/run.sh desktop

# 运行桌面应用（release 模式，生产环境，收费版，Google Play 渠道）
./scripts/run.sh desktop -PbuildType=release -PappEnv=prod -PappTier=premium -PappChannel=googlePlay

# 构建并安装 Android 应用
./scripts/run.sh android -PappEnv=prod -PappTier=premium

# 运行 Web 应用
./scripts/run.sh web

# 构建并打开 iOS 项目
./scripts/run.sh ios

# 运行服务器
./scripts/run.sh server
```

**注意：** 脚本使用 `-P` 格式参数，与 Gradle 命令的参数格式完全一致。使用 `./scripts/run.sh --help` 查看完整帮助信息。

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
./gradlew :composeApp:jsBrowserDevelopmentRun -PappEnv=dev -PappTier=free
```

#### iOS

在 Xcode 中打开 `iosApp/iosApp.xcodeproj` 并运行。

### 运行服务器

```bash
# 使用脚本
./scripts/run.sh server

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
- **core:platform-compose**: 平台 Compose 支持模块
- **core:logger**: 统一日志模块（基于 kotlin-logging）
- **core:network**: 网络模块（Ktor Client 封装）
- **core:analytics**: 统一统计框架模块（支持多 Provider）
- **core:app-build-config**: 应用构建配置模块

**数据层模块**

- **core:datastore-model**: 数据模型定义（Card、Tag、Template、User 等）
- **core:datastore-database**: SQLDelight 数据库 Schema 定义
- **core:datastore-database-client**: 客户端数据库配置和驱动工厂
- **core:datastore-database-server**: 服务端数据库配置和驱动工厂
- **core:datastore-datasource-local**: 本地数据源实现（SQLDelight）
- **core:datastore-datasource-remote**: 远程数据源实现（Ktor Client）
- **core:datastore-repository**: Repository 接口定义
- **core:datastore-repository-client**: 客户端 Repository 实现（协调本地和远程数据源）
- **core:datastore-repository-server**: 服务端 Repository 实现（仅使用本地数据源）

#### 组件模块

- **component:card**: 卡片组件模块（支持 6 种卡片类型：Quote、Code、Idea、Article、Dictionary、Checklist）
- **component:mixed**: 混合组件模块

#### 功能模块

- **feature:dashboard**: 仪表板功能模块
- **feature:profile**: 个人资料功能模块
- **feature:settings**: 设置功能模块

#### 服务器模块

- **server**: Ktor 服务器应用，提供 RESTful API 服务

更多关于数据层模块的详细信息，请参考 [core/datastore/README.md](core/datastore/README.md)。

### 📚 文档

- **[架构设计文档](docs/myhub_architecture.md)** - MyHub 整体架构设计
- **[待办事项](docs/myhub_todos.md)** - 项目整体待办事项
- **[FAQ](docs/FAQ.md)** - 常见问题解答

#### 模块文档

**核心模块**

- [core:platform 模块](core/platform/README.md) - 平台抽象模块说明
- [core:logger 模块](core/logger/README.md) - 日志模块说明
- [core:analytics 模块](core/analytics/README.md) - 统计框架模块说明
- [core:datastore 模块套件](core/datastore/README.md) - 数据层模块套件概述
- [core:datastore 架构设计](core/datastore/docs/datastore_architecture.md) - 详细的数据模型架构设计

**组件模块**

- [component:card 模块](component/card/README.md) - 卡片组件模块架构设计

**功能模块**

- [feature:dashboard 模块](feature/dashboard/README.md) - 仪表板功能模块说明
- [feature:profile 模块](feature/profile/README.md) - 个人资料功能模块说明

**服务器模块**

- [server 模块](server/README.md) - 服务器模块说明和快速开始

#### 其他文档

- [构建变体说明](docs/BUILD_VARIANTS.md) - 构建变体配置说明
- [数据模块迁移](docs/DATA_MODULE_MIGRATION.md) - 数据模块迁移指南
- [多语言支持](docs/I18N_MULTILINGUAL_SUPPORT.md) - 多语言支持说明
- [统计框架设计](docs/ANALYTICS_FRAMEWORK_DESIGN.md) - 统计框架设计方案
- [Dashboard 迁移](DASHBOARD_MIGRATION.md) - Dashboard 模块迁移说明

## 📄 许可证

[待添加]

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
