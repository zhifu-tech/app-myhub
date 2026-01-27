# Repository-Server 功能增强计划

> 服务端 Repository 设计原则和实施计划

## 📋 执行摘要

本文档定义了 `repository-server` 模块的设计原则和实施计划。服务端 Repository 的设计**不强制与客户端统一**，而是依据实际需求实现。服务端提供两种数据访问方式：**实时 API 访问**和**同步（Sync）访问**。

---

## 🎯 一、设计原则

### 1.1 核心设计决策

#### ✅ 决策 1：服务端不需要响应式流（Flow）

**决定**：服务端 Repository **不需要响应式流（Flow）**。

**理由**：

- 服务端是数据存储层，不需要实时数据流
- 客户端通过 API 请求获取数据，每次请求都是独立的
- 简化实现，避免不必要的复杂度

**影响**：

- 所有 Repository 接口不包含 `Flow` 返回类型的方法
- 不实现 `stream*` 和 `fetch*` 等响应式方法
- 使用简单的 `suspend fun` 返回同步数据

---

### 1.2 数据访问方式

服务端提供**两种数据访问方式**：

#### 方式 1：实时 API 访问

**描述**：用户通过 HTTP API 请求直接访问数据。

**特点**：

- 每次请求都是独立的
- 立即返回当前数据
- 适用于实时查询、创建、更新、删除操作

**示例**：

**CardRepository API**：

```
GET  /api/cards/fetchCards?userId={userId}  - 获取用户所有卡片
GET  /api/cards/fetchCard?cardId={id}       - 获取指定卡片
POST /api/cards/upsertCard                  - 创建或更新卡片
DELETE /api/cards/deleteCard?cardId={id}    - 删除卡片
```

**TagRepository API**：

```
GET  /api/tags/fetchTags?userId={userId}    - 获取用户所有标签
GET  /api/tags/fetchTag?tagId={id}          - 获取指定标签
POST /api/tags/upsertTag                    - 创建或更新标签
DELETE /api/tags/deleteTag?tagId={id}       - 删除标签
```

**CollectionRepository API**：

```
GET  /api/collections/fetchCollections?userId={userId}  - 获取用户所有集合
GET  /api/collections/fetchCollection?collectionId={id} - 获取指定集合
POST /api/collections/upsertCollection                  - 创建或更新集合
DELETE /api/collections/deleteCollection?collectionId={id} - 删除集合
```

**CardTemplateRepository API**：

```
GET  /api/templates/fetchTemplates           - 获取所有模板
GET  /api/templates/fetchTemplate?templateId={id} - 获取指定模板
POST /api/templates/upsertTemplate           - 创建或更新模板
DELETE /api/templates/deleteTemplate?templateId={id} - 删除模板
```

**UserRepository API**：

```
GET  /api/users/fetchUser?userId={id}       - 获取指定用户
POST /api/users/upsertUser                   - 创建或更新用户
DELETE /api/users/deleteUser?userId={id}    - 删除用户
```

**实现层次**：

```
API 路由 (CardsApi)
    ↓
Service 层 (CardService)
    ↓
Repository 层 (CardRepository)
    ↓
DataSource 层 (LocalCardDataSource)
    ↓
Database (SQLDelight)
```

#### 方式 2：同步（Sync）访问

**描述**：用户触发 sync 请求，同步所有数据操作。

**特点**：

- 批量处理数据变更
- 支持增量同步（基于 sinceToken）
- 客户端已实现，服务端提供同步接口

**示例**：

```
POST /api/sync/push  - 推送本地变更到服务端
GET  /api/sync/pull  - 从服务端拉取变更
```

**实现层次**：

```
API 路由 (SyncApi)
    ↓
Service 层 (SyncService)
    ↓
Repository 层 (SyncRepository)
    ↓
DataSource 层 (LocalSyncDataSource)
    ↓
Database (SQLDelight)
```

---

### 1.3 Repository 设计原则

#### 原则 1：不强制与客户端统一

**决定**：服务端的 Repository 接口和实现**不强制与客户端统一**，依据实际需求实现。

**理由**：

- 服务端和客户端的使用场景不同
- 服务端不需要响应式流、缓存管理等客户端特性
- 保持接口简洁，只包含必要的功能

**影响**：

- 服务端 Repository 接口可以不同于客户端
- 方法签名和返回类型依据实际需求定义
- 不需要实现客户端特有的功能（如 `clear*`、`stream*` 等）

