# MyHub 数据仓库客户端模块方案设计

**方案名称**：Datastore Repository Client Infra v1  
**文档版本**：v1.0  
**文档类型**：技术方案设计文档  
**创建日期**：2026-01-13  
**锁定日期**：2026-01-13  
**最后更新**：2026-01-23  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Repository Client Infra v1 的基线设计
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
| v1.0 | 2026-01-23 | 同步当前实现结构        | 落地实现更新 |

---

## 1. 问题背景

### 1.1 用户场景

在 MyHub 客户端应用的开发和运行过程中，需要协调本地和远程数据源。典型的场景包括：

1. **数据同步**：从服务器获取最新数据并同步到本地
2. **离线支持**：应用离线时仍能访问本地数据
3. **响应式更新**：数据变化时实时更新 UI
4. **数据一致性**：确保本地和远程数据的一致性

### 1.2 问题根因

在引入统一的数据仓库模块之前，MyHub 客户端应用面临以下问题：

1. **数据源协调分散**：各模块可能直接使用数据源，导致代码重复
2. **同步逻辑缺失**：缺乏统一的数据同步机制
3. **响应式更新缺失**：缺乏数据变化的实时监听机制
4. **离线支持不足**：离线时无法访问数据

### 1.3 影响范围

- **代码维护**：数据访问逻辑分散，增加维护成本
- **用户体验**：缺乏实时更新和离线支持导致用户体验差
- **数据一致性**：本地和远程数据可能不一致

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的数据仓库接口**：提供统一的接口协调本地和远程数据源
- ✅ **响应式数据流**：提供 `Flow` 支持，实现数据变更的实时监听
- ✅ **数据同步**：自动从远程同步数据到本地
- ✅ **离线支持**：离线时使用本地数据，在线时优先使用远程数据
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）

### 2.2 非功能目标

- ✅ **性能优化**：优先使用本地数据，减少网络请求
- ✅ **错误处理**：网络错误时自动降级到本地数据
- ✅ **易于测试**：提供清晰的接口，易于 Mock

### 2.3 模块特性说明

**重要说明**：`datastore/repository-client` 模块是一个**单一功能模块**，专注于客户端数据仓库的实现。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 响应式数据流：Kotlin Flow

**选择理由**：

- ✅ **KMP 原生支持**：Kotlin Flow 完全支持 KMP
- ✅ **协程集成**：与 Kotlin Coroutines 完美集成
- ✅ **实时更新**：支持数据变化的实时监听

#### 3.1.2 数据源协调：Repository 模式

**选择理由**：

- ✅ **关注点分离**：数据访问逻辑与业务逻辑分离
- ✅ **易于测试**：接口清晰，易于 Mock
- ✅ **易于扩展**：可以轻松添加新的数据源

### 3.2 架构模式

#### 3.2.1 Repository 模式

**设计原则**：

- **Repository 接口**：定义数据访问接口（基础接口 + 响应式接口）
- **RepositoryImpl**：实现数据访问逻辑，协调本地和远程数据源
- **数据同步策略**：优先使用远程数据，失败时降级到本地数据

**优势**：

- ✅ **关注点分离**：数据访问逻辑与业务逻辑分离
- ✅ **易于测试**：接口清晰，易于 Mock
- ✅ **易于扩展**：可以轻松添加新的数据源

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/repository-client/
├── src/
│   ├── commonMain/
│   │   └── kotlin/tech/zhifu/app/myhub/datastore/repository/
│   │       ├── di/
│   │       │   └── RepositoryModule.kt      # Koin DI 模块
│   │       ├── SyncChangeApplier.kt
│   │       └── impl/
│   │           ├── CardRepositoryImpl.kt
│   │           ├── TagRepositoryImpl.kt
│   │           ├── CollectionRepositoryImpl.kt
│   │           ├── CardTemplateRepositoryImpl.kt
│   │           ├── UserRepositoryImpl.kt
│   │           └── SyncRepositoryImpl.kt
│   └── commonTest/
│       └── kotlin/.../
│           ├── CardRepositoryTest.kt
│           └── ...
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 CardRepositoryImpl（卡片仓库实现）

**功能**：

- 协调本地和远程数据源
- 提供自动补齐标签的标准插卡 API（`insertCardWithTags`）
- 数据同步和离线支持（Outbox + OpLog）

**主要方法**：

```kotlin
interface CardRepository {
    suspend fun insertCardWithTags(card: Card, needSync: Boolean = true)
}
```

**数据同步策略**：

- **读取**：优先从远程获取，失败时使用本地数据
- **写入**：先保存到本地，然后同步到远程
- **响应式**：监听本地数据变化，实时更新 UI

#### 4.2.2 TagRepositoryImpl（标签仓库实现）

**功能**：

- 协调本地和远程标签数据源
- 同步变更通过 `SyncChangeApplier` 统一接入

#### 4.2.3 RepositoryModule（Koin DI 模块）

**功能**：

- 提供所有 Repository 的实现
- 包含本地和远程数据源模块

**定义**：

```kotlin
val repositoryModule = module {
    includes(
        databaseModule,
        localDataSourceModule,
        remoteDataSourceModule
    )

    single<CardRepository> { CardRepositoryImpl(...) }
    single<TagRepository> { TagRepositoryImpl(...) }
    single<CollectionRepository> { CollectionRepositoryImpl(...) }
    single<CardTemplateRepository> { CardTemplateRepositoryImpl(...) }
    single<UserRepository> { UserRepositoryImpl(...) }
    single<SyncRepository> { SyncRepositoryImpl(...) }
    // ...
}
```

