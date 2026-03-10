# MyHub 数据存储套件总体架构设计

**方案名称**：Datastore Suite Infra v1  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-13  
**锁定日期**：2026-01-13  
**最后更新**：2026-01-13  
**作者**：MyHub Development Team  
**评审状态**：🟢 通过  
**方案状态**：🔒 已锁定

---

## 📋 文档目录

1. [修改历史](#修改历史)
2. [方案状态摘要](#-方案状态摘要)
3. [问题背景](#1-问题背景)
4. [设计目标](#2-设计目标)
5. [技术调研](#3-技术调研)
6. [架构设计](#4-架构设计)
7. [实现细节](#5-实现细节)
8. [实施计划](#6-实施计划)
9. [风险评估](#7-风险评估)
10. [附录](#8-附录)

---

## 📊 方案状态摘要

**当前状态**：

- **评审状态**：🟢 通过（文档已通过评审，可以进入实施阶段）
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Suite Infra v1 的基线设计
- **锁定日期**：2026-01-13
- **当前进度**：所有阶段已完成，方案设计已确定并锁定

**状态说明**：

- **评审状态**：用于标识文档的评审进度
    - 🟢 通过：文档已通过评审，可以进入实施阶段
    - 🟡 待评审：文档正在等待评审或评审进行中
    - 🔴 需修改：文档评审后需要修改
- **方案状态**：用于标识方案的实施进度
    - 🔒 已锁定：方案设计已确定，不允许随意修改
    - 📝 进行中：方案设计正在进行中，可以修改
    - ⏸️ 暂停：方案设计暂时停止，保留当前状态
- 详细状态定义请参考 [MyHub 架构设计文档规范](../../../docs/infra/myhub-infra-rules.md)

---

## 修改历史

| 版本   | 日期         | 修改内容            | 修改原因      |
|------|------------|-----------------|-----------|
| v1.0 | 2026-01-13 | 初始方案设计          | 新建        |
| v1.0 | 2026-01-13 | 完成架构设计文档        | 完善文档      |
| v1.0 | 2026-01-13 | 更新状态：评审通过、方案已锁定 | 状态更新：评审通过 |

---

## 1. 问题背景

### 1.1 用户场景

在 MyHub 应用的开发和运行过程中，需要处理数据的存储、访问和同步。典型的场景包括：

1. **数据持久化**：应用需要将数据持久化到本地数据库，支持离线访问
2. **数据同步**：客户端需要与服务器同步数据，确保数据一致性
3. **响应式更新**：数据变化时需要实时更新 UI
4. **多端支持**：数据层需要在 Android、iOS、Desktop、Web 等多端保持一致
5. **用户隔离**：支持多用户场景，确保用户数据隔离

### 1.2 问题根因

在引入统一的数据存储套件之前，MyHub 应用面临以下问题：

1. **数据访问逻辑分散**：各模块直接访问数据库或网络，导致代码重复和不一致
2. **缺乏统一抽象**：没有统一的数据访问接口，难以替换实现
3. **同步逻辑缺失**：缺乏统一的数据同步机制，导致数据不一致
4. **响应式更新缺失**：缺乏数据变化的实时监听机制
5. **跨平台兼容性**：不同平台使用不同的数据存储实现，难以统一
6. **测试困难**：数据访问逻辑与业务逻辑耦合，难以测试

### 1.3 影响范围

- **代码维护**：数据访问逻辑分散，增加维护成本和出错风险
- **用户体验**：缺乏实时更新和离线支持导致用户体验差
- **数据一致性**：本地和远程数据可能不一致
- **开发效率**：缺乏统一抽象降低开发效率
- **测试成本**：数据访问逻辑难以测试，增加测试成本

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **分层架构**：采用清晰的分层架构（Repository → Datasource → Database），职责明确
- ✅ **统一接口**：提供统一的数据访问接口，便于替换实现
- ✅ **响应式数据流**：支持 Kotlin Flow，实现数据变更的实时监听
- ✅ **离线优先**：优先使用本地数据，保证离线可用
- ✅ **数据同步**：自动从远程同步数据到本地
- ✅ **用户隔离**：支持多用户场景，确保用户数据隔离
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）
- ✅ **类型安全**：使用类型安全的数据库查询和序列化

### 2.2 非功能目标

- ✅ **性能优化**：优先使用本地数据，减少网络请求
- ✅ **可测试性**：清晰的接口设计，便于 Mock 和测试
- ✅ **可维护性**：模块化设计，职责清晰，易于维护
- ✅ **可扩展性**：易于添加新的数据源和业务逻辑
- ✅ **错误处理**：统一的错误处理机制

### 2.3 模块特性说明

**重要说明**：`datastore` 是一个**混合模块套件**，包含多个子模块，每个子模块专注于特定的职责。整体采用分层架构设计，由上而下分为：**Repository 层**、**Datasource 层**、**Database 层**。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据库：SQLDelight

**选择理由**：

- ✅ **KMP 原生支持**：SQLDelight 完全支持 KMP，所有平台共享 Schema 定义
- ✅ **类型安全**：编译时生成类型安全的 Kotlin 代码
- ✅ **跨平台兼容**：支持 Android、iOS、JVM、JS、WASM
- ✅ **性能优秀**：直接使用 SQLite，性能优异
- ✅ **官方支持**：Cash App 维护，社区活跃

**官方文档**：<https://cashapp.github.io/sqldelight/>

#### 3.1.2 网络：Ktor Client

**选择理由**：

- ✅ **KMP 原生支持**：Ktor Client 完全支持 KMP
- ✅ **协程集成**：与 Kotlin Coroutines 完美集成
- ✅ **易于配置**：支持灵活的配置和拦截器
- ✅ **官方支持**：JetBrains 维护，社区活跃

**官方文档**：<https://ktor.io/docs/client.html>

#### 3.1.3 序列化：kotlinx.serialization

**选择理由**：

- ✅ **KMP 原生支持**：kotlinx.serialization 完全支持 KMP
- ✅ **类型安全**：编译时生成序列化代码
- ✅ **性能优秀**：性能优于其他序列化库
- ✅ **官方支持**：JetBrains 维护，社区活跃

**官方文档**：<https://kotlinlang.org/docs/serialization.html>

#### 3.1.4 响应式：Kotlin Flow

**选择理由**：

- ✅ **KMP 原生支持**：Kotlin Flow 完全支持 KMP
- ✅ **协程集成**：与 Kotlin Coroutines 完美集成
- ✅ **实时更新**：支持数据变化的实时监听
- ✅ **官方支持**：JetBrains 维护，社区活跃

**官方文档**：<https://kotlinlang.org/docs/flow.html>

#### 3.1.5 依赖注入：Koin

**选择理由**：

- ✅ **KMP 原生支持**：Koin 完全支持 KMP
- ✅ **轻量级**：相比 Dagger，Koin 更轻量级
- ✅ **易于使用**：声明式 API，易于理解和使用
- ✅ **社区活跃**：社区活跃，文档完善

**官方文档**：<https://insert-koin.io/>

### 3.2 架构模式

#### 3.2.1 Repository 模式

**设计原则**：

- **Repository 接口**：定义数据访问接口，隐藏数据源细节
- **Repository 实现**：协调多个数据源，实现业务逻辑
- **数据源抽象**：LocalDataSource 和 RemoteDataSource 分离

**优势**：

- ✅ **关注点分离**：数据访问逻辑与业务逻辑分离
- ✅ **易于测试**：接口清晰，易于 Mock
- ✅ **易于扩展**：可以轻松添加新的数据源

#### 3.2.2 分层架构

**设计原则**：

- **Repository 层**：业务逻辑层，协调数据源
- **Datasource 层**：数据访问层，封装数据源细节
- **Database 层**：数据存储层，定义 Schema 和驱动

**优势**：

- ✅ **职责清晰**：每层职责明确，易于理解
- ✅ **易于维护**：修改一层不影响其他层
- ✅ **易于测试**：可以单独测试每一层

---

## 4. 架构设计

### 4.1 整体架构

#### 4.1.1 分层架构图

```text
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                       │
│  - ViewModel                                                │
│  - UI State (CardUiState, etc.)                            │
│  - Composable Functions                                     │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ 使用 Repository 接口
                           │
┌──────────────────────────▼──────────────────────────────────┐
│              Repository Layer (业务逻辑层)                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:repository-client                    │  │
│  │  - CardRepositoryImpl                                │  │
│  │  - TagRepositoryImpl                                 │  │
│  │  - TemplateRepositoryImpl                            │  │
│  │  - UserRepositoryImpl                                │  │
│  │  - StatisticsRepositoryImpl                         │  │
│  │  (协调本地和远程数据源，实现业务逻辑)                  │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:repository-server                    │  │
│  │  - CardRepositoryImpl (服务端版本)                    │  │
│  │  - TagRepositoryImpl                                 │  │
│  │  (仅使用本地数据源，封装业务逻辑)                      │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ 使用 DataSource 接口
                           │
┌──────────────────────────▼──────────────────────────────────┐
│            Datasource Layer (数据访问层)                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:datasource-local                    │  │
│  │  - LocalCardDataSource                              │  │
│  │  - LocalTagDataSource                               │  │
│  │  - LocalTemplateDataSource                          │  │
│  │  - LocalUserDataSource                              │  │
│  │  - LocalStatisticsDataSource                        │  │
│  │  (基于 SQLDelight，提供本地数据访问)                  │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:datasource-remote                   │  │
│  │  - RemoteCardDataSource                             │  │
│  │  - RemoteTagDataSource                              │  │
│  │  - RemoteTemplateDataSource                         │  │
│  │  - RemoteUserDataSource                             │  │
│  │  - RemoteStatisticsDataSource                       │  │
│  │  (基于 Ktor Client，提供远程数据访问)                  │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ 使用 Database 接口
                           │
┌──────────────────────────▼──────────────────────────────────┐
│            Database Layer (数据存储层)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:database                            │  │
│  │  - SQLDelight Schema (.sq 文件)                     │  │
│  │  - MyHubDatabase (生成的接口)                        │  │
│  │  (定义数据库 Schema，生成类型安全的查询接口)            │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:database-client                      │  │
│  │  - DatabaseDriverFactory (expect/actual)             │  │
│  │  - DatabaseModule (Koin)                            │  │
│  │  (客户端数据库驱动工厂和配置)                          │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:database-server                      │  │
│  │  - DatabaseDriverFactory (服务端版本)                │  │
│  │  - DatabaseModule (Koin)                             │  │
│  │  (服务端数据库驱动工厂和配置)                          │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ 使用 Model
                           │
┌──────────────────────────▼──────────────────────────────────┐
│              Model Layer (数据模型层)                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  datastore:model                               │  │
│  │  - Card, Tag, Template, User, Statistics            │  │
│  │  - CardDto, CreateCardRequest, UpdateCardRequest    │  │
│  │  - 转换函数 (toDomain(), toDto())                   │  │
│  │  (定义领域模型和 DTO，提供转换函数)                    │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

#### 4.1.2 数据流向

**客户端数据流（读取）**：

```text
UI Layer
    ↓
Repository (ReactiveCardRepository)
    ↓
LocalDataSource (优先读取，快速响应)
    ↓
[如果需要] RemoteDataSource (同步最新数据)
    ↓
更新 LocalDataSource
    ↓
通过 Flow 返回给 UI
```

**客户端数据流（写入）**：

```text
UI Layer
    ↓
Repository (ReactiveCardRepository)
    ↓
1. 乐观更新 LocalDataSource (立即响应)
    ↓
2. 同步到 RemoteDataSource
    ↓
3. 如果成功：更新本地数据
   如果失败：回滚或标记待同步
    ↓
通过 Flow 通知 UI 更新
```

**服务端数据流**：

```text
Service Layer
    ↓
Repository (CardRepository)
    ↓
LocalDataSource (直接操作数据库)
    ↓
Database (SQLDelight)
```

### 4.2 核心组件

#### 4.2.1 Repository 层

**职责**：

- 协调本地和远程数据源
- 实现业务逻辑（如数据同步、缓存策略）
- 提供统一的接口给 UI 层

**核心模块**：

- **`datastore:repository`**：Repository 接口定义
    - `CardRepository` / `ReactiveCardRepository`
    - `TagRepository` / `ReactiveTagRepository`
    - `TemplateRepository` / `ReactiveTemplateRepository`
    - `UserRepository` / `ReactiveUserRepository`
    - `StatisticsRepository` / `ReactiveStatisticsRepository`

- **`datastore:repository-client`**：客户端 Repository 实现
    - 协调 `LocalDataSource` 和 `RemoteDataSource`
    - 实现响应式接口（`observe*()` 方法）
    - 实现数据同步逻辑

- **`datastore:repository-server`**：服务端 Repository 实现
    - 仅使用 `LocalDataSource`
    - 封装业务逻辑
    - 确保用户隔离

#### 4.2.2 Datasource 层

**职责**：

- 封装数据源细节（数据库、网络）
- 提供统一的数据访问接口
- 实现数据转换（Domain Model ↔ DTO）

**核心模块**：

- **`datastore:datasource-local`**：本地数据源实现
    - 基于 SQLDelight，提供类型安全的数据库操作
    - 提供响应式方法（`observe*(): Flow<T>`）
    - 实现用户隔离（基于 `userId`）

- **`datastore:datasource-remote`**：远程数据源实现
    - 基于 Ktor Client，提供 HTTP API 调用
    - 处理 JSON 序列化/反序列化
    - 处理网络错误和异常

#### 4.2.3 Database 层

**职责**：

- 定义数据库 Schema
- 提供数据库驱动工厂
- 管理数据库版本和迁移

**核心模块**：

- **`datastore:database`**：数据库 Schema 定义
    - SQLDelight Schema 文件（`.sq` 文件）
    - 生成的 `MyHubDatabase` 接口
    - 数据库迁移脚本（`.sqm` 文件）

- **`datastore:database-client`**：客户端数据库配置
    - `DatabaseDriverFactory`（expect/actual）
    - 平台特定的驱动实现（Android、iOS、JVM、JS）
    - Koin DI 模块

- **`datastore:database-server`**：服务端数据库配置
    - 服务端特定的驱动实现（JVM）
    - 支持 SQLite 和 PostgreSQL（通过环境变量配置）
    - Koin DI 模块

- **`datastore:database-test`**：数据库测试工具
    - `runDatabaseTest` 辅助函数
    - 跨平台测试支持
    - 内存数据库创建

- **`datastore:database-manage`**：数据库管理工具
    - 初始数据加载（从 JSON 资源文件）
    - 数据清理功能
    - 用户数据关联

#### 4.2.4 Model 层

**职责**：

- 定义领域模型（Domain Model）
- 定义数据传输对象（DTO）
- 提供转换函数

**核心模块**：

- **`datastore:model`**：数据模型定义
    - 领域模型：`Card`、`Tag`、`Template`、`User`、`Statistics`
    - DTO：`CardDto`、`CreateCardRequest`、`UpdateCardRequest`
    - 转换函数：`toDomain()`、`toDto()`

### 4.3 模块依赖关系

#### 4.3.1 客户端依赖关系

```text
composeApp
    ↓
datastore:repository-client
    ↓
    ├── datastore:repository (接口)
    ├── datastore:datasource-local
    │       ↓
    │       ├── datastore:database-client
    │       │       ↓
    │       │       └── datastore:database (Schema)
    │       │               ↓
    │       │               └── datastore:model
    │       └── datastore:model
    ├── datastore:datasource-remote
    │       ↓
    │       ├── datastore:model
    │       └── core:network (Ktor Client)
    └── datastore:database-client
            ↓
            └── datastore:database
                    ↓
                    └── datastore:model
```

#### 4.3.2 服务端依赖关系

```text
server
    ↓
datastore:repository-server
    ↓
    ├── datastore:repository (接口)
    ├── datastore:datasource-local
    │       ↓
    │       ├── datastore:database-server
    │       │       ↓
    │       │       └── datastore:database (Schema)
    │       │               ↓
    │       │               └── datastore:model
    │       └── datastore:model
    └── datastore:database-server
            ↓
            └── datastore:database
                    ↓
                    └── datastore:model
```

### 4.4 设计原则

#### 4.4.1 分层设计原则

1. **单一职责**：每层只负责自己的职责
2. **依赖方向**：上层依赖下层，下层不依赖上层
3. **接口抽象**：层与层之间通过接口交互
4. **平台隔离**：平台特定实现通过 expect/actual 机制隔离

#### 4.4.2 数据访问原则

1. **离线优先**：优先使用本地数据，保证离线可用
2. **响应式更新**：使用 Flow 提供响应式数据流
3. **用户隔离**：所有数据操作都基于 `userId` 进行隔离
4. **类型安全**：使用类型安全的查询和序列化

#### 4.4.3 模块化原则

1. **模块职责单一**：每个模块只负责一个职责
2. **接口与实现分离**：接口定义和实现分离
3. **客户端与服务端分离**：客户端和服务端实现分离
4. **易于测试**：清晰的接口设计，便于 Mock 和测试

---

## 5. 实现细节

### 5.1 Repository 层实现

#### 5.1.1 客户端 Repository 实现

**核心逻辑**：

```kotlin
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
    private val remoteDataSource: RemoteCardDataSource
) : ReactiveCardRepository {
    
    override fun observeAllCards(): Flow<List<Card>> {
        return localDataSource.observeAllCards()
            .onStart {
                // 首次加载时从远程同步
                refreshFromRemote()
            }
    }
    
    override suspend fun createCard(card: Card): Card {
        // 1. 先写入本地（乐观更新）
        val createdCard = localDataSource.insertCard(card)
        
        // 2. 异步同步到远程
        try {
            remoteDataSource.createCard(card.toDto())
        } catch (e: Exception) {
            // 同步失败，标记待同步
            markForSync(createdCard.id)
        }
        
        return createdCard
    }
    
    private suspend fun refreshFromRemote() {
        try {
            val remoteCards = remoteDataSource.getAllCards()
            localDataSource.insertAll(remoteCards.map { it.toDomain() })
        } catch (e: Exception) {
            // 同步失败，使用本地数据
        }
    }
}
```

#### 5.1.2 服务端 Repository 实现

**核心逻辑**：

```kotlin
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource
) : CardRepository {
    
    override suspend fun getAllCards(): List<Card> {
        return localDataSource.getAllCards()
    }
    
    override suspend fun createCard(card: Card): Card {
        return localDataSource.insertCard(card)
    }
}
```

### 5.2 Datasource 层实现

#### 5.2.1 本地数据源实现

**核心逻辑**：

```kotlin
class LocalCardDataSourceImpl(
    private val database: MyHubDatabase,
    private val userContextProvider: UserContextProvider
) : LocalCardDataSource {
    
    override fun observeAllCards(): Flow<List<Card>> {
        val userId = userContextProvider.getCurrentUserId()
        return database.cardQueries
            .observeAllCards(userId)
            .map { it.map { row -> row.toDomain() } }
    }
    
    override suspend fun insertCard(card: Card) {
        val userId = userContextProvider.getCurrentUserId()
        database.cardQueries.insertCard(
            id = card.id,
            userId = userId,
            type = card.type.name,
            title = card.title,
            content = card.content,
            // ... 其他字段
        )
    }
}
```

#### 5.2.2 远程数据源实现

**核心逻辑**：

```kotlin
class RemoteCardDataSourceImpl(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig
) : RemoteCardDataSource {
    
    override suspend fun getAllCards(): List<CardDto> {
        return httpClient.get("${apiConfig.baseUrl}/cards") {
            contentType(ContentType.Application.Json)
        }.body<List<CardDto>>()
    }
    
    override suspend fun createCard(cardDto: CardDto): CardDto {
        return httpClient.post("${apiConfig.baseUrl}/cards") {
            contentType(ContentType.Application.Json)
            setBody(cardDto)
        }.body<CardDto>()
    }
}
```

### 5.3 Database 层实现

#### 5.3.1 Schema 定义

**示例 Schema**：

```sql
-- card.sq
CREATE TABLE card (
    id TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT,
    content TEXT NOT NULL,
    -- ... 其他字段
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE INDEX card_user_id ON card(user_id);
CREATE INDEX card_type ON card(type);
CREATE INDEX card_created_at ON card(created_at);

selectAllCards:
SELECT * FROM card WHERE user_id = ? ORDER BY updated_at DESC;

insertCard:
INSERT INTO card (id, user_id, type, title, content, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?, ?);
```

#### 5.3.2 数据库驱动工厂

**客户端实现**：

```kotlin
// expect
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// Android actual
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MyHubDatabase.Schema,
            context = getApplicationContext(),
            name = "myhub.db"
        )
    }
}

// iOS actual
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = MyHubDatabase.Schema,
            name = "myhub.db"
        )
    }
}
```

### 5.4 依赖注入配置

#### 5.4.1 客户端 Koin 模块

```kotlin
val repositoryModule = module {
    // Database
    includes(databaseModule) // 来自 datastore:database-client
    
    // DataSource
    includes(localDataSourceModule) // 来自 datastore:datasource-local
    includes(remoteDataSourceModule) // 来自 datastore:datasource-remote
    
    // Repository
    single<CardRepository> { CardRepositoryImpl(get(), get()) }
    single<ReactiveCardRepository> { get<CardRepository>() as ReactiveCardRepository }
    // ... 其他 Repository
}
```

#### 5.4.2 服务端 Koin 模块

```kotlin
val repositoryModule = module {
    // Database
    includes(databaseModule) // 来自 datastore:database-server
    
    // DataSource
    includes(localDataSourceModule) // 来自 datastore:datasource-local
    
    // Repository
    single<CardRepository> { CardRepositoryImpl(get()) }
    // ... 其他 Repository
}
```

---

## 6. 实施计划

### 6.1 当前进度

**方案状态**：🔒 已锁定

**状态说明**：

- 🔒 **已锁定**：方案已于 2026-01-13 锁定，锁定原因：评审通过，方案设计已确定

**已完成阶段**：

- ✅ **阶段 1**：数据模型层设计（`datastore:model`）
- ✅ **阶段 2**：数据库层设计（`datastore:database`、`datastore:database-client`、`datastore:database-server`）
- ✅ **阶段 3**：数据源层设计（`datastore:datasource-local`、`datastore:datasource-remote`）
- ✅ **阶段 4**：仓库层设计（`datastore:repository-client`、`datastore:repository-server`）
- ✅ **阶段 5**：总体架构设计（本文档）

**下一步计划**：

- ✅ **已完成**：评审通过，方案已锁定

### 6.2 阶段划分

#### 阶段 1：数据模型层（✅ 已完成）

- **目标**：定义领域模型和 DTO
- **产出**：`datastore:model` 模块
- **时间**：已完成

#### 阶段 2：数据库层（✅ 已完成）

- **目标**：定义数据库 Schema 和驱动工厂
- **产出**：
    - `datastore:database` 模块
    - `datastore:database-client` 模块
    - `datastore:database-server` 模块
    - `datastore:database-test` 模块
    - `datastore:database-manage` 模块
- **时间**：已完成

#### 阶段 3：数据源层（✅ 已完成）

- **目标**：实现本地和远程数据源
- **产出**：
    - `datastore:datasource-local` 模块
    - `datastore:datasource-remote` 模块
- **时间**：已完成

#### 阶段 4：仓库层（✅ 已完成）

- **目标**：实现客户端和服务端仓库
- **产出**：
    - `datastore:repository` 模块（接口）
    - `datastore:repository-client` 模块
    - `datastore:repository-server` 模块
- **时间**：已完成

#### 阶段 5：总体架构设计（✅ 已完成）

- **目标**：完成总体架构设计文档
- **产出**：本文档
- **时间**：已完成

### 6.3 里程碑

- ✅ **里程碑 1**：数据模型层完成（2026-01-13）
- ✅ **里程碑 2**：数据库层完成（2026-01-13）
- ✅ **里程碑 3**：数据源层完成（2026-01-13）
- ✅ **里程碑 4**：仓库层完成（2026-01-13）
- ✅ **里程碑 5**：总体架构设计完成（2026-01-13）
- ✅ **里程碑 6**：评审通过，方案锁定（2026-01-13）

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 跨平台兼容性风险

**风险描述**：不同平台的数据库驱动和网络库可能存在兼容性问题

**风险等级**：🟡 中等

**缓解措施**：

- ✅ 使用成熟的 KMP 库（SQLDelight、Ktor Client）
- ✅ 提供平台特定的测试覆盖
- ✅ 使用 expect/actual 机制隔离平台差异
- ✅ 参考现有模块的实现经验

#### 7.1.2 数据同步冲突风险

**风险描述**：客户端和服务端数据可能发生冲突

**风险等级**：🟡 中等

**缓解措施**：

- ✅ 使用时间戳和版本号进行冲突检测
- ✅ 实现冲突解决策略（如最后写入获胜）
- ✅ 提供数据同步状态监控

#### 7.1.3 性能风险

**风险描述**：大量数据可能导致性能问题

**风险等级**：🟢 低

**缓解措施**：

- ✅ 使用分页查询
- ✅ 使用索引优化查询性能
- ✅ 实现数据缓存策略

### 7.2 边界条件

#### 7.2.1 功能边界

**方案解决**：

- ✅ 数据持久化（本地数据库）
- ✅ 数据同步（客户端 ↔ 服务端）
- ✅ 响应式数据流（Flow）
- ✅ 用户隔离（多用户支持）

**方案不解决**：

- ❌ 数据加密（由上层模块处理）
- ❌ 数据备份和恢复（由上层模块处理）
- ❌ 数据分析和统计（由业务层处理）

#### 7.2.2 平台边界

**完全支持**：

- ✅ Android（使用 AndroidSqliteDriver）
- ✅ iOS（使用 NativeSqliteDriver）
- ✅ Desktop JVM（使用 JdbcSqliteDriver）
- ✅ Web JS（使用 WebWorkerDriver）

**部分支持**：

- ⚠️ Web WASM（需要特殊处理，参考现有实现）

#### 7.2.3 性能边界

**性能指标**：

- ✅ 数据库查询：< 10ms（本地查询）
- ✅ 网络请求：< 500ms（正常网络）
- ✅ 数据同步：支持增量同步

**限制**：

- ⚠️ 大量数据查询可能需要分页
- ⚠️ 网络请求受网络状况影响

#### 7.2.4 兼容性边界

**向后兼容性**：

- ✅ Schema 变更通过迁移脚本支持
- ✅ API 变更通过版本控制支持

**API 兼容性**：

- ✅ Repository 接口保持稳定
- ✅ DataSource 接口保持稳定

---

## 8. 附录

### 8.1 相关文档

- [数据模型模块方案设计](../datastore-model/docs/myhub-datastore-model-infra-v1.0.md)
- [数据库模块方案设计](../datastore-database/docs/myhub-datastore-database-infra-v1.0.md)
- [数据库客户端模块方案设计](../datastore-database-client/docs/myhub-datastore-database-client-infra-v1.0.md)
- [数据库服务端模块方案设计](../datastore-database-server/docs/myhub-datastore-database-server-infra-v1.0.md)
- [数据库测试模块方案设计](../datastore-database-test/docs/myhub-datastore-database-test-infra-v1.0.md)
- [数据库管理模块方案设计](../datastore-database-manage/docs/myhub-datastore-database-manage-infra-v1.0.md)
- [本地数据源模块方案设计](../datastore-datasource-local/docs/myhub-datastore-datasource-local-infra-v1.0.md)
- [远程数据源模块方案设计](../datastore-datasource-remote/docs/myhub-datastore-datasource-remote-infra-v1.0.md)
- [数据仓库客户端模块方案设计](../datastore-repository-client/docs/myhub-datastore-repository-client-infra-v1.0.md)
- [数据仓库服务端模块方案设计](../datastore-repository-server/docs/myhub-datastore-repository-server-infra-v1.0.md)
- [Datastore README](../README.md)
- [MyHub 架构设计文档规范](../../../docs/infra/myhub-infra-rules.md)

### 8.2 参考资料

- [SQLDelight 官方文档](<https://cashapp.github.io/sqldelight/>)
- [Ktor Client 官方文档](<https://ktor.io/docs/client.html>)
- [kotlinx.serialization 官方文档](<https://kotlinlang.org/docs/serialization.html>)
- [Kotlin Flow 官方文档](<https://kotlinlang.org/docs/flow.html>)
- [Koin 官方文档](<https://insert-koin.io/>)
- [Clean Architecture](<https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html>)
- [Repository Pattern](<https://martinfowler.com/eaaCatalog/repository.html>)

### 8.3 术语表

- **Repository**：数据仓库，协调数据源，实现业务逻辑
- **DataSource**：数据源，封装数据访问细节
- **Database**：数据库，定义 Schema 和驱动
- **Domain Model**：领域模型，业务实体
- **DTO**：数据传输对象，用于网络传输
- **Schema**：数据库 Schema，定义表结构
- **Migration**：数据库迁移，Schema 变更时的数据迁移
- **Flow**：Kotlin Flow，响应式数据流
- **expect/actual**：KMP 平台抽象机制

---

**规则版本**：v1.0  
**最后更新**：2026-01-13  
**维护者**：MyHub Development Team
