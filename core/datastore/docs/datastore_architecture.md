# Datastore 架构设计

## 📋 概述

本文档阐述 MyHub 应用的数据模型架构设计方案。数据模型作为应用的核心基础，为业务逻辑层和 UI 层提供数据支撑。

## 🏗️ 架构设计

### 分层架构

```
┌─────────────────────────────────────────┐
│         UI Layer (Compose)             │
│  - ViewModel                           │
│  - UI State (CardUiState, etc.)        │
│  - Composable Functions                │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Repository Layer                   │
│  - CardRepository                      │
│  - TagRepository                       │
│  - TemplateRepository                  │
│  - StatisticsRepository                │
│  - UserRepository                      │
│  (业务逻辑协调，数据转换)                │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴───────┐
       │               │
┌──────▼──────┐ ┌──────▼──────┐
│   Local     │ │   Remote    │
│  DataSource │ │  DataSource │
│             │ │             │
│ - SQLDelight│ │ - Ktor      │
│   (SQLite)  │ │   Client    │
└─────────────┘ └─────────────┘
```

## 📁 目录结构

```
datastore/
├── model/                           # 核心数据模型（领域模型）
│   ├── Card.kt                      # 卡片实体
│   ├── User.kt                      # 用户实体
│   ├── Tag.kt                       # 标签实体
│   ├── Template.kt                  # 模板实体
│   ├── Statistics.kt                # 统计信息
│   └── SearchFilter.kt              # 搜索筛选条件
│
├── dto/                             # 数据传输对象（用于API）
│   └── CardDto.kt                   # 卡片DTO及Domain Model转换函数
│
├── repository/                      # 仓库接口和实现
│   ├── CardRepository.kt            # 卡片仓库接口
│   ├── TagRepository.kt             # 标签仓库接口
│   ├── TemplateRepository.kt        # 模板仓库接口
│   ├── StatisticsRepository.kt      # 统计信息仓库接口
│   ├── UserRepository.kt            # 用户仓库接口
│   └── impl/                        # 仓库实现
│       ├── CardRepositoryImpl.kt            # 卡片仓库实现（协调本地和远程数据源）
│       ├── TagRepositoryImpl.kt             # 标签仓库实现
│       ├── TemplateRepositoryImpl.kt        # 模板仓库实现
│       ├── StatisticsRepositoryImpl.kt      # 统计信息仓库实现
│       └── UserRepositoryImpl.kt            # 用户仓库实现
│
├── datasource/                      # 数据源接口和实现
│   ├── LocalDataSource.kt           # 本地数据源接口定义
│   ├── RemoteDataSource.kt          # 远程数据源接口定义
│   └── impl/                        # 数据源实现
│       ├── LocalCardDataSourceImpl.kt        # 本地卡片数据源实现（SQLDelight）
│       ├── LocalTagDataSourceImpl.kt         # 本地标签数据源实现（SQLDelight）
│       ├── LocalTemplateDataSourceImpl.kt    # 本地模板数据源实现（SQLDelight）
│       ├── LocalUserDataSourceImpl.kt        # 本地用户数据源实现（SQLDelight）
│       ├── LocalStatisticsDataSourceImpl.kt  # 本地统计信息数据源实现（SQLDelight）
│       └── RemoteCardDataSourceImpl.kt       # 远程卡片数据源实现（Ktor Client）
│
├── database/                        # 数据库相关
│   └── DatabaseDriverFactory.kt     # 跨平台数据库驱动工厂（expect/actual）
│
├── network/                         # 网络相关
│   ├── ApiConfig.kt                 # API配置（基础URL、路径、超时等）
│   ├── KtorClientFactory.kt         # Ktor Client工厂（expect/actual）
│   └── README.md                    # 网络层实现文档
│
├── ui/                              # UI状态模型
│   └── CardUiState.kt               # 卡片相关UI状态（列表、详情、Dashboard、搜索）
│
├── di/                              # 依赖注入配置（Koin模块）
│   ├── DataModule.kt                # 数据模块（DataSource和Repository配置）
│   ├── NetworkModule.kt             # 网络模块（HttpClient配置）
│   └── DatabaseModule.kt            # 数据库模块（数据库实例配置）
│
├── sqldelight/                      # SQLDelight数据库Schema定义
│   └── tech/zhifu/app/myhub/datastore/database/
│       ├── Card.sq                  # 卡片表定义（主表、标签关联、元数据、待办项）
│       ├── Tag.sq                   # 标签表定义
│       ├── Template.sq              # 模板表定义
│       ├── User.sq                  # 用户表定义（用户信息和偏好设置）
│       └── Statistics.sq            # 统计信息表定义（总体统计、类型统计、标签统计）
│
└── docs/                            # 文档目录
    └── datastore_architecture.md    # 本文档（架构设计文档）

> **模块位置**：`core/datastore/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/`
```