#### 原则 2：依据实际需求实现

**决定**：每个 Repository 的实现依据实际业务需求，不追求功能完整性。

**实施方式**：

- 分析 Service 层的实际需求
- 只实现 Service 层需要的方法
- 不需要实现所有可能的 CRUD 操作

---

## 📊 二、当前状态分析

### 2.1 已实现的 Repository

| Repository     | 接口定义 | 实现状态    | 说明                |
|----------------|------|---------|-------------------|
| CardRepository | ✅    | ⚠️ 部分实现 | 只有查询和删除，缺少插入      |
| UserRepository | ✅    | ⚠️ 部分实现 | 只有 getUserById    |
| SyncRepository | ✅    | ✅ 完整实现  | push 和 pull 方法已实现 |

### 2.2 缺失的 Repository

| Repository             | 接口定义 | 实现状态 | 是否需要                    |
|------------------------|------|------|-------------------------|
| TagRepository          | ❌    | ❌    | 确认（CardRepository 可能需要） |
| CollectionRepository   | ❌    | ❌    | 确认                      |
| CardTemplateRepository | ❌    | ❌    | 确认                      |

---

## 🔍 三、需求分析

### 3.1 实时 API 访问需求

#### CardRepository 需求

**当前 API**：

- `GET /api/cards/fetchCards?userId={userId}` - 获取用户所有卡片
- `GET /api/cards/fetchCard?cardId={id}` - 获取指定卡片

**缺失的 API**（确认）：

- `POST /api/cards/upsertCard` - 创建或更新卡片
- `DELETE /api/cards/deleteCard?cardId={id}` - 删除卡片

**对应的 Repository 方法**：

- ✅ `getCards(userId: String): List<Card>` - 已实现
- ✅ `getCard(cardId: String): Card?` - 已实现
- ✅ `deleteCard(cardId: String)` - 已实现
- ❌ `upsertCard(card: Card)` - **需要实现**

#### UserRepository 需求

**当前 API**：

- `GET /api/users/fetchUser?userId={id}` - 获取用户信息

**缺失的 API**（确认）：

- `POST /api/users/upsertUser` - 创建或更新用户
- `DELETE /api/users/deleteUser?userId={id}` - 删除用户

**对应的 Repository 方法**：

- ✅ `getUserById(userId: String): User?` - 已实现
- ❌ `upsertUser(user: User)` - **确认**
- ❌ `deleteUser(userId: String)` - **确认**

#### TagRepository 需求

**是否需要**：确认

**如果 CardRepository.upsertCard 需要自动创建标签，则需要**：

- `ensureTags(userId: String, tags: List<Tag>): List<Tag>` - 确保标签存在

**如果提供完整的标签管理功能，则需要以下 API**：

- `GET /api/tags/fetchTags?userId={userId}` - 获取用户所有标签
- `GET /api/tags/fetchTag?tagId={id}` - 获取指定标签
- `POST /api/tags/upsertTag` - 创建或更新标签
- `DELETE /api/tags/deleteTag?tagId={id}` - 删除标签

**对应的 Repository 方法**（如果需要）：

- ❌ `getTags(userId: String): List<Tag>` - 获取用户所有标签
- ❌ `getTag(tagId: String): Tag?` - 获取指定标签
- ❌ `upsertTag(tag: Tag)` - 创建或更新标签
- ❌ `deleteTag(tagId: String)` - 删除标签
- ❌ `ensureTags(userId: String, tags: List<Tag>): List<Tag>` - 确保标签存在

#### CollectionRepository 需求

**是否需要**：确认

**如果提供集合管理功能，则需要以下 API**：

- `GET /api/collections/fetchCollections?userId={userId}` - 获取用户所有集合
- `GET /api/collections/fetchCollection?collectionId={id}` - 获取指定集合
- `POST /api/collections/upsertCollection` - 创建或更新集合
- `DELETE /api/collections/deleteCollection?collectionId={id}` - 删除集合

**对应的 Repository 方法**（如果需要）：

- ❌ `getCollections(userId: String): List<Collection>` - 获取用户所有集合
- ❌ `getCollection(collectionId: String): Collection?` - 获取指定集合
- ❌ `upsertCollection(collection: Collection)` - 创建或更新集合
- ❌ `deleteCollection(collectionId: String)` - 删除集合

