# Datastore Module Suite

## 📋 概述

`core/datastore` 是 MyHub 应用的数据层模块套件，负责所有数据相关的操作，包括本地存储、网络请求、数据模型和业务逻辑。该套件采用模块化设计，将不同职责拆分为独立的子模块，以提高代码的可维护性和可测试性。

> **注意**：`core/datastore` 目录现在主要包含文档，实际的代码已拆分为多个子模块。

## 📚 文档

- **[架构设计文档](./docs/datastore_architecture.md)** - 详细的数据模型架构设计文档
- **[待办事项](./docs/datastore_todos.md)** - 数据层待办事项和功能完善计划

## 🏗️ 模块结构

Datastore 套件由以下子模块组成，按照职责分为三大类：**核心模块**、**数据源模块** 和 **仓库模块**。

### 核心模块

#### 1. `core:datastore-model`

**职责**：定义应用的核心数据模型（领域模型）和 DTO（数据传输对象）

**主要内容**：

- **领域模型**：
  - `Card` - 卡片实体（支持 6 种类型：QUOTE、CODE、IDEA、ARTICLE、DICTIONARY、CHECKLIST）
  - `Tag` - 标签实体
  - `Template` - 模板实体
  - `User` - 用户实体（包含用户偏好设置）
  - `Statistics` - 统计信息
  - `SearchFilter` - 搜索筛选条件
  - `SortBy` - 排序方式枚举
- **DTO（数据传输对象）**：
  - `CardDto` - 卡片 DTO（用于网络传输）
  - `CardMetadataDto` - 卡片元数据 DTO
  - `CreateCardRequest` - 创建卡片请求
  - `UpdateCardRequest` - 更新卡片请求
- **转换函数**：
  - `CardDto.toDomain()` - DTO 转领域模型
  - `Card.toDto()` - 领域模型转 DTO
  - `CreateCardRequest.toDomain()` - 请求转领域模型

**依赖关系**：

- 仅依赖 Kotlin 标准库和序列化库
- 被所有其他 datastore 模块依赖

---

#### 2. `core:datastore-database`

**职责**：SQLDelight 数据库 Schema 定义

**主要内容**：

- 数据库表定义（`.sq` 文件）：
  - `card.sq` - 卡片表
  - `tag.sq` - 标签表
  - `template.sq` - 模板表
  - `user.sq` - 用户表
  - `statistics.sq` - 统计信息表
- 数据库迁移脚本（`.sqm` 文件）
- 生成的 `MyHubDatabase` 接口

**依赖关系**：

- 依赖 `core:datastore-model`（使用领域模型类型）
- 被数据库客户端/服务端模块依赖

---

#### 3. `core:datastore-database-client`

**职责**：客户端数据库配置和驱动工厂

**主要内容**：

- `DatabaseDriverFactory` - 跨平台数据库驱动工厂（expect/actual）
  - Android: `AndroidSqliteDriver`
  - iOS: `NativeSqliteDriver`
  - Desktop (JVM): `JdbcSqliteDriver`
  - Web (JS): `WebWorkerDriver` (SQL.js)
- `DatabaseConfig` - 数据库配置（数据库名称、版本等）
- `DatabaseModule` - Koin 依赖注入模块
  - `databaseDriverFactoryModule()` - 提供 `DatabaseDriverFactory`
  - `databaseModule` - 提供 `MyHubDatabase` 实例

**依赖关系**：

- 依赖 `core:datastore-database`（使用生成的数据库接口）
- 被 `core:datastore-datasource-local` 和 `core:datastore-repository-client` 依赖

---

#### 4. `core:datastore-database-server`

**职责**：服务端数据库配置和驱动工厂

**主要内容**：

- 服务端特定的数据库驱动实现（JVM `JdbcSqliteDriver`）
- 服务端数据库配置
- `DatabaseModule` - Koin 依赖注入模块（服务端版本）

**依赖关系**：

- 依赖 `core:datastore-database`
- 被 `core:datastore-repository-server` 依赖

---

#### 5. `core:datastore-database-test`

**职责**：数据库测试工具和辅助函数

**主要内容**：

- `DatabaseTest` - 数据库基础功能测试
  - Schema 创建测试
  - 插入查询测试
  - 事务回滚测试
  - 外键约束测试