## 📦 核心数据模型

### 1. Card (卡片)

**位置**: `datastore/model/Card.kt`

卡片是应用的核心实体，支持 6 种类型：

- `QUOTE`: 引言卡片 - 存储名言、格言等
- `CODE`: 代码片段 - 存储代码示例，支持语法高亮
- `IDEA`: 想法 - 存储灵感、创意
- `ARTICLE`: 文章 - 存储文章摘要和链接
- `DICTIONARY`: 字典 - 存储单词定义和发音
- `CHECKLIST`: 待办清单 - 存储待办事项列表

**核心字段**:

```kotlin
data class Card(
    val id: String,                    // 唯一标识
    val type: CardType,                // 卡片类型
    val title: String? = null,         // 标题（可选）
    val content: String,               // 主要内容
    val author: String? = null,         // 作者
    val source: String? = null,        // 来源
    val language: String? = null,       // 语言（用于代码卡片）
    val tags: List<String> = emptyList(), // 标签列表
    val isFavorite: Boolean = false,   // 是否收藏
    val isTemplate: Boolean = false,   // 是否为模板
    val createdAt: Instant,            // 创建时间
    val updatedAt: Instant,            // 更新时间
    val lastReviewedAt: Instant? = null, // 最后查看时间
    val metadata: CardMetadata? = null  // 扩展元数据
)
```

**元数据 (CardMetadata)**:
根据卡片类型存储特定信息：

- Quote: `quoteAuthor`, `quoteCategory`
- Code: `codeLanguage`, `codeSnippet`
- Article: `articleUrl`, `articleSummary`, `articleImageUrl`
- Dictionary: `wordPronunciation`, `wordDefinition`, `wordExample`
- Checklist: `checklistItems` (待办项列表)
- Idea: `ideaPriority`, `ideaStatus`

### 2. User (用户)

**位置**: `datastore/model/User.kt`

```kotlin
data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Instant,
    val preferences: UserPreferences? = null
)
```

**用户偏好 (UserPreferences)**:

- 主题设置 (light/dark/auto)
- 语言设置
- 默认卡片类型
- 自动同步设置

### 3. Tag (标签)

**位置**: `datastore/model/Tag.kt`

用于分类和组织卡片：

```kotlin
data class Tag(
    val id: String,
    val name: String,
    val color: String? = null,        // 十六进制颜色值
    val description: String? = null,
    val cardCount: Int = 0,           // 使用该标签的卡片数量
    val createdAt: Instant
)
```

### 4. Template (模板)

**位置**: `datastore/model/Template.kt`

用于快速创建卡片：

```kotlin
data class Template(
    val id: String,
    val name: String,
    val description: String? = null,
    val cardType: CardType,
    val defaultContent: String? = null,
    val defaultMetadata: CardMetadata? = null,
    val defaultTags: List<String> = emptyList(),
    val usageCount: Int = 0,
    val isSystemTemplate: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### 5. Statistics (统计信息)

**位置**: `datastore/model/Statistics.kt`

应用级别的统计信息：

```kotlin
data class Statistics(
    val totalCards: Int = 0,
    val favoriteCards: Int = 0,
    val recentEdits: Int = 0,
    val cardsByType: Map<CardType, Int> = emptyMap(),
    val cardsByTag: Map<String, Int> = emptyMap(),
    val lastSyncTime: Long? = null
)
```

### 6. SearchFilter (搜索筛选)

**位置**: `datastore/model/SearchFilter.kt`

用于搜索和筛选卡片：

```kotlin
data class SearchFilter(
    val query: String? = null,
    val cardTypes: List<CardType> = emptyList(),
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean? = null,
    val isTemplate: Boolean? = null,
    val dateRange: DateRange? = null,
    val sortBy: SortBy = SortBy.UPDATED_AT_DESC
)
```

## 🔄 数据流

### 读取数据流程

```
UI Layer (ViewModel)
    ↓
Repository (CardRepository)
    ↓
LocalDataSource (优先读取，快速响应)
    ↓
[如果需要] RemoteDataSource (同步最新数据)
    ↓
更新 LocalDataSource
    ↓
通过 Flow 返回给 UI
```

### 写入数据流程

```
UI Layer (用户操作)
    ↓