#### CardTemplateRepository 需求

**是否需要**：确认

**如果提供模板管理功能，则需要以下 API**：

- `GET /api/templates/fetchTemplates` - 获取所有模板
- `GET /api/templates/fetchTemplate?templateId={id}` - 获取指定模板
- `POST /api/templates/upsertTemplate` - 创建或更新模板
- `DELETE /api/templates/deleteTemplate?templateId={id}` - 删除模板

**对应的 Repository 方法**（如果需要）：

- ❌ `getTemplates(): List<CardTemplate>` - 获取所有模板
- ❌ `getTemplate(templateId: String): CardTemplate?` - 获取指定模板
- ❌ `upsertTemplate(template: CardTemplate)` - 创建或更新模板
- ❌ `deleteTemplate(templateId: String)` - 删除模板

---

### 3.2 同步（Sync）访问需求

#### SyncRepository 需求

**当前实现**：

- ✅ `push(request: SyncPushRequest): SyncPushResponse` - 已实现
- ✅ `pull(userId: String, entityType: String, sinceToken: String?, limit: Int): SyncPullResponse` - 已实现

**说明**：

- SyncRepository 的接口与客户端不同，这是**设计上的差异**，不是缺失
- 服务端的 SyncRepository 是"同步服务"，客户端的 SyncRepository 是"同步协调器"
- 当前实现已满足同步需求

---

## 🎯 四、实施计划

### 4.1 设计阶段

#### 任务 1：分析 Service 层需求

**目标**：确定每个 Service 需要哪些 Repository 方法。

**步骤**：

1. 查看所有 Service 类（CardService, UserService, SyncService 等）
2. 列出每个 Service 调用的 Repository 方法
3. 确认缺失的方法

**输出**：

- Service 层需求清单
- Repository 方法需求清单

#### 任务 2：设计 Repository 接口

**目标**：为每个 Repository 设计接口，只包含实际需要的方法。

**原则**：

- 不包含 Flow 返回类型
- 不包含客户端特有的方法（如 `clear*`、`stream*`）
- 方法签名简洁明了
- 返回类型使用简单的 Domain Model（如 `Card`、`List<Card>`）

**步骤**：

1. 为每个 Repository 定义接口（在 `repository-server-api` 模块）
2. 只包含 Service 层需要的方法
3. 不追求与客户端接口一致

---

### 4.2 实现阶段

#### 任务 3：实现 CardRepository

**需要实现的方法**（依据 Service 层需求）：

- [ ] `upsertCard(card: Card)` - 创建或更新卡片
    - [ ] 如果需要自动创建标签，调用 `tagRepository.ensureTags`
    - [ ] 调用 `localDataSource.upsertCard` 保存或更新到数据库

**设计考虑**：

- 服务端是数据源，不需要同步标记
- 是否需要自动创建标签？确认业务需求

#### 任务 4：实现 TagRepository（如果需要）

**前提条件**：确认 CardRepository.upsertCard 是否需要自动创建标签。

**如果需要，实现**：

- [ ] `ensureTags(userId: String, tags: List<Tag>): List<Tag>` - 确保标签存在
    - [ ] 查询现有标签
    - [ ] 创建不存在的标签
    - [ ] 返回所有标签（包括新创建的）

#### 任务 5：完善 UserRepository（如果需要）

**确认**：

- Service 层需要 `upsertUser` 和 `deleteUser`

**如果需要，实现**：

- [ ] `upsertUser(user: User)` - 创建或更新用户
- [ ] `deleteUser(userId: String)` - 删除用户

#### 任务 6：实现其他 Repository（如果需要）

**确认**：

- 需要 CollectionRepository
- 需要 CardTemplateRepository

**如果确认需要，则**：

- [ ] 在 `repository-server-api` 中定义接口
- [ ] 在 `repository-server` 中实现
- [ ] 只实现 Service 层需要的方法

---

### 4.3 集成阶段

#### 任务 7：更新依赖注入

**步骤**：

1. 更新 `RepositoryModule`，添加新实现的 Repository
2. 确保所有依赖关系正确注入

#### 任务 8：更新 Service 层

**步骤**：

1. 更新 Service 类，使用新实现的 Repository 方法
2. 实现对应的 API 路由（如果需要）

#### 任务 9：测试验证

**步骤**：

1. 测试实时 API 访问
2. 测试同步访问
3. 验证数据正确性