- `DatabaseTestHelper` - 测试辅助函数
  - `createTestDatabase()` - 创建测试数据库（跨平台实现）
- 跨平台测试支持（Android、iOS、JVM、JS）

**依赖关系**：

- 依赖 `core:datastore-database`
- 被其他模块的测试代码依赖

---

### 数据源模块

#### 6. `core:datastore-datasource-local`

**职责**：本地数据源实现（基于 SQLDelight）

**主要内容**：

- **接口定义**：
  - `LocalCardDataSource` - 卡片本地数据源接口
  - `LocalTagDataSource` - 标签本地数据源接口
  - `LocalTemplateDataSource` - 模板本地数据源接口
  - `LocalUserDataSource` - 用户本地数据源接口
  - `LocalStatisticsDataSource` - 统计信息本地数据源接口
- **实现类**：
  - `LocalCardDataSourceImpl` - 使用 SQLDelight 实现
  - `LocalTagDataSourceImpl`
  - `LocalTemplateDataSourceImpl`
  - `LocalUserDataSourceImpl`
  - `LocalStatisticsDataSourceImpl`
- **特性**：
  - 所有数据源都提供 `observe*(): Flow<T>` 响应式方法
  - 支持 CRUD 操作
  - 使用 SQLDelight 进行类型安全的数据库操作
- **依赖注入**：
  - `LocalDataSourceModule` - Koin 模块，提供所有 LocalDataSource 实现

**依赖关系**：

- 依赖 `core:datastore-database`（使用 `MyHubDatabase`）
- 依赖 `core:datastore-model`（使用领域模型）
- 被 `core:datastore-repository-client` 和 `core:datastore-repository-server` 依赖

---

#### 7. `core:datastore-datasource-remote`

**职责**：远程数据源实现（基于 Ktor Client）

**主要内容**：

- **接口定义**：
  - `RemoteCardDataSource` - 卡片远程数据源接口
  - `RemoteTagDataSource` - 标签远程数据源接口
  - `RemoteTemplateDataSource` - 模板远程数据源接口
  - `RemoteUserDataSource` - 用户远程数据源接口
  - `RemoteStatisticsDataSource` - 统计信息远程数据源接口
- **实现类**：
  - `RemoteCardDataSourceImpl` - 使用 Ktor Client 实现
  - `RemoteTagDataSourceImpl`
  - `RemoteTemplateDataSourceImpl`
  - `RemoteUserDataSourceImpl`
  - `RemoteStatisticsDataSourceImpl`
- **网络层**：
  - `NetworkModule` - Ktor Client 配置模块
    - `KtorClientFactory` - Ktor Client 工厂（expect/actual）
    - `HttpClient` - 配置好的 HTTP 客户端实例
    - `ApiConfig` - API 配置（BASE_URL、路径等）
  - `ApiException` / `NetworkException` - 网络异常定义
- **请求对象**：
  - `CreateTagRequest` / `UpdateTagRequest`
  - `CreateTemplateRequest` / `UpdateTemplateRequest`
  - `UpdateUserRequest` / `UpdateUserPreferencesRequest`
- **依赖注入**：
  - `RemoteDataSourceModule` - Koin 模块
    - 包含 `networkModule`
    - 提供所有 RemoteDataSource 实现

**依赖关系**：

- 依赖 `core:datastore-model`（使用领域模型和 DTO）
- 被 `core:datastore-repository-client` 依赖

---

### 仓库模块

#### 8. `core:datastore-repository`

**职责**：Repository 接口定义

**主要内容**：

- **基础接口**（同步风格）：
  - `CardRepository` - 卡片仓库基础接口
  - `TagRepository` - 标签仓库基础接口
  - `TemplateRepository` - 模板仓库基础接口
  - `UserRepository` - 用户仓库基础接口
  - `StatisticsRepository` - 统计信息仓库基础接口
- **响应式接口**（扩展接口，客户端使用）：
  - `ReactiveCardRepository` - 扩展 `CardRepository`，添加 `observe*()` 方法
  - `ReactiveTagRepository` - 扩展 `TagRepository`，添加 `observeAllTags()`
  - `ReactiveTemplateRepository` - 扩展 `TemplateRepository`，添加 `observeAllTemplates()` 和 `observeTemplatesByType()`
  - `ReactiveUserRepository` - 扩展 `UserRepository`，添加 `observeCurrentUser()`
  - `ReactiveStatisticsRepository` - 扩展 `StatisticsRepository`，添加 `observeStatistics()`