Repository (CardRepository)
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

## ✅ 实现状态

### 已完成的工作

#### 1. 核心数据模型

- ✅ Card (卡片实体)
- ✅ User (用户实体)
- ✅ Tag (标签实体)
- ✅ Template (模板实体)
- ✅ Statistics (统计信息)
- ✅ SearchFilter (搜索筛选条件)

#### 2. 数据传输层 (DTO)

- ✅ CardDto 及转换函数
- ✅ 支持 API 请求/响应的序列化

#### 3. 本地数据源 (LocalDataSource)

所有本地数据源已使用 SQLDelight 实现：

- ✅ **LocalCardDataSourceImpl** - 卡片数据源
- ✅ **LocalTagDataSourceImpl** - 标签数据源
- ✅ **LocalTemplateDataSourceImpl** - 模板数据源
- ✅ **LocalUserDataSourceImpl** - 用户数据源
- ✅ **LocalStatisticsDataSourceImpl** - 统计信息数据源

#### 4. 远程数据源 (RemoteDataSource)

- ✅ **RemoteCardDataSourceImpl** - 使用 Ktor Client 实现的远程卡片数据源

#### 5. 仓库层 (Repository)

所有仓库已实现：

- ✅ **CardRepositoryImpl** - 卡片仓库（协调本地和远程数据源）
- ✅ **TagRepositoryImpl** - 标签仓库
- ✅ **TemplateRepositoryImpl** - 模板仓库
- ✅ **UserRepositoryImpl** - 用户仓库
- ✅ **StatisticsRepositoryImpl** - 统计信息仓库

#### 6. 数据库

- ✅ SQLDelight 配置和 Schema 定义
- ✅ 跨平台数据库驱动工厂
- ✅ 数据库模块配置

#### 7. 网络层

- ✅ Ktor Client 配置
- ✅ 跨平台 HttpClient 工厂
- ✅ API 配置和错误处理

#### 8. 依赖注入

- ✅ **DataModule** - 已配置所有 DataSource 和 Repository
- ✅ **NetworkModule** - 已配置 HttpClient
- ✅ **DatabaseModule** - 已配置数据库实例

#### 9. UI 状态模型

- ✅ CardListUiState
- ✅ CardDetailUiState
- ✅ DashboardUiState
- ✅ SearchUiState

#### 10. 测试

所有数据层的核心组件都已实现单元测试：

- ✅ **LocalDataSource 测试** - 所有 5 个 LocalDataSource 实现都有完整的单元测试

  - LocalCardDataSourceTest
  - LocalTagDataSourceTest
  - LocalTemplateDataSourceTest
  - LocalUserDataSourceTest
  - LocalStatisticsDataSourceTest

- ✅ **Repository 测试** - 所有 5 个 Repository 实现都有完整的单元测试

  - CardRepositoryTest
  - TagRepositoryTest
  - TemplateRepositoryTest
  - UserRepositoryTest
  - StatisticsRepositoryTest

- ✅ **数据库基础功能测试** - DatabaseTest 覆盖了数据库的核心功能

  - Schema 创建
  - 插入和查询
  - 事务回滚
  - 外键约束
  - CASCADE 删除

- ✅ **跨平台测试支持** - 测试在所有平台（Android、iOS、JVM、Web）都能运行
  - 使用 `runDatabaseTest` 辅助函数统一测试写法
  - 自动处理平台差异（特别是 Kotlin/JS 的异步问题）

📖 **测试文档**: 参见 [`datastore_test_guide.md`](./datastore_test_guide.md)

## 🎯 使用方式

### 在 ViewModel 中使用

```kotlin
class DashboardViewModel(
    private val cardRepository: CardRepository,
    private val tagRepository: TagRepository,
    private val templateRepository: TemplateRepository,
    private val userRepository: UserRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    val cards: Flow<List<Card>> = cardRepository.getAllCards()
    val tags: Flow<List<Tag>> = tagRepository.getAllTags()
    val statistics: Flow<Statistics> = statisticsRepository.getStatistics()

    fun syncData() {
        viewModelScope.launch {
            cardRepository.sync() // 从远程同步数据
        }
    }
}
```

### 创建卡片

```kotlin
val card = Card(
    id = "unique-id",
    type = CardType.QUOTE,
    content = "The more that you read...",
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now()
)

cardRepository.createCard(card)
    .onSuccess { createdCard ->
        // 卡片创建成功
    }
    .onFailure { error ->
        // 处理错误
    }
```

### 使用模板创建卡片