---

## 📝 五、具体工作清单

### 5.1 接口定义工作

#### 工作项 1：CardRepository 接口扩展

**位置**：`datastore/repository-server-api/src/main/kotlin/.../CardRepository.kt`

**需要添加的方法**：

- `upsertCard(card: Card)` - 创建或更新卡片

**设计要点**：

- 不包含 `needSync` 参数（服务端是数据源）
- 返回类型使用 `Card` 或 `Unit`
- 不包含 Flow 返回类型

#### 工作项 3：TagRepository 接口定义（如果需要）

**位置**：`datastore/repository-server-api/src/main/kotlin/.../TagRepository.kt`

**需要定义的方法**（依据实际需求）：

- `getTags(userId: String): List<Tag>` - 获取用户所有标签
- `getTag(tagId: String): Tag?` - 获取指定标签
- `upsertTag(tag: Tag)` - 创建或更新标签
- `deleteTag(tagId: String)` - 删除标签
- `ensureTags(userId: String, tags: List<Tag>): List<Tag>` - 确保标签存在（如果 CardRepository 需要）

**设计要点**：

- 只包含实际需要的方法
- 不追求与客户端接口一致

#### 工作项 4：CollectionRepository 接口定义（如果需要）

**位置**：`datastore/repository-server-api/src/main/kotlin/.../CollectionRepository.kt`

**需要定义的方法**（依据实际需求）：

- `getCollections(userId: String): List<Collection>` - 获取用户所有集合
- `getCollection(collectionId: String): Collection?` - 获取指定集合
- `upsertCollection(collection: Collection)` - 创建或更新集合
- `deleteCollection(collectionId: String)` - 删除集合

**设计要点**：

- 只包含实际需要的方法
- 不追求与客户端接口一致

#### 工作项 5：CardTemplateRepository 接口定义（如果需要）

**位置**：`datastore/repository-server-api/src/main/kotlin/.../CardTemplateRepository.kt`

**需要定义的方法**（依据实际需求）：

- `getTemplates(): List<CardTemplate>` - 获取所有模板
- `getTemplate(templateId: String): CardTemplate?` - 获取指定模板
- `upsertTemplate(template: CardTemplate)` - 创建或更新模板
- `deleteTemplate(templateId: String)` - 删除模板

**设计要点**：

- 只包含实际需要的方法
- 不追求与客户端接口一致

#### 工作项 6：UserRepository 接口扩展（如果需要）

**位置**：`datastore/repository-server-api/src/main/kotlin/.../UserRepository.kt`

**需要添加的方法**（依据实际需求）：

- `upsertUser(user: User)` - 创建或更新用户
- `deleteUser(userId: String)` - 删除用户

**设计要点**：

- 只包含实际需要的方法
- 不追求与客户端接口一致

---

### 5.2 实现工作

#### 工作项 7：CardRepositoryImpl 实现

**位置**：`datastore/repository-server/src/main/kotlin/.../CardRepositoryImpl.kt`

**需要实现的方法**：

- `upsertCard(card: Card)`
    - 如果需要，调用 `tagRepository.ensureTags` 确保标签存在
    - 调用 `localDataSource.upsertCard` 保存或更新到数据库

**设计要点**：

- 直接使用 LocalDataSource，不需要 RemoteDataSource
- 不需要同步逻辑（服务端是数据源）
- 实现简洁，只包含必要的业务逻辑

#### 工作项 8：TagRepositoryImpl 实现（如果需要）

**位置**：`datastore/repository-server/src/main/kotlin/.../TagRepositoryImpl.kt`

**需要实现的方法**：

- `ensureTags(userId: String, tags: List<Tag>): List<Tag>`
    - 查询现有标签（通过 LocalTagDataSource）
    - 创建不存在的标签
    - 返回所有标签（包括新创建的）

**设计要点**：

- 使用 LocalTagDataSource 查询和插入
- 实现去重逻辑（避免重复创建）

#### 工作项 9：CollectionRepositoryImpl 实现（如果需要）

**位置**：`datastore/repository-server/src/main/kotlin/.../CollectionRepositoryImpl.kt`

**需要实现的方法**（依据实际需求）：

- `getCollections(userId: String): List<Collection>`
    - 调用 `localDataSource.getCollections(userId)` 查询数据库
- `getCollection(collectionId: String): Collection?`
    - 调用 `localDataSource.getCollection(collectionId)` 查询数据库
