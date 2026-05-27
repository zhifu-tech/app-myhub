# MyHub 数据仓库服务端模块方案设计

**方案名称**：Datastore Repository Server Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Repository Server Infra v1 的基线设计
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

在 MyHub 服务端应用的开发和运行过程中，需要提供统一的数据访问接口。典型的场景包括：

1. **数据访问抽象**：为 Service 层提供统一的数据访问接口
2. **用户隔离**：确保每个用户只能访问自己的数据
3. **业务逻辑封装**：封装数据访问的业务逻辑（如搜索、排序等）
4. **代码复用**：复用客户端的数据源实现，避免代码重复

### 1.2 问题根因

在引入统一的数据仓库模块之前，MyHub 服务端应用面临以下问题：

1. **数据访问逻辑分散**：各 Service 可能直接使用数据源，导致代码重复
2. **业务逻辑分散**：搜索、排序等业务逻辑分散在各处
3. **用户隔离缺失**：数据访问可能没有正确过滤用户
4. **代码重复**：服务端和客户端可能重复实现相同的数据访问逻辑

### 1.3 影响范围

- **代码维护**：数据访问逻辑分散，增加维护成本
- **数据安全**：用户隔离缺失可能导致数据泄露
- **代码复用**：缺乏代码复用机制

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的数据仓库接口**：为 Service 层提供统一的数据访问接口
- ✅ **代码复用**：复用客户端的数据源实现，避免代码重复
- ✅ **用户隔离**：所有数据操作都基于用户上下文进行隔离
- ✅ **业务逻辑封装**：封装搜索、排序等业务逻辑
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **服务端专用**：专为 JVM 服务端应用设计

### 2.2 非功能目标

- ✅ **代码复用**：复用客户端的数据源实现
- ✅ **易于测试**：提供清晰的接口，易于 Mock
- ✅ **性能优化**：直接使用本地数据源，性能最优

### 2.3 模块特性说明

**重要说明**：`datastore/repository-server` 模块是一个**单一功能模块**，专注于服务端数据仓库的实现。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据源复用：LocalDataSource

**选择理由**：

- ✅ **代码复用**：复用客户端的 `LocalDataSource` 实现，避免代码重复
- ✅ **架构一致性**：服务端和客户端使用相同的数据访问层
- ✅ **易于维护**：数据访问逻辑集中在一个地方

#### 3.1.2 用户上下文：UserContextProvider

**选择理由**：

- ✅ **用户隔离**：通过 `UserContextProvider` 获取当前用户ID
- ✅ **统一管理**：用户上下文统一管理
- ✅ **易于扩展**：可以轻松添加新的用户上下文提供者

### 3.2 架构模式

#### 3.2.1 Repository 模式

**设计原则**：

- **Repository 接口**：定义数据访问接口（基础接口）
- **RepositoryImpl**：实现数据访问逻辑，使用 `LocalDataSource`
- **用户隔离**：所有操作都基于用户上下文进行过滤

**优势**：

- ✅ **关注点分离**：数据访问逻辑与业务逻辑分离
- ✅ **易于测试**：接口清晰，易于 Mock
- ✅ **代码复用**：复用客户端的数据源实现

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/repository-server/
├── src/
│   ├── main/
│   │   └── kotlin/tech/zhifu/app/myhub/datastore/
│   │       ├── datasource/
│   │       │   └── impl/
│   │       │       └── UserContextProviderImpl.kt
│   │       └── repository/
│   │           ├── di/
│   │           │   └── RepositoryModule.kt      # Koin DI 模块
│   │           └── impl/
│   │               ├── CardRepositoryImpl.kt
│   │               ├── TagRepositoryImpl.kt
│   │               ├── TemplateRepositoryImpl.kt
│   │               ├── UserRepositoryImpl.kt
│   │               └── StatisticsRepositoryImpl.kt
│   └── test/
│       └── kotlin/.../
│           ├── CardRepositoryTest.kt
│           └── ...
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 CardRepositoryImpl（卡片仓库实现）

**功能**：

- 使用 `LocalCardDataSource` 实现数据访问
- 封装搜索、排序等业务逻辑
- 用户隔离

**主要方法**：

```kotlin
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
    private val userContextProvider: UserContextProvider
) : CardRepository {
    
    override suspend fun getAllCards(): List<Card>
    override suspend fun searchCards(filter: SearchFilter): List<Card>
    override suspend fun createCard(card: Card): Card
    // ...
}
```

**搜索实现**：

- 从本地数据源获取所有卡片
- 在内存中进行过滤和排序
- 支持多条件搜索（查询、类型、标签、收藏等）

#### 4.2.2 UserContextProviderImpl（用户上下文提供者实现）

**功能**：

- 从 `LocalUserDataSource` 获取当前用户ID
- 服务端特定的用户上下文管理

**实现**：

```kotlin
class UserContextProviderImpl(
    private val userDataSource: LocalUserDataSource
) : UserContextProvider {
    
    override fun getCurrentUserId(): String? {
        return runBlocking {
            userDataSource.getCurrentUser()?.id
        }
    }
}
```

#### 4.2.3 RepositoryModule（Koin DI 模块）

**功能**：

- 提供所有 Repository 的实现
- 包含本地数据源模块和数据库模块

**定义**：

```kotlin
val repositoryModule = module {
    includes(databaseModule)
    includes(localDataSourceModule)
    
    single<UserContextProvider> {
        UserContextProviderImpl(...)
    }
    
    single<CardRepository> {
        CardRepositoryImpl(...)
    }
    // ...
}
```

### 4.3 数据流

