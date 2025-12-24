# MyHub 架构设计

## 📋 概述

MyHub 是一个基于 Kotlin Multiplatform 和 Compose Multiplatform 的跨平台应用，支持 Android、iOS、Desktop、Web 等多个平台。本文档描述了 MyHub 应用的整体架构设计。

## 🏗️ 整体架构

### 分层架构

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Android    │  │     iOS     │  │  Desktop/Web │  │
│  │   (Compose)  │  │  (Compose)  │  │   (Compose)  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                           │
│  - ViewModel (业务逻辑)                                  │
│  - UI State (状态管理)                                   │
│  - Composable Functions (UI 组件)                        │
└───────────────────────┬───────────────────────────────────┘
                        │
┌───────────────────────▼───────────────────────────────────┐
│              Application Layer                            │
│  ┌─────────────────────────────────────────────────────┐  │
│  │              composeApp Module                      │  │
│  │  - UI 实现和业务逻辑协调                              │  │
│  │  - 依赖注入配置 (Koin)                               │  │
│  │  - 平台特定配置                                       │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────┬───────────────────────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
┌───────▼────────┐          ┌──────────▼──────────┐
│  Core Modules  │          │   Server Module     │
│                │          │                     │
│  - datastore   │          │  - Ktor Server     │
│  - local       │          │  - API Endpoints    │
│  - platform    │          │  - Business Logic   │
└────────────────┘          └─────────────────────┘
```

### 模块依赖关系

```
composeApp
├── core:datastore (数据层)
│   └── core:platform (平台抽象)
├── core:local (本地存储)
└── core:platform (平台抽象)

server
└── (独立运行，提供 API 服务)

core:datastore
└── core:platform

core:local
└── core:platform
```

## 📦 模块说明

### 1. composeApp - 主应用模块

**职责**：

- UI 层实现（Compose Multiplatform）
- ViewModel 和业务逻辑协调
- 依赖注入配置
- 平台特定配置和入口点

**支持的平台**：

- Android
- iOS
- Desktop (JVM)
- Web (JS/WASM)

**技术栈**：

- Compose Multiplatform
- Koin (依赖注入)
- ViewModel (状态管理)

**文档**：

- [模块 README](../composeApp/README.md) (待创建)

---

### 2. core:datastore - 数据层模块

**职责**：

- 数据模型定义（Card、Tag、Template、User、Statistics 等）
- 本地数据源实现（SQLDelight）
- 远程数据源实现（Ktor Client）
- Repository 层实现（业务逻辑协调）
- 数据库配置和迁移
- 网络层配置

**技术栈**：

- SQLDelight (本地存储)
- Ktor Client (网络请求)
- Kotlin Flow (响应式数据流)
- Koin (依赖注入)

**文档**：

- [模块 README](../core/datastore/README.md)
- [架构设计文档](../core/datastore/docs/datastore_architecture.md)
- [测试指南](../core/datastore/docs/datastore_test_guide.md)
- [待办事项](../core/datastore/docs/datastore_todos.md)

---

### 3. core:local - 本地存储模块

**职责**：

- 本地文件存储抽象
- 平台特定的本地存储实现
- 配置和偏好设置存储

**技术栈**：

- Kotlin Multiplatform
- 平台特定实现（Android、iOS、Desktop、Web）

**文档**：

- [模块 README](../core/local/README.md)

---

### 4. core:platform - 平台抽象模块

**职责**：

- 平台常量定义（如 SERVER_PORT）
- 平台抽象接口
- 跨平台工具类

**技术栈**：

- Kotlin Multiplatform

**文档**：

- [模块 README](../core/platform/README.md) (待创建)

---

### 5. server - 服务器模块

**职责**：

- Ktor 服务器实现
- REST API 端点
- 业务逻辑处理
- 数据持久化

**技术栈**：

- Ktor Server
- Kotlin Coroutines
- 数据库（待确定）

**文档**：

- [模块 README](../server/README.md) (待创建)

---

## 🔄 数据流

### 读取数据流程

```
UI Layer (Compose)
    ↓
ViewModel
    ↓
Repository (core:datastore)
    ↓
LocalDataSource (SQLDelight) ← 优先读取，快速响应
    ↓
[如果需要] RemoteDataSource (Ktor Client) ← 同步最新数据
    ↓
更新 LocalDataSource
    ↓
通过 Flow 返回给 ViewModel
    ↓
更新 UI State
    ↓
UI 自动更新
```

### 写入数据流程

```
UI Layer (用户操作)
    ↓
ViewModel
    ↓
Repository (core:datastore)
    ↓
1. 乐观更新 LocalDataSource (立即响应)
    ↓
2. 同步到 RemoteDataSource (Ktor Client)
    ↓
3. 如果成功：更新本地数据
   如果失败：回滚或标记待同步
    ↓
