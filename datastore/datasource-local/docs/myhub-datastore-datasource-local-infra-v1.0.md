# MyHub 本地数据源模块方案设计

**方案名称**：Datastore Datasource Local Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Datasource Local Infra v1 的基线设计
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
- 详细状态定义请参考 [MyHub 架构设计文档规范](../../../docs/myhub-infra-rules.md)

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

在 MyHub 应用的开发和运行过程中，需要处理本地数据存储。典型的场景包括：

1. **数据 CRUD 操作**：对卡片、标签、模板等数据进行增删改查
2. **数据实时监听**：监听数据变化并更新 UI
3. **用户数据隔离**：确保每个用户只能访问自己的数据
4. **离线数据访问**：应用离线时仍能访问本地数据

### 1.2 问题根因

在引入统一的本地数据源模块之前，MyHub 应用面临以下问题：

1. **数据访问逻辑分散**：各模块可能直接使用数据库，导致代码重复
2. **用户隔离缺失**：数据访问可能没有正确过滤用户
3. **响应式更新缺失**：缺乏数据变化的实时监听机制
4. **类型安全问题**：直接使用 SQL 查询缺乏类型安全

### 1.3 影响范围

- **代码维护**：数据访问逻辑分散，增加维护成本
- **数据安全**：用户隔离缺失可能导致数据泄露
- **用户体验**：缺乏实时更新导致用户体验差

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的本地数据源接口**：提供统一的接口访问本地数据
- ✅ **SQLDelight 集成**：使用 SQLDelight 进行类型安全的数据库操作
- ✅ **响应式数据流**：提供 `Flow` 支持，实现数据变更的实时监听
- ✅ **用户隔离**：所有数据操作都基于 `userId` 进行隔离
- ✅ **依赖注入**：提供 Koin DI 模块，便于集成
- ✅ **跨平台支持**：支持所有平台（Android、iOS、JVM、JS、WASM）

### 2.2 非功能目标

- ✅ **类型安全**：使用 Kotlin 类型系统确保数据操作的类型安全
- ✅ **性能优化**：使用 SQLDelight 的编译时查询优化
- ✅ **易于测试**：提供清晰的接口，易于测试

### 2.3 模块特性说明

**重要说明**：`datastore/datasource-local` 模块是一个**单一功能模块**，专注于本地数据源的实现。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 数据库访问：SQLDelight

**选择理由**：

- ✅ **类型安全**：编译时生成类型安全的查询接口
- ✅ **KMP 原生支持**：完全支持 KMP
- ✅ **性能优化**：编译时查询优化

#### 3.1.2 响应式数据流：Kotlin Flow

**选择理由**：

- ✅ **KMP 原生支持**：Kotlin Flow 完全支持 KMP
- ✅ **协程集成**：与 Kotlin Coroutines 完美集成
- ✅ **实时更新**：支持数据变化的实时监听

### 3.2 架构模式

#### 3.2.1 Repository 模式

**设计原则**：

- **DataSource 接口**：定义数据访问接口
- **DataSourceImpl**：实现数据访问逻辑
- **用户隔离**：所有操作都基于 `userId` 进行过滤

**优势**：

- ✅ **关注点分离**：数据访问逻辑与业务逻辑分离
- ✅ **易于测试**：接口清晰，易于 Mock
- ✅ **易于扩展**：可以轻松添加新的数据源实现

---

## 4. 架构设计

### 4.1 模块结构

```text
datastore/datasource-local/
├── src/
│   ├── commonMain/
│   │   └── kotlin/tech/zhifu/app/myhub/datastore/datasource/
│   │       ├── LocalDataSource.kt              # 数据源接口定义
│   │       ├── UserContextProvider.kt         # 用户上下文提供者接口
│   │       ├── di/
│   │       │   └── LocalDataSourceModule.kt    # Koin DI 模块
│   │       └── impl/
│   │           ├── LocalCardDataSourceImpl.kt
│   │           ├── LocalTagDataSourceImpl.kt
│   │           ├── LocalTemplateDataSourceImpl.kt
│   │           ├── LocalUserDataSourceImpl.kt
│   │           ├── LocalStatisticsDataSourceImpl.kt
│   │           └── UserContextProviderImpl.kt
│   └── commonTest/
│       └── kotlin/.../
│           ├── LocalCardDataSourceTest.kt
│           └── ...
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 LocalCardDataSource（卡片数据源）

**接口**：

```kotlin
interface LocalCardDataSource {
    suspend fun getAllCards(userId: String): List<Card>
    suspend fun getCardById(id: String, userId: String): Card?
    suspend fun insertCard(card: Card, userId: String)
    suspend fun updateCard(card: Card, userId: String)
    suspend fun deleteCard(id: String, userId: String)
    suspend fun deleteAllCards(userId: String)
    fun observeCards(userId: String): Flow<List<Card>>
}
```

**实现**：`LocalCardDataSourceImpl`

#### 4.2.2 LocalTagDataSource（标签数据源）

**接口**：

```kotlin
interface LocalTagDataSource {
    suspend fun getAllTags(userId: String): List<Tag>
    suspend fun getTagById(id: String, userId: String): Tag?
    suspend fun getTagByName(name: String, userId: String): Tag?
    suspend fun insertTag(tag: Tag, userId: String)
    suspend fun updateTag(tag: Tag, userId: String)
    suspend fun deleteTag(id: String, userId: String)
    fun observeTags(userId: String): Flow<List<Tag>>
}
```

**实现**：`LocalTagDataSourceImpl`

#### 4.2.3 UserContextProvider（用户上下文提供者）

**接口**：

```kotlin
interface UserContextProvider {
    fun getCurrentUserId(): String?
}
```

**功能**：

- 提供当前用户 ID
- 如果用户未登录则返回 `null`

---

## 5. 实现细节

### 5.1 数据访问实现

**示例：LocalCardDataSourceImpl**：

```kotlin
class LocalCardDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCardDataSource {
    
