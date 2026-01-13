# Core Datastore Model Module

本模块用于**规范**和**实现** MyHub 应用的数据模型基础设施（Datastore Model Infra），为各功能模块**提供统一、跨平台的数据模型定义和 DTO 转换能力**。它基于 **kotlinx.serialization 和 kotlinx.datetime**，实现了**领域模型定义**、**DTO 支持**、**类型安全**、**JSON 序列化**、**时间类型处理**等特性，并提供了面向 KMP 场景的**统一数据模型接口**，方便在 **Android、iOS、JVM、JS、WASM** 等多端项目中集成和使用。

## 📋 功能特性

- ✅ **领域模型**：定义应用的核心数据实体
- ✅ **DTO 支持**：提供网络传输的数据传输对象
- ✅ **类型安全**：使用 Kotlin 数据类和枚举确保类型安全
- ✅ **序列化支持**：使用 kotlinx.serialization 支持 JSON 序列化
- ✅ **跨平台支持**：纯 Kotlin 代码，支持所有平台（Android、iOS、JVM、JS、WASM）
- ✅ **单元测试**：完整的数据模型和转换函数测试覆盖

## 🎯 支持的平台

- **Android** - Android 平台
- **iOS** - iOS 平台（所有架构）
- **JVM** - 桌面应用（Windows、macOS、Linux）
- **JS** - Web 应用（Kotlin/JS）
- **WASM** - Web 应用（Kotlin/WASM）

> **注意**：该模块只有 `commonMain`，没有平台特定的代码，所有平台共享相同的数据模型。

## 📁 模块结构

```
core/datastore-model/src/
└── commonMain/
    └── kotlin/tech/zhifu/app/myhub/datastore/model/
        ├── Card.kt              # 卡片实体和元数据
        ├── CardDto.kt           # 卡片 DTO 和转换函数
        ├── Tag.kt               # 标签实体
        ├── Template.kt          # 模板实体
        ├── User.kt              # 用户实体和偏好设置
        ├── Statistics.kt      # 统计信息
        └── SearchFilter.kt     # 搜索筛选条件
```

## 🔧 数据模型

### Card（卡片）

应用的核心数据实体，支持 6 种类型：

- **QUOTE** - 引言卡片
- **CODE** - 代码片段
- **IDEA** - 想法
- **ARTICLE** - 文章
- **DICTIONARY** - 字典
- **CHECKLIST** - 待办清单

```kotlin
val card = Card(
    id = "card-1",
    type = CardType.QUOTE,
    title = "Test Title",
    content = "Test content",
    author = "Author",
    tags = listOf("tag1", "tag2"),
    isFavorite = true,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    metadata = CardMetadata(
        quoteAuthor = "Quote Author"
    )
)
```

### Tag（标签）

用于分类和组织卡片：

```kotlin
val tag = Tag(
    id = "tag-1",
    name = "Programming",
    color = "#FF5733",
    description = "Programming related cards",
    cardCount = 10,
    createdAt = Clock.System.now()
)
```

### Template（模板）

卡片模板，用于快速创建卡片：

```kotlin
val template = Template(
    id = "template-1",
    name = "Quote Template",
    cardType = CardType.QUOTE,
    defaultContent = "Default content",
    isSystemTemplate = true,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now()
)
```

### User（用户）

用户实体和偏好设置：

```kotlin
val user = User(
    id = "user-1",
    username = "testuser",
    email = "test@example.com",
    preferences = UserPreferences(
        theme = "dark",
        language = "zh-CN",
        defaultCardType = CardType.QUOTE,
        autoSync = true
    ),
    createdAt = Clock.System.now()
)
```

### Statistics（统计信息）

应用统计信息：

```kotlin
val stats = Statistics(
    totalCards = 100,
    favoriteCards = 20,
    recentEdits = 10,
    cardsByType = mapOf(
        CardType.QUOTE to 30,
        CardType.CODE to 20
    ),
    cardsByTag = mapOf(
        "tag1" to 40
    )
)
```

### SearchFilter（搜索筛选）

搜索和筛选条件：