**依赖关系**：

- 仅依赖 `core:datastore-model`
- 被 `core:datastore-repository-client` 和 `core:datastore-repository-server` 依赖

---

#### 9. `core:datastore-repository-client`

**职责**：客户端 Repository 实现（协调本地和远程数据源）

**主要内容**：

- **实现类**：
  - `CardRepositoryImpl` - 实现 `ReactiveCardRepository`
    - 协调 `LocalCardDataSource` 和 `RemoteCardDataSource`
    - 优先使用本地数据，本地没有时从远程获取
    - 写操作：先写本地，再同步到远程
    - 提供响应式方法：`observeAllCards()`, `observeSearchCards()`, `observeFavoriteCards()`, `observeCardsByType()`, `observeCardsByTag()`
  - `TagRepositoryImpl` - 实现 `ReactiveTagRepository`
  - `TemplateRepositoryImpl` - 实现 `ReactiveTemplateRepository`
  - `UserRepositoryImpl` - 实现 `ReactiveUserRepository`
  - `StatisticsRepositoryImpl` - 实现 `ReactiveStatisticsRepository`
- **依赖注入**：
  - `RepositoryModule` - Koin 模块（客户端版本）
    - 包含 `databaseModule`（来自 `core:datastore-database-client`）
    - 包含 `localDataSourceModule`（来自 `core:datastore-datasource-local`）
    - 包含 `remoteDataSourceModule`（来自 `core:datastore-datasource-remote`）
    - 提供所有 Repository 实现（同时绑定基础接口和响应式接口）

**依赖关系**：

- 依赖 `core:datastore-repository`（接口）
- 依赖 `core:datastore-datasource-local`
- 依赖 `core:datastore-datasource-remote`
- 依赖 `core:datastore-database-client`
- 被 `composeApp` 依赖

---

#### 10. `core:datastore-repository-server`

**职责**：服务端 Repository 实现（仅使用本地数据源）

**主要内容**：

- **实现类**：
  - `CardRepositoryImpl` - 实现 `CardRepository`（服务端版本，仅使用 `LocalCardDataSource`）
  - `TagRepositoryImpl` - 实现 `TagRepository`
  - `TemplateRepositoryImpl` - 实现 `TemplateRepository`
  - `UserRepositoryImpl` - 实现 `UserRepository`（包含 `initializeDefaultUserIfNeeded()`）
  - `StatisticsRepositoryImpl` - 实现 `StatisticsRepository`
- **依赖注入**：
  - `RepositoryModule` - Koin 模块（服务端版本）
    - 包含 `localDataSourceModule`
    - 提供所有 Repository 实现

**依赖关系**：

- 依赖 `core:datastore-repository`（接口）
- 依赖 `core:datastore-datasource-local`
- 依赖 `core:datastore-database-server`
- 被 `server` 依赖

---

## 📁 模块依赖关系图

### 客户端依赖关系

```
composeApp
    ↓
core:datastore-repository-client
    ↓
    ├── core:datastore-repository (接口)
    ├── core:datastore-datasource-local
    │       ↓
    │       ├── core:datastore-database-client
    │       │       ↓
    │       │       └── core:datastore-database (Schema)
    │       │               ↓
    │       │               └── core:datastore-model
    │       └── core:datastore-model
    ├── core:datastore-datasource-remote
    │       ↓
    │       ├── core:datastore-model
    │       └── networkModule (Ktor Client)
    └── core:datastore-database-client
            ↓
            └── core:datastore-database
                    ↓
                    └── core:datastore-model
```

### 服务端依赖关系

```
server
    ↓
core:datastore-repository-server
    ↓
    ├── core:datastore-repository (接口)
    ├── core:datastore-datasource-local
    │       ↓
    │       ├── core:datastore-database-server
    │       │       ↓
    │       │       └── core:datastore-database (Schema)
    │       │               ↓
    │       │               └── core:datastore-model
    │       └── core:datastore-model
    └── core:datastore-database-server
            ↓
            └── core:datastore-database
                    ↓
                    └── core:datastore-model
```

### Koin 模块包含关系（客户端）