### 4.3 数据流

#### 4.3.1 读取数据流

```text
业务代码
    ↓
Repository (CardRepository)
    ↓
优先：RemoteDataSource → 保存到 LocalDataSource → 返回数据
降级：LocalDataSource → 返回数据
```

#### 4.3.2 写入数据流

```text
业务代码
    ↓
Repository (CardRepository)
    ↓
LocalDataSource.insertCard() → 同步记录写入 Outbox/OpLog
    ↓
RemoteDataSource.createCard() → 更新 LocalDataSource
```

#### 4.3.3 响应式数据流

```text
业务代码
    ↓
Repository.observeCards()
    ↓
LocalDataSource.observeCards() → Flow<List<Card>>
    ↓
UI 实时更新
```

---

## 5. 实现细节

### 5.1 数据同步实现

**示例：getAllCards**：

```kotlin
override suspend fun getAllCards(): List<Card> {
    val userId = requireUserId()
    // 优先从远程获取最新数据
    return try {
        val remoteCards = remoteDataSource.getAllCards().map { it.toDomain() }

        // 保存到本地
        remoteCards.forEach { card ->
            localDataSource.insertCard(card, userId)
        }
        remoteCards
    } catch (e: Exception) {
        // 如果远程获取失败，返回本地数据（降级处理）
        localDataSource.getAllCards(userId)
    }
}
```

### 5.2 响应式数据流实现

**示例：observeAllCards**：

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
override fun observeAllCards(): Flow<List<Card>> {
    return userDataSource.observeUser().flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            localDataSource.observeCards(user.id)
        }
    }
}
```

### 5.3 数据写入实现

**示例：createCard**：

```kotlin
override suspend fun createCard(card: Card): Card {
    val userId = requireUserId()
    // 先保存到本地
    localDataSource.insertCard(card, userId)

    // 然后同步到远程
    val request = card.toDto().let { dto ->
        CreateCardRequest(...)
    }
    val remoteCard = remoteDataSource.createCard(request).toDomain()

    // 更新本地数据（使用服务器返回的数据）
    localDataSource.updateCard(remoteCard, userId)

    return remoteCard
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

#### 阶段 2：响应式接口支持（已完成）

- ✅ ReactiveCardRepository 实现
- ✅ ReactiveTagRepository 实现
- ✅ ReactiveTemplateRepository 实现
- ✅ ReactiveUserRepository 实现
- ✅ ReactiveStatisticsRepository 实现

#### 阶段 3：依赖注入集成（已完成）

- ✅ RepositoryModule 实现
- ✅ Koin 集成

### 6.2 里程碑

| 里程碑     | 目标日期       | 状态    |
|---------|------------|-------|
| 核心仓库完成  | 2026-01-13 | ✅ 已完成 |
| 响应式支持完成 | 2026-01-13 | ✅ 已完成 |
| 依赖注入完成  | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 数据同步冲突风险

**风险描述**：本地和远程数据可能不一致，导致同步冲突

**影响**：中

**缓解措施**：

- ✅ 使用服务器返回的数据更新本地数据
- ✅ 提供数据冲突解决机制
- ✅ 文档说明同步策略

#### 7.1.2 网络错误处理风险

**风险描述**：网络错误时可能无法正常降级到本地数据

**影响**：高

**缓解措施**：

- ✅ 统一的错误处理机制
- ✅ 自动降级到本地数据
- ✅ 错误日志记录

### 7.2 业务风险

#### 7.2.1 数据一致性风险

**风险描述**：本地和远程数据可能不一致

**影响**：中

**缓解措施**：

- ✅ 优先使用远程数据
- ✅ 写入时同步更新本地和远程
- ✅ 提供数据同步机制

---

## 8. 附录

### 8.1 相关文档

- [MyHub 本地数据源模块方案设计](../datastore-datasource-local/docs/myhub-datastore-datasource-local-infra-v1.0.md)
- [MyHub 远程数据源模块方案设计](../datastore-datasource-remote/docs/myhub-datastore-datasource-remote-infra-v1.0.md)
- [MyHub 数据模型模块方案设计](../datastore-model/docs/myhub-datastore-model-infra-v1.0.md)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
class CardViewModel(
    private val cardRepository: ReactiveCardRepository
) : ViewModel() {

    val cards: Flow<List<Card>> = cardRepository.observeAllCards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadCards() {
        viewModelScope.launch {
            cardRepository.getAllCards()
        }
    }
}
```

### 8.3 术语表

| 术语                 | 说明                     |
|--------------------|------------------------|
| Repository         | 数据仓库接口，协调本地和远程数据源      |
| ReactiveRepository | 响应式数据仓库接口，支持 Flow 实时监听 |
| 数据同步               | 从远程获取最新数据并同步到本地        |
| 离线支持               | 离线时使用本地数据，在线时优先使用远程数据  |

### 8.4 常见问题

#### Q1: 如何确保数据同步？

**A**: Repository 优先从远程获取数据，失败时自动降级到本地数据。写入时先保存到本地，然后同步到远程。

#### Q2: 如何监听数据变化？

**A**: 使用 `observe*` 方法返回 `Flow`，可以实时监听数据变化。

---