```kotlin
val filter = SearchFilter(
    query = "test",
    cardTypes = listOf(CardType.QUOTE, CardType.CODE),
    tags = listOf("tag1"),
    isFavorite = true,
    sortBy = SortBy.CREATED_AT_DESC
)
```

## 🔄 DTO 和转换函数

### CardDto

用于网络传输的卡片 DTO：

```kotlin
// 领域模型转 DTO
val dto = card.toDto()

// DTO 转领域模型
val card = dto.toDomain()
```

### CreateCardRequest

创建卡片请求：

```kotlin
val request = CreateCardRequest(
    type = "quote",
    title = "Test Title",
    content = "Test content",
    tags = listOf("tag1")
)

// 转换为领域模型
val card = request.toDomain()
```

### UpdateCardRequest

更新卡片请求（所有字段可选）：

```kotlin
val request = UpdateCardRequest(
    title = "Updated Title",
    isFavorite = true
)
```

## 📦 序列化

所有数据模型都使用 `@Serializable` 注解，支持 JSON 序列化：

```kotlin
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// 序列化
val json = Json.encodeToString(card)

// 反序列化
val card = Json.decodeFromString<Card>(json)
```

## 🧪 测试

模块包含完整的单元测试：

- **CardTest** - Card 和 CardMetadata 测试
- **CardDtoTest** - DTO 转换和序列化测试
- **TagTest** - Tag 和 TagStats 测试
- **SearchFilterTest** - SearchFilter 和 DateRange 测试
- **UserTest** - User 和 UserPreferences 测试
- **StatisticsTest** - Statistics 和 CardStatistics 测试
- **TemplateTest** - Template 测试

### 运行测试

```bash
# 运行所有平台的测试
./gradlew :core:datastore-model:allTests

# 运行特定平台的测试
./gradlew :core:datastore-model:jvmTest
./gradlew :core:datastore-model:jsTest
./gradlew :core:datastore-model:wasmJsTest
```

## 📝 设计说明

### 领域模型 vs DTO

- **领域模型（Card、Tag 等）**：应用内部使用的数据模型，使用 `Instant` 类型表示时间
- **DTO（CardDto 等）**：用于网络传输，使用 `String` 类型表示时间（ISO 8601 格式）

### 数据模型特点

1. **不可变性**：所有数据类都是 `data class`，确保不可变
2. **可选字段**：使用默认参数提供可选字段
3. **类型安全**：使用枚举类型（`CardType`、`SortBy`）确保类型安全
4. **序列化支持**：所有模型都支持 JSON 序列化

### 用户关联

从数据库版本 2 开始，所有实体都支持 `userId` 字段，用于多用户支持：
- `Card.userId` - 卡片所属用户
- `Tag.userId` - 标签所属用户
- `Template.userId` - 模板所属用户（系统模板使用 "system"）

## 🔗 相关模块

- `core:datastore-database` - 数据库 Schema 定义
- `core:datastore-datasource-local` - 本地数据源
- `core:datastore-datasource-remote` - 远程数据源
- `core:datastore-repository` - 仓库接口

## 📚 使用示例

### 创建卡片

```kotlin
val card = Card(
    id = generateId(),
    type = CardType.QUOTE,
    content = "The only way to do great work is to love what you do.",
    author = "Steve Jobs",
    tags = listOf("inspiration", "work"),
    isFavorite = true,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    metadata = CardMetadata(
        quoteAuthor = "Steve Jobs",
        quoteCategory = "Business"
    )
)
```

### 搜索和筛选

```kotlin
val filter = SearchFilter(
    query = "work",
    cardTypes = listOf(CardType.QUOTE),
    tags = listOf("inspiration"),
    isFavorite = true,
    sortBy = SortBy.UPDATED_AT_DESC
)
```

### DTO 转换

```kotlin
// 从 API 接收数据
val dto: CardDto = apiClient.getCard(id)

// 转换为领域模型
val card: Card = dto.toDomain()

// 发送到 API
val createRequest = CreateCardRequest(
    type = "quote",
    content = "New quote"
)
apiClient.createCard(createRequest)
```