```
repositoryModule (core:datastore-repository-client)
    ├── databaseModule (core:datastore-database-client)
    │       └── databaseDriverFactoryModule()
    ├── localDataSourceModule (core:datastore-datasource-local)
    │       └── 依赖 databaseModule 提供的 MyHubDatabase
    └── remoteDataSourceModule (core:datastore-datasource-remote)
            └── networkModule
                    ├── KtorClientFactory
                    └── HttpClient
```

### Koin 模块包含关系（服务端）

```
repositoryModule (core:datastore-repository-server)
    ├── databaseModule (core:datastore-database-server)
    │       └── databaseDriverFactoryModule()
    └── localDataSourceModule (core:datastore-datasource-local)
            └── 依赖 databaseModule 提供的 MyHubDatabase
```

---

## 🔗 模块间关系说明

### 1. 数据流向

**客户端数据流**：

1. **读取操作**：

   - UI 层调用 `Repository` → `Repository` 优先从 `LocalDataSource` 读取
   - 如果本地没有，`Repository` 从 `RemoteDataSource` 获取并保存到本地
   - 通过 `Flow` 响应式更新 UI

2. **写入操作**：
   - UI 层调用 `Repository` → `Repository` 先写入 `LocalDataSource`（立即生效）
   - 然后异步同步到 `RemoteDataSource`
   - 如果远程同步失败，本地数据仍然保留

**服务端数据流**：

- 所有操作都直接通过 `LocalDataSource` 操作数据库
- 不涉及远程数据源

### 2. 职责分离

- **`core:datastore-model`**：纯数据模型，无业务逻辑，无依赖
- **`core:datastore-database`**：数据库 Schema 定义，生成类型安全的数据库接口
- **`core:datastore-database-client/server`**：平台特定的数据库驱动配置
- **`core:datastore-datasource-local`**：本地数据访问层，封装 SQLDelight 操作
- **`core:datastore-datasource-remote`**：远程数据访问层，封装 Ktor Client 操作
- **`core:datastore-repository`**：业务接口定义，定义数据访问契约
- **`core:datastore-repository-client/server`**：业务逻辑层，协调数据源，实现业务规则

### 3. 接口与实现分离

- **Repository 接口**（`core:datastore-repository`）与实现（`core:datastore-repository-client/server`）分离
- **DataSource 接口**与实现在同一模块，但通过接口暴露
- 便于测试和替换实现

### 4. 客户端与服务端分离

- **客户端**：需要协调本地和远程数据源，支持离线优先和响应式更新
- **服务端**：仅使用本地数据源，直接操作数据库

---

## 📦 提供的功能

### 数据模型（`core:datastore-model`）

- `Card` - 卡片实体（支持 6 种类型：QUOTE、CODE、IDEA、ARTICLE、DICTIONARY、CHECKLIST）
- `Tag` - 标签实体
- `Template` - 模板实体
- `User` - 用户实体（包含用户偏好设置）
- `Statistics` - 统计信息
- `SearchFilter` - 搜索筛选条件
- `SortBy` - 排序方式枚举
- `CardDto` - 卡片 DTO（用于网络传输）
- `CreateCardRequest` / `UpdateCardRequest` - 请求对象

### Repository（客户端）

- `ReactiveCardRepository` - 卡片仓库（响应式接口）
  - `getAllCards()`, `getCardById()`, `searchCards()`
  - `createCard()`, `updateCard()`, `deleteCard()`, `toggleFavorite()`
  - `observeAllCards()`, `observeSearchCards()`, `observeFavoriteCards()`, `observeCardsByType()`, `observeCardsByTag()`
- `ReactiveTagRepository` - 标签仓库（响应式接口）
- `ReactiveTemplateRepository` - 模板仓库（响应式接口）
- `ReactiveUserRepository` - 用户仓库（响应式接口）
- `ReactiveStatisticsRepository` - 统计信息仓库（响应式接口）

所有 Repository 都提供：

- 同步方法（`suspend fun`）
- 响应式方法（`fun observe*(): Flow<T>`）

---

## 🧪 测试

### 测试模块分布

- **`core:datastore-datasource-local`** - 本地数据源单元测试

  - `LocalCardDataSourceTest`
  - `LocalTagDataSourceTest`
  - `LocalTemplateDataSourceTest`
  - `LocalUserDataSourceTest`
  - `LocalStatisticsDataSourceTest`