- `upsertCollection(collection: Collection)`
    - 调用 `localDataSource.upsertCollection(collection)` 保存或更新到数据库
- `deleteCollection(collectionId: String)`
    - 调用 `localDataSource.deleteCollection(collectionId)` 删除数据

**设计要点**：

- 直接使用 LocalDataSource，不需要 RemoteDataSource
- 不需要同步逻辑（服务端是数据源）
- 实现简洁，只包含必要的业务逻辑

#### 工作项 10：CardTemplateRepositoryImpl 实现（如果需要）

**位置**：`datastore/repository-server/src/main/kotlin/.../CardTemplateRepositoryImpl.kt`

**需要实现的方法**（依据实际需求）：

- `getTemplates(): List<CardTemplate>`
    - 调用 `localDataSource.getTemplates()` 查询数据库
- `getTemplate(templateId: String): CardTemplate?`
    - 调用 `localDataSource.getTemplate(templateId)` 查询数据库
- `upsertTemplate(template: CardTemplate)`
    - 调用 `localDataSource.upsertTemplate(template)` 保存或更新到数据库
- `deleteTemplate(templateId: String)`
    - 调用 `localDataSource.deleteTemplate(templateId)` 删除数据

**设计要点**：

- 直接使用 LocalDataSource，不需要 RemoteDataSource
- 不需要同步逻辑（服务端是数据源）
- 实现简洁，只包含必要的业务逻辑

#### 工作项 11：UserRepositoryImpl 扩展（如果需要）

**位置**：`datastore/repository-server/src/main/kotlin/.../UserRepositoryImpl.kt`

**需要实现的方法**（依据实际需求）：

- `upsertUser(user: User)`
    - 调用 `localDataSource.upsertUser(user)` 保存或更新到数据库
- `deleteUser(userId: String)`
    - 调用 `localDataSource.deleteUser(userId)` 删除数据

**设计要点**：

- 直接使用 LocalDataSource，不需要 RemoteDataSource
- 不需要同步逻辑（服务端是数据源）
- 实现简洁，只包含必要的业务逻辑

---

### 5.3 集成工作

#### 工作项 12：更新 RepositoryModule

**位置**：`datastore/repository-server/src/main/kotlin/.../di/RepositoryModule.kt`

**需要更新**：

- 添加新实现的 Repository 的依赖注入
- 确保依赖关系正确（如 CardRepository 依赖 TagRepository）

#### 工作项 13：更新 Service 层

**位置**：`server/src/main/kotlin/.../service/`

**需要更新**：

- CardService：使用 `cardRepository.upsertCard`
- 其他 Service：依据实际需求更新

#### 工作项 14：更新 API 路由

**位置**：`server/src/main/kotlin/.../api/`

**需要更新**：

**CardsApi**：

- `POST /api/cards/upsertCard` - 创建或更新卡片
- `PUT /api/cards/upsertCard?cardId={id}` - 更新卡片
- `DELETE /api/cards/deleteCard?cardId={id}` - 删除卡片

**TagsApi**（如果需要）：

- `GET /api/tags/fetchTags?userId={userId}` - 获取用户所有标签
- `GET /api/tags/fetchTag?tagId={id}` - 获取指定标签
- `POST /api/tags/upsertTag` - 创建或更新标签
- `PUT /api/tags/upsertTag?tagId={id}` - 更新标签
- `DELETE /api/tags/deleteTag?tagId={id}` - 删除标签

**CollectionsApi**（如果需要）：

- `GET /api/collections/fetchCollections?userId={userId}` - 获取用户所有集合
- `GET /api/collections/fetchCollection?collectionId={id}` - 获取指定集合
- `POST /api/collections/upsertCollection` - 创建或更新集合
- `PUT /api/collections/upsertCollection?collectionId={id}` - 更新集合
- `DELETE /api/collections/deleteCollection?collectionId={id}` - 删除集合

**TemplatesApi**（如果需要）：

- `GET /api/templates/fetchTemplates` - 获取所有模板
- `GET /api/templates/fetchTemplate?templateId={id}` - 获取指定模板
- `POST /api/templates/upsertTemplate` - 创建或更新模板
- `PUT /api/templates/upsertTemplate?templateId={id}` - 更新模板
- `DELETE /api/templates/deleteTemplate?templateId={id}` - 删除模板

