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
- **Ktor** - 异步 Web 框架（用于服务器模块）
- **Gradle** - 构建工具
- **build-logic** - 集中式构建配置管理

## 📦 项目结构

```
app-myhub/
├── composeApp/          # 主应用模块（Compose Multiplatform）
├── shared/              # 共享代码模块
├── server/              # 服务器模块（Ktor）
├── build-logic/         # 构建逻辑和约定插件
└── iosApp/              # iOS 原生应用配置
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

### 构建逻辑

项目使用 `build-logic` 模块集中管理构建配置，遵循 Gradle 最佳实践。更多信息请参考 [build-logic/README.md](build-logic/README.md)。

### 模块说明

- **composeApp**: 主应用模块，包含所有平台的 UI 代码
- **core:datastore**: 数据层模块，负责数据存储和网络请求
- **core:local**: 本地存储模块
- **core:platform**: 平台抽象模块
- **server**: Ktor 服务器应用
- **build-logic**: 约定插件和构建配置

### 📚 文档

- **[架构设计文档](docs/myhub_architecture.md)** - MyHub 整体架构设计
- **[待办事项](docs/myhub_todos.md)** - 项目整体待办事项
- **[FAQ](docs/FAQ.md)** - 常见问题解答

#### 模块文档

- [core:datastore 架构设计](core/datastore/docs/datastore_architecture.md)
- [core:datastore 待办事项](core/datastore/docs/datastore_todos.md)
- [core:datastore 测试指南](core/datastore/docs/datastore_test_guide.md)

## 📄 许可证

[待添加]

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