通过 Flow 通知 ViewModel
    ↓
更新 UI State
    ↓
UI 自动更新
```

## 🎯 设计原则

### 1. 模块化

- 清晰的模块边界
- 单一职责原则
- 低耦合、高内聚

### 2. 跨平台优先

- 最大化共享代码
- 最小化平台特定代码
- 统一的 API 接口

### 3. 响应式架构

- 使用 Kotlin Flow 提供响应式数据流
- UI 自动响应数据变化
- 状态管理统一

### 4. 离线优先

- 优先使用本地数据
- 后台同步远程数据
- 支持离线编辑

### 5. 可测试性

- 接口便于 Mock
- 业务逻辑与平台实现分离
- 完整的单元测试覆盖

## 🔧 技术栈

### 核心框架

- **Kotlin Multiplatform** - 跨平台开发框架
- **Compose Multiplatform** - 声明式 UI 框架
- **Kotlin Coroutines** - 异步编程
- **Kotlin Flow** - 响应式数据流

### 数据层

- **SQLDelight** - 类型安全的 SQL 查询构建器
- **Ktor Client** - 异步 HTTP 客户端
- **kotlinx.serialization** - 序列化框架

### 依赖注入

- **Koin** - 轻量级依赖注入框架

### 构建工具

- **Gradle** - 构建工具
- **Kotlin Multiplatform Plugin** - 跨平台构建支持

### 服务器

- **Ktor Server** - 异步 Web 框架

## 📁 项目结构

```
app-myhub/
├── composeApp/              # 主应用模块
│   ├── src/
│   │   ├── commonMain/      # 共享代码
│   │   ├── androidMain/     # Android 特定
│   │   ├── iosMain/         # iOS 特定
│   │   ├── jvmMain/         # Desktop 特定
│   │   └── jsMain/          # Web 特定
│   └── build.gradle.kts
│
├── core/                    # 核心模块
│   ├── datastore/           # 数据层模块
│   │   ├── docs/            # 模块文档
│   │   └── src/
│   ├── local/               # 本地存储模块
│   └── platform/           # 平台抽象模块
│
├── server/                  # 服务器模块
│   └── src/
│
├── iosApp/                  # iOS 原生应用配置
│   └── iosApp.xcodeproj/
│
├── docs/                    # 项目文档
│   ├── myhub_architecture.md    # 本文档
│   ├── myhub_todos.md           # 待办事项
│   └── ...
│
└── settings.gradle.kts      # 项目配置
```

## 🔗 模块间通信

### composeApp ↔ core:datastore

- **composeApp** 通过 Repository 接口使用 **core:datastore** 的功能
- 使用 Koin 进行依赖注入
- 通过 Kotlin Flow 进行数据流通信

### core:datastore ↔ core:platform

- **core:datastore** 使用 **core:platform** 提供的平台常量
- 平台抽象接口

### composeApp ↔ core:local

- **composeApp** 使用 **core:local** 进行本地文件存储
- 配置和偏好设置

### composeApp ↔ server

- 通过 **core:datastore** 的 RemoteDataSource 间接通信
- REST API 调用

## 📚 相关文档

### 项目文档

- [项目 README](../README.md) - 项目概述和快速开始
- [待办事项](./myhub_todos.md) - 整体待办事项

### 模块文档

- [core:datastore 架构设计](../core/datastore/docs/datastore_architecture.md)
- [core:datastore 测试指南](../core/datastore/docs/datastore_test_guide.md)
- [core:datastore 待办事项](../core/datastore/docs/datastore_todos.md)

### 迁移文档

- [数据模块迁移](./DATA_MODULE_MIGRATION.md)
- [Datastore 模块重命名](./DATASTORE_MODULE_RENAME.md)

### 其他文档

- [FAQ](./FAQ.md) - 常见问题解答
- [iOS Framework 配置](./IOS_FRAMEWORK_DEFAULT_CONFIG.md)
- [iOS Framework 静态 vs 动态](./iOS_FRAMEWORK_STATIC_VS_DYNAMIC.md)

## 💡 最佳实践

1. **模块边界清晰**：各模块职责明确，避免循环依赖
2. **接口驱动开发**：使用接口定义模块间的契约
3. **测试优先**：为每个模块编写完整的单元测试
4. **文档同步**：代码变更时及时更新文档
5. **平台抽象**：将平台特定代码封装在平台模块中

## ⚠️ 注意事项

1. **模块依赖**：注意模块间的依赖关系，避免循环依赖
2. **平台差异**：处理不同平台的差异时，使用 `expect/actual` 机制
3. **性能优化**：注意跨平台代码的性能影响
4. **测试覆盖**：确保所有平台的测试都能正常运行
5. **文档维护**：保持文档与代码同步更新

---

**最后更新**: 2025-12-25  
**维护者**: MyHub Team  
**状态**: 🚧 持续更新中