**UsersApi**（如果需要）：

- `POST /api/users/upsertUser` - 创建或更新用户
- `PUT /api/users/upsertUser?userId={id}` - 更新用户
- `DELETE /api/users/deleteUser?userId={id}` - 删除用户

---

## 🏗️ 六、架构设计

### 6.1 服务端 Repository 架构

```
┌─────────────────────────────────────────────────────────┐
│                    API 层 (Ktor Routing)                │
│  CardsApi, UsersApi, SyncApi, TagsApi, ...              │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Service 层                              │
│  CardService, UserService, SyncService, ...             │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│              Repository 层 (repository-server)           │
│  CardRepository, UserRepository, SyncRepository, ...    │
│  - 不包含 Flow 返回类型                                  │
│  - 只包含实际需要的方法                                   │
│  - 不强制与客户端统一                                     │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│            DataSource 层 (datasource-local)              │
│  LocalCardDataSource, LocalUserDataSource, ...         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│              Database 层 (SQLDelight)                    │
│  MyHubDatabase                                          │
└─────────────────────────────────────────────────────────┘
```

### 6.2 数据访问流程

#### 实时 API 访问流程

```
客户端 HTTP 请求
    ↓
API 路由 (CardsApi)
    ↓
Service 层 (CardService)
    ↓
Repository 层 (CardRepository)
    ↓
DataSource 层 (LocalCardDataSource)
    ↓
Database (SQLDelight)
    ↓
返回数据
```

#### 同步访问流程

```
客户端 Sync 请求
    ↓
API 路由 (SyncApi)
    ↓
Service 层 (SyncService)
    ↓
Repository 层 (SyncRepository)
    ↓
DataSource 层 (LocalSyncDataSource)
    ↓
Database (SQLDelight)
    ↓
返回同步结果
```

---

## ✅ 七、验收标准

### 7.1 功能完整性

- [ ] 所有 Service 层需要的 Repository 方法都已实现
- [ ] 所有 API 路由都能正常工作
- [ ] 同步功能正常工作

### 7.2 设计原则遵循

- [ ] 所有 Repository 接口不包含 Flow 返回类型
- [ ] Repository 接口不强制与客户端统一
- [ ] 只实现实际需要的方法

### 7.3 代码质量

- [ ] 所有代码通过 linter 检查
- [ ] 关键方法有适当的注释
- [ ] 代码结构清晰，易于维护

---

## 📌 八、关键说明

### 8.1 关于响应式流

**明确**：服务端 Repository **不需要响应式流（Flow）**。

- 服务端是数据存储层，不需要实时数据流
- 客户端通过 API 请求获取数据，每次请求都是独立的
- 所有 Repository 方法使用 `suspend fun` 返回同步数据

### 8.2 关于接口统一

**明确**：服务端的 Repository 接口**不强制与客户端统一**。

- 服务端和客户端的使用场景不同
- 服务端不需要响应式流、缓存管理等客户端特性
- 保持接口简洁，只包含必要的功能

### 8.3 关于功能完整性

**明确**：每个 Repository 的实现**依据实际需求**，不追求功能完整性。

- 分析 Service 层的实际需求
- 只实现 Service 层需要的方法
- 不需要实现所有可能的 CRUD 操作

---

## 📋 九、API 设计总结

### 9.1 API 命名规范

所有 Repository 的 API 遵循统一的命名规范：

- **查询操作**：`GET /api/{resource}/fetch{Resource}` 或 `GET /api/{resource}/fetch{Resource}s`
- **创建或更新操作**：`POST /api/{resource}/upsert{Resource}`
- **删除操作**：`DELETE /api/{resource}/delete{Resource}?{resource}Id={id}`

### 9.2 所有 Repository 的 API 列表

#### CardRepository API

- ✅ `GET /api/cards/fetchCards?userId={userId}` - 已实现
- ✅ `GET /api/cards/fetchCard?cardId={id}` - 已实现
- ❌ `POST /api/cards/createCard` - 需要实现
- ❌ `PUT /api/cards/upsertCard?cardId={id}` - 需要实现
- ✅ `DELETE /api/cards/deleteCard?cardId={id}` - 已实现

#### TagRepository API（确认）

- ❌ `GET /api/tags/fetchTags?userId={userId}` - 确认
- ❌ `GET /api/tags/fetchTag?tagId={id}` - 确认
- ❌ `POST /api/tags/upsertTag` - 确认
- ❌ `DELETE /api/tags/deleteTag?tagId={id}` - 确认