- **`core:datastore-datasource-remote`** - 远程数据源单元测试

  - `RemoteCardDataSourceTest`
  - `RemoteTagDataSourceTest`
  - `RemoteTemplateDataSourceTest`
  - `RemoteUserDataSourceTest`
  - `RemoteStatisticsDataSourceTest`
  - `TestUtils` - 测试工具类（`createMockHttpClient`）

- **`core:datastore-repository-client`** - Repository 单元测试

  - `CardRepositoryTest`
  - `TagRepositoryTest`
  - `TemplateRepositoryTest`
  - `UserRepositoryTest`
  - `StatisticsRepositoryTest`
  - `Remote*DataSourceStub` - 测试用的 Stub 实现

- **`core:datastore-database-test`** - 数据库基础功能测试
  - `DatabaseTest` - Schema 创建、插入查询、事务回滚、外键约束等

### 测试工具

- `runDatabaseTest` - 跨平台数据库测试辅助函数（在 `core:datastore-database-test` 中）
- `createMockHttpClient` - Mock HTTP 客户端工具（在 `core:datastore-datasource-remote` 中）

---

## 🎯 设计原则

### 1. 模块化设计

- 每个模块职责单一，边界清晰
- 模块之间通过接口依赖，便于测试和替换
- 客户端和服务端实现分离

### 2. 单一数据源 (Single Source of Truth)

- LocalDataSource 是主要数据源
- RemoteDataSource 用于同步
- Repository 协调两者

### 3. 响应式数据流

- 使用 Kotlin Flow 提供响应式数据流
- UI 层通过 StateFlow/Flow 观察数据变化
- 自动更新 UI，无需手动刷新

### 4. 离线优先

- 优先使用本地数据，保证离线可用
- 后台同步远程数据
- 支持离线编辑，上线后自动同步

### 5. 类型安全

- 使用 Kotlin 的强类型系统
- 使用 kotlinx.serialization 进行序列化
- DTO 和 Domain Model 分离，通过转换函数映射
- SQLDelight 提供类型安全的数据库操作

### 6. 可测试性

- Repository 接口便于 Mock
- DataSource 接口便于替换实现
- 业务逻辑与平台实现分离
- 提供测试工具模块和辅助函数

---

## 💡 使用示例

### 在客户端应用中使用

```kotlin
// 在 Koin 模块中引入
val appModule = module {
    includes(repositoryModule) // 来自 core:datastore-repository-client
}

// 在 ViewModel 中使用
class DashboardViewModel(
    private val cardRepository: ReactiveCardRepository,
    private val tagRepository: ReactiveTagRepository
) : ViewModel() {

    val cards: Flow<List<Card>> = cardRepository.observeAllCards()
    val tags: Flow<List<Tag>> = tagRepository.observeAllTags()

    fun createCard(card: Card) {
        viewModelScope.launch {
            cardRepository.createCard(card)
        }
    }
}
```

### 在服务端应用中使用

```kotlin
// 在 Koin 模块中引入
val serverModule = module {
    includes(repositoryModule) // 来自 core:datastore-repository-server
}

// 在服务中使用
class CardService(
    private val cardRepository: CardRepository
) {
    suspend fun getAllCards(): List<Card> {
        return cardRepository.getAllCards()
    }
}
```

---

## 🔧 技术栈

- **序列化**: kotlinx.serialization
- **日期时间**: kotlin.time.Instant (kotlinx.datetime 用于 Clock)
- **协程**: kotlinx.coroutines
- **响应式**: Kotlin Flow
- **依赖注入**: Koin
- **网络**: Ktor Client
- **本地存储**: SQLDelight (SQLite)
  - Android: AndroidSqliteDriver
  - iOS: NativeSqliteDriver
  - Desktop (JVM): JdbcSqliteDriver
  - Web: WebWorkerDriver (SQL.js)

---

## 📚 详细文档

- [架构设计文档](./docs/datastore_architecture.md) - 详细的数据模型架构设计文档
- [待办事项](./docs/datastore_todos.md) - 数据层待办事项和功能完善计划

---

**最后更新**: 2025-01-XX  
**维护者**: MyHub Team  
**状态**: ✅ 模块化重构完成，所有功能正常运行