```kotlin
templateRepository.createCardFromTemplate(templateId)
    .onSuccess { card ->
        // 使用创建的卡片
        cardRepository.createCard(card)
    }
    .onFailure { error ->
        // 处理错误
    }
```

### 搜索卡片

```kotlin
val filter = SearchFilter(
    query = "kotlin",
    cardTypes = listOf(CardType.CODE),
    tags = listOf("programming"),
    isFavorite = null,
    sortBy = SortBy.UPDATED_AT_DESC
)

cardRepository.searchCards(filter)
    .collect { cards ->
        // 处理搜索结果
    }
```

## 🎯 设计原则

### 1. 单一数据源 (Single Source of Truth)

- LocalDataSource 是主要数据源
- RemoteDataSource 用于同步
- Repository 协调两者

### 2. 响应式数据流

- 使用 Kotlin Flow 提供响应式数据流
- UI 层通过 StateFlow/Flow 观察数据变化
- 自动更新 UI，无需手动刷新

### 3. 离线优先

- 优先使用本地数据，保证离线可用
- 后台同步远程数据
- 支持离线编辑，上线后自动同步

### 4. 类型安全

- 使用 Kotlin 的强类型系统
- 使用 kotlinx.serialization 进行序列化
- DTO 和 Domain Model 分离，通过转换函数映射

### 5. 可测试性

- Repository 接口便于 Mock
- DataSource 接口便于替换实现
- 业务逻辑与平台实现分离

## 🔧 功能特性

### 1. 离线优先

- 所有数据优先从本地数据库读取
- 后台自动同步远程数据
- 支持离线编辑

### 2. 响应式数据流

- 使用 Kotlin Flow 提供响应式数据流
- UI 自动响应数据变化

### 3. 类型安全

- 使用 kotlinx.serialization 进行序列化
- 强类型系统确保类型安全

### 4. 错误处理

- 统一的 Result<T> 类型处理成功/失败
- 区分网络错误和 API 错误（ApiException, NetworkException）

## 📝 待完善的功能

详见 [`datastore_todos.md`](./datastore_todos.md)

## 🚀 下一步工作

详见 [`datastore_todos.md`](./datastore_todos.md)

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
  - Web: WebWorkerDriver

## 📚 参考资料

- [Kotlin Multiplatform 官方文档](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlin Flow 文档](https://kotlinlang.org/docs/flow.html)
- [Ktor Client 文档](https://ktor.io/docs/client.html)
- [SQLDelight 文档](https://cashapp.github.io/sqldelight/)
- [Koin 文档](https://insert-koin.io/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [项目测试文档](./datastore_test_guide.md) - 数据库测试指南
- [项目 FAQ](../../../../docs/FAQ.md) - 常见问题解答（包含 Kotlin/JS 测试问题）

## 💡 最佳实践

1. **始终使用 Flow 返回数据**: 确保 UI 能够响应数据变化
2. **优先本地数据**: 提供快速响应，后台同步
3. **错误处理**: 使用 Result<T> 类型统一处理成功/失败
4. **类型安全**: 避免使用 Any 或原始类型
5. **可空性**: 明确标记可空类型，避免 NPE
6. **线程安全**: Repository 和 DataSource 的操作应该是线程安全的
7. **事务处理**: 使用数据库事务确保数据一致性

## ⚠️ 注意事项

1. **时间戳处理**: 使用 `kotlin.time.Clock.System.now()` 和 `kotlin.time.Instant` 确保跨平台兼容（避免使用 `System.currentTimeMillis()`）
2. **序列化**: 所有模型类使用 `@Serializable` 注解
3. **空值处理**: 使用 Kotlin 的可空类型明确处理可能为空的值
4. **错误处理**: 使用 `Result<T>` 类型统一处理成功/失败情况
5. **线程安全**: Repository 和 DataSource 的操作应该是线程安全的
6. **数据库迁移**: 修改 Schema 时需要提供迁移脚本
7. **网络超时**: 默认连接和请求超时都是 30 秒，可根据需要调整
8. **测试编写**:
   - ✅ 使用 `runDatabaseTest { database -> ... }` 进行数据库测试
   - ❌ 避免在 `@BeforeTest` 中使用 `runTest`（在 Kotlin/JS 平台不会被等待）
   - 📖 更多测试注意事项参见 [FAQ](../../../../docs/FAQ.md#q1-为什么在-kotlinjs-测试中beforetest-里的-runtest--不会等待完成)

---

**最后更新**: 2025-12-24  
**维护者**: MyHub Team  
**状态**: ✅ 核心功能已完成，单元测试已覆盖，可投入使用