#### CollectionRepository API（确认）

- ❌ `GET /api/collections/fetchCollections?userId={userId}` - 确认
- ❌ `GET /api/collections/fetchCollection?collectionId={id}` - 确认
- ❌ `POST /api/collections/upsertCollection` - 确认
- ❌ `DELETE /api/collections/deleteCollection?collectionId={id}` - 确认

#### CardTemplateRepository API（确认）

- ❌ `GET /api/templates/fetchTemplates` - 确认
- ❌ `GET /api/templates/fetchTemplate?templateId={id}` - 确认
- ❌ `POST /api/templates/upsertTemplate` - 确认
- ❌ `DELETE /api/templates/deleteTemplate?templateId={id}` - 确认

#### UserRepository API

- ✅ `GET /api/users/fetchUser?userId={id}` - 已实现（当前为 PUT，需要统一）
- ❌ `POST /api/users/upsertUser` - 确认
- ❌ `DELETE /api/users/deleteUser?userId={id}` - 确认

#### SyncRepository API

- ✅ `POST /api/sync/push` - 已实现
- ✅ `GET /api/sync/pull` - 已实现

### 9.3 需要修改的内容清单

#### 接口定义修改（repository-server-api）

1. **CardRepository**：
    - 添加 `upsertCard(card: Card)`

2. **TagRepository**（确认）：
    - 创建新接口文件
    - 定义所有 CRUD 方法
    - 定义 `ensureTags` 方法（如果 CardRepository 需要）

3. **CollectionRepository**（确认）：
    - 创建新接口文件
    - 定义所有 CRUD 方法

4. **CardTemplateRepository**（确认）：
    - 创建新接口文件
    - 定义所有 CRUD 方法

5. **UserRepository**（确认）：
    - 添加 `upsertUser(user: User)`
    - 添加 `deleteUser(userId: String)`

#### 实现修改（repository-server）

1. **CardRepositoryImpl**：
    - 实现 `upsertCard` 方法

2. **TagRepositoryImpl**（确认）：
    - 创建新实现类
    - 实现所有接口方法

3. **CollectionRepositoryImpl**（确认）：
    - 创建新实现类
    - 实现所有接口方法

4. **CardTemplateRepositoryImpl**（确认）：
    - 创建新实现类
    - 实现所有接口方法

5. **UserRepositoryImpl**（确认）：
    - 实现 `upsertUser` 方法
    - 实现 `deleteUser` 方法

#### API 路由修改（server）

1. **CardsApi**：
    - 添加 `POST /api/cards/upsertCard` 路由
    - 添加 `PUT /api/cards/upsertCard?cardId={id}` 路由

2. **TagsApi**（如果需要）：
    - 取消注释或创建新文件
    - 实现所有 CRUD 路由

3. **CollectionsApi**（如果需要）：
    - 创建新文件
    - 实现所有 CRUD 路由

4. **TemplatesApi**（如果需要）：
    - 取消注释或创建新文件
    - 实现所有 CRUD 路由

5. **UsersApi**（如果需要）：
    - 添加 `POST /api/users/upsertUser` 路由
    - 添加 `PUT /api/users/upsertUser?userId={id}` 路由
    - 添加 `DELETE /api/users/deleteUser?userId={id}` 路由
    - 统一 `GET /api/users/fetchUser?userId={id}` 路由（当前为 PUT）

#### Service 层修改（server）

1. **CardService**：
    - 添加 `upsertCard` 方法

2. **TagService**（确认）：
    - 创建新 Service 类或取消注释
    - 实现所有业务逻辑方法

3. **CollectionService**（确认）：
    - 创建新 Service 类
    - 实现所有业务逻辑方法

4. **TemplateService**（确认）：
    - 创建新 Service 类或取消注释
    - 实现所有业务逻辑方法

5. **UserService**（确认）：
    - 添加 `upsertUser` 方法
    - 添加 `deleteUser` 方法

#### 依赖注入修改（repository-server）

1. **RepositoryModule**：
    - 添加新 Repository 的依赖注入
    - 更新现有 Repository 的依赖关系

---

**文档版本**: v2.1  
**创建日期**: 2026-01-26  
**最后更新**: 2026-01-27  
**状态**: 已确认设计原则，已列出所有修改内容