    override suspend fun getAllCards(userId: String): List<Card> {
        return database.cardQueries.selectAll(userId)
            .awaitAsList()
            .map { it.toDomain() }
    }
    
    override fun observeCards(userId: String): Flow<List<Card>> {
        return database.cardQueries.selectAll(userId)
            .asFlow()
            .mapToList()
            .map { it.map { row -> row.toDomain() } }
    }
}
```

### 5.2 响应式数据流

**实现**：

```kotlin
fun observeCards(userId: String): Flow<List<Card>> {
    return database.cardQueries.selectAll(userId)
        .asFlow()
        .mapToList()
        .map { it.map { row -> row.toDomain() } }
}
```

**使用**：

```kotlin
class CardViewModel(
    private val cardDataSource: LocalCardDataSource,
    private val userId: String
) {
    val cards: Flow<List<Card>> = cardDataSource.observeCards(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

### 5.3 用户隔离

**所有查询都添加用户过滤**：

```kotlin
override suspend fun getAllCards(userId: String): List<Card> {
    return database.cardQueries.selectAll(userId)  // 添加 userId 过滤
        .awaitAsList()
        .map { it.toDomain() }
}
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心数据源实现（已完成）

- ✅ LocalCardDataSource 实现
- ✅ LocalTagDataSource 实现
- ✅ LocalTemplateDataSource 实现
- ✅ LocalUserDataSource 实现
- ✅ LocalStatisticsDataSource 实现

#### 阶段 2：响应式数据流支持（已完成）

- ✅ Flow 支持实现
- ✅ 数据变化监听

#### 阶段 3：依赖注入集成（已完成）

- ✅ LocalDataSourceModule 实现
- ✅ Koin 集成

### 6.2 里程碑

| 里程碑     | 目标日期       | 状态    |
|---------|------------|-------|
| 核心数据源完成 | 2026-01-13 | ✅ 已完成 |
| 响应式支持完成 | 2026-01-13 | ✅ 已完成 |
| 依赖注入完成  | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 用户隔离风险

**风险描述**：数据访问可能没有正确过滤用户

**影响**：高

**缓解措施**：

- ✅ 所有查询都添加 `userId` 过滤
- ✅ 单元测试覆盖用户隔离场景
- ✅ 代码审查

#### 7.1.2 性能风险

**风险描述**：大量数据查询可能影响性能

**影响**：中

**缓解措施**：

- ✅ 使用 SQLDelight 的查询优化
- ✅ 添加适当的索引
- ✅ 分页查询支持

### 7.2 维护风险

#### 7.2.1 接口变更风险

**风险描述**：接口变更可能影响使用方

**影响**：低

**缓解措施**：

- ✅ 版本化接口
- ✅ 向后兼容
- ✅ 充分的文档

---

## 8. 附录

### 8.1 相关文档

- [MyHub 数据库模块方案设计](../datastore-database/docs/myhub-datastore-database-infra-v1.0.md)
- [MyHub 数据模型模块方案设计](../datastore-model/docs/myhub-datastore-model-infra-v1.0.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)

### 8.2 代码示例

#### 8.2.1 基本使用

```kotlin
class CardRepository(
    private val cardDataSource: LocalCardDataSource
) {
    suspend fun getAllCards(userId: String): List<Card> {
        return cardDataSource.getAllCards(userId)
    }
    
    fun observeCards(userId: String): Flow<List<Card>> {
        return cardDataSource.observeCards(userId)
    }
}
```

### 8.3 术语表

| 术语              | 说明                          |
|-----------------|-----------------------------|
| LocalDataSource | 本地数据源接口，负责本地数据存储访问          |
| Flow            | Kotlin 响应式数据流，支持数据变化的实时监听   |
| 用户隔离            | 数据访问基于 `userId` 进行过滤，确保数据安全 |

### 8.4 常见问题

#### Q1: 如何确保用户数据隔离？

**A**: 所有数据访问方法都要求提供 `userId` 参数，查询时自动过滤用户数据。

#### Q2: 如何监听数据变化？

**A**: 使用 `observe*` 方法返回 `Flow`，可以实时监听数据变化。

---