```
Service Layer
    ↓
Repository (CardRepository)
    ↓
UserContextProvider.getCurrentUserId()
    ↓
LocalDataSource (LocalCardDataSource)
    ↓
MyHubDatabase (SQLDelight)
    ↓
SQLite/PostgreSQL
```

---

## 5. 实现细节

### 5.1 数据访问实现

**示例：getAllCards**：

```kotlin
override suspend fun getAllCards(): List<Card> {
    val userId = requireUserId()
    return localDataSource.getAllCards(userId)
}
```

### 5.2 搜索实现

**示例：searchCards**：

```kotlin
override suspend fun searchCards(filter: SearchFilter): List<Card> {
    val userId = requireUserId()
    val allCards = localDataSource.getAllCards(userId)
    
    return allCards.filter { card ->
        // 查询匹配
        val matchesQuery = filter.query?.let { query ->
            query.isBlank() ||
                card.title?.contains(query, ignoreCase = true) == true ||
                card.content.contains(query, ignoreCase = true) ||
                card.author?.contains(query, ignoreCase = true) == true ||
                card.tags.any { it.contains(query, ignoreCase = true) }
        } ?: true
        
        // 类型过滤
        val matchesType = filter.cardTypes.isEmpty() || filter.cardTypes.contains(card.type)
        
        // 标签过滤
        val matchesTags = filter.tags.isEmpty() || filter.tags.all { card.tags.contains(it) }
        
        // 收藏过滤
        val matchesFavorite = filter.isFavorite?.let { card.isFavorite == it } ?: true
        
        matchesQuery && matchesType && matchesTags && matchesFavorite
    }.sortedWith(compareBy { card ->
        when (filter.sortBy) {
            SortBy.CREATED_AT_ASC -> card.createdAt.epochSeconds
            SortBy.CREATED_AT_DESC -> -card.createdAt.epochSeconds
            // ...
        }
    })
}
```

### 5.3 用户隔离实现

**示例：requireUserId**：

```kotlin
private suspend fun requireUserId(): String {
    return userContextProvider.getCurrentUserId()
        ?: throw IllegalStateException("User not authenticated")
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心仓库实现（已完成）

- ✅ CardRepositoryImpl 实现
- ✅ TagRepositoryImpl 实现
- ✅ TemplateRepositoryImpl 实现
- ✅ UserRepositoryImpl 实现
- ✅ StatisticsRepositoryImpl 实现

#### 阶段 2：用户上下文实现（已完成）

- ✅ UserContextProviderImpl 实现
- ✅ 用户隔离机制

#### 阶段 3：依赖注入集成（已完成）

- ✅ RepositoryModule 实现
- ✅ Koin 集成

### 6.2 里程碑

| 里程碑     | 目标日期       | 状态    |
|---------|------------|-------|
| 核心仓库完成  | 2026-01-13 | ✅ 已完成 |
| 用户上下文完成 | 2026-01-13 | ✅ 已完成 |
| 依赖注入完成  | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 用户隔离风险

**风险描述**：数据访问可能没有正确过滤用户

**影响**：高

**缓解措施**：

- ✅ 所有操作都通过 `requireUserId()` 获取用户ID
- ✅ 数据源层自动过滤用户数据
- ✅ 单元测试覆盖用户隔离场景

#### 7.1.2 搜索性能风险

**风险描述**：大量数据时内存搜索可能影响性能

**影响**：中

**缓解措施**：

- ✅ 使用数据库索引优化查询
- ✅ 分页支持（如需要）
- ✅ 性能监控

### 7.2 业务风险

#### 7.2.1 代码复用风险

**风险描述**：服务端和客户端代码可能不一致

**影响**：低

**缓解措施**：

- ✅ 复用客户端的数据源实现
- ✅ 统一的接口定义
- ✅ 代码审查

---

## 8. 附录

### 8.1 相关文档

- [MyHub 本地数据源模块方案设计](../datastore-datasource-local/docs/myhub-datastore-datasource-local-infra-v1.0.md)
- [MyHub 数据库服务端模块方案设计](../datastore-database-server/docs/myhub-datastore-database-server-infra-v1.0.md)
- [MyHub 数据仓库客户端模块方案设计](../datastore-repository-client/docs/myhub-datastore-repository-client-infra-v1.0.md)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
class CardService(
    private val cardRepository: CardRepository
) {
    suspend fun getAllCards(): List<Card> {
        return cardRepository.getAllCards()
    }
    
    suspend fun searchCards(query: String): List<Card> {
        val filter = SearchFilter(
            query = query,
            cardTypes = emptyList(),
            tags = emptyList()
        )
        return cardRepository.searchCards(filter)
    }
}
```

### 8.3 术语表

| 术语                  | 说明                     |
|---------------------|------------------------|
| Repository          | 数据仓库接口，封装数据访问逻辑        |
| UserContextProvider | 用户上下文提供者，获取当前用户ID      |
| 用户隔离                | 数据访问基于用户上下文进行过滤，确保数据安全 |
| 代码复用                | 复用客户端的数据源实现，避免代码重复     |

### 8.4 常见问题

#### Q1: 如何确保用户数据隔离？

**A**: 所有 Repository 实现都通过 `requireUserId()` 获取用户ID，数据源层自动过滤用户数据。

#### Q2: 为什么服务端不使用远程数据源？

**A**: 服务端直接访问数据库，不需要通过 HTTP API。复用客户端的 `LocalDataSource` 实现可以保持架构一致性。

#### Q3: 搜索性能如何优化？

**A**: 当前实现在内存中搜索，对于大量数据可以考虑使用数据库索引或分页。

---
