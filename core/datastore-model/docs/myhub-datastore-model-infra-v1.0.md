# MyHub 数据模型模块方案设计

**方案名称**：Datastore Model Infra v1  
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
- **方案状态**：🔒 已锁定 - 此版本已冻结，作为 Datastore Model Infra v1 的基线设计
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

| 版本 | 日期       | 修改内容                           | 修改原因           |
| ---- | ---------- | ---------------------------------- | ------------------ |
| v1.0 | 2026-01-13 | 初始方案设计                       | 新建               |
| v1.0 | 2026-01-13 | 完成架构设计文档                   | 完善文档           |
| v1.0 | 2026-01-13 | 更新状态：评审通过、方案已锁定     | 状态更新：评审通过 |

---

## 1. 问题背景

### 1.1 用户场景

在 MyHub 应用的开发和运行过程中，需要处理数据模型的定义、序列化和转换。典型的场景包括：

1. **领域模型定义**：应用需要定义核心业务实体（如 Card、Tag、Template、User 等），这些实体需要在应用内部使用
2. **数据传输对象（DTO）**：应用需要与服务器进行数据交换，需要定义网络传输的数据格式
3. **数据序列化**：数据需要在不同格式间转换（如 JSON、数据库记录等）
4. **类型安全**：需要确保数据类型安全，避免运行时错误
5. **跨平台兼容**：在 KMP 项目中，数据模型需要在所有平台（Android、iOS、JVM、JS、WASM）上保持一致

### 1.2 问题根因

在引入统一的数据模型模块之前，MyHub 应用面临以下问题：

1. **数据模型分散**：各模块可能定义自己的数据模型，导致代码重复和不一致
2. **领域模型与 DTO 混淆**：业务代码直接使用 DTO，导致领域逻辑与传输逻辑耦合
3. **序列化不一致**：不同模块使用不同的序列化方式，导致兼容性问题
4. **类型安全问题**：使用 `Any` 类型或字符串表示枚举，导致类型不安全
5. **时间类型不一致**：领域模型使用 `Instant`，DTO 使用 `String`，缺乏统一的转换机制

### 1.3 影响范围

- **代码维护**：数据模型分散，增加维护成本和出错风险
- **类型安全**：缺乏类型安全导致运行时错误
- **跨平台兼容性**：数据模型不一致导致跨平台问题
- **测试困难**：数据模型分散导致测试困难

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **统一的领域模型**：定义应用的核心业务实体（Card、Tag、Template、User、Statistics 等）
- ✅ **DTO 支持**：提供网络传输的数据传输对象和转换函数
- ✅ **类型安全**：使用 Kotlin 数据类和枚举确保类型安全
- ✅ **序列化支持**：使用 kotlinx.serialization 支持 JSON 序列化
- ✅ **跨平台支持**：纯 Kotlin 代码，支持所有平台（Android、iOS、JVM、JS、WASM）
- ✅ **时间类型处理**：领域模型使用 `Instant`，DTO 使用 `String`（ISO 8601），提供转换函数

### 2.2 非功能目标

- ✅ **代码复用**：数据模型在多个模块间复用
- ✅ **易于测试**：数据模型易于测试，提供完整的单元测试覆盖
- ✅ **易于扩展**：数据模型易于扩展，支持新增字段和类型
- ✅ **性能优化**：序列化性能优化，支持增量更新

### 2.3 模块特性说明

**重要说明**：`core/datastore-model` 模块是一个**单一功能模块**，专注于数据模型的定义和管理，不包含业务逻辑或平台特定实现。

---

## 3. 技术调研

### 3.1 技术选型

#### 3.1.1 序列化框架：kotlinx.serialization

**选择理由**：

- ✅ **KMP 原生支持**：kotlinx.serialization 是 Kotlin 官方提供的序列化框架，完全支持 KMP
- ✅ **编译时生成**：使用编译时注解生成序列化代码，性能优于反射方案
- ✅ **类型安全**：编译时类型检查，避免运行时错误
- ✅ **JSON 支持**：内置 JSON 序列化支持，无需额外依赖
- ✅ **多格式支持**：支持 JSON、CBOR、Protobuf 等多种格式

**替代方案对比**：

| 方案                  | 优点                               | 缺点                   | 结论        |
| --------------------- | ---------------------------------- | ---------------------- | ----------- |
| kotlinx.serialization | KMP 原生支持，编译时生成，类型安全 | 需要添加注解           | ✅ **选择** |
| Gson                  | 简单易用                           | 不支持 KMP，运行时反射 | ❌          |
| Jackson               | 功能强大                           | 不支持 KMP，运行时反射 | ❌          |
| Moshi                 | 类型安全                           | 不支持 KMP             | ❌          |

#### 3.1.2 时间类型：kotlinx.datetime

**选择理由**：

- ✅ **KMP 原生支持**：kotlinx.datetime 是 Kotlin 官方提供的时间库，完全支持 KMP
- ✅ **跨平台兼容**：在所有平台上行为一致，避免平台差异
- ✅ **类型安全**：使用 `Instant` 类型，避免字符串解析错误
- ✅ **ISO 8601 支持**：内置 ISO 8601 格式支持

**替代方案对比**：

| 方案             | 优点                     | 缺点               | 结论        |
| ---------------- | ------------------------ | ------------------ | ----------- |
| kotlinx.datetime | KMP 原生支持，跨平台兼容 | 需要额外依赖       | ✅ **选择** |
| java.time        | Java 标准库              | 不支持 JS/WASM     | ❌          |
| Date             | 简单                     | 已废弃，类型不安全 | ❌          |

### 3.2 架构模式

#### 3.2.1 领域模型与 DTO 分离

**设计原则**：

- **领域模型（Domain Model）**：应用内部使用的数据模型，使用 `Instant` 类型表示时间
- **DTO（Data Transfer Object）**：用于网络传输，使用 `String` 类型表示时间（ISO 8601 格式）
- **转换函数**：提供 `toDto()` 和 `toDomain()` 函数进行转换

**优势**：

- ✅ **关注点分离**：领域模型和传输逻辑分离
- ✅ **类型安全**：领域模型使用强类型，DTO 使用字符串兼容 API
- ✅ **向后兼容**：DTO 变更不影响领域模型

---

## 4. 架构设计

### 4.1 模块结构

```text
core/datastore-model/
├── src/
│   └── commonMain/
│       └── kotlin/tech/zhifu/app/myhub/datastore/model/
│           ├── Card.kt              # 卡片实体和元数据
│           ├── CardDto.kt           # 卡片 DTO 和转换函数
│           ├── Tag.kt               # 标签实体
│           ├── Template.kt          # 模板实体
│           ├── User.kt              # 用户实体和偏好设置
│           ├── Statistics.kt        # 统计信息
│           └── SearchFilter.kt      # 搜索筛选条件
└── build.gradle.kts
```

### 4.2 核心组件

#### 4.2.1 Card（卡片）

**领域模型**：

```kotlin
@Serializable
data class Card(
    val id: String,
    val type: CardType,
    val title: String? = null,
    val content: String,
    val author: String? = null,
    val source: String? = null,
    val language: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastReviewedAt: Instant? = null,
    val metadata: CardMetadata? = null,
    val userId: String? = null
)
```

**CardType 枚举**：

```kotlin
@Serializable
enum class CardType {
    QUOTE,      // 引言卡片
    CODE,       // 代码片段
    IDEA,       // 想法
    ARTICLE,    // 文章
    DICTIONARY, // 字典
    CHECKLIST   // 待办清单
}
```

**CardMetadata**：

```kotlin
@Serializable
data class CardMetadata(
    val quoteAuthor: String? = null,
    val quoteCategory: String? = null,
    val codeLanguage: String? = null,
    val codeSnippet: String? = null,
    val articleUrl: String? = null,
    val articleSummary: String? = null,
    val articleImageUrl: String? = null,
    val wordPronunciation: String? = null,
    val wordDefinition: String? = null,
    val wordExample: String? = null,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val ideaPriority: String? = null,
    val ideaStatus: String? = null
)
```

#### 4.2.2 CardDto（卡片 DTO）

**DTO 定义**：

```kotlin
@Serializable
data class CardDto(
    val id: String,
    val type: String, // 使用String便于API兼容
    val title: String? = null,
    val content: String,
    val author: String? = null,
    val source: String? = null,
    val language: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val createdAt: String, // ISO 8601格式
    val updatedAt: String,
    val lastReviewedAt: String? = null,
    val metadata: CardMetadataDto? = null
)
```

**转换函数**：

```kotlin
// 领域模型转 DTO
fun Card.toDto(): CardDto

// DTO 转领域模型
fun CardDto.toDomain(): Card
```

#### 4.2.3 Tag（标签）

```kotlin
@Serializable
data class Tag(
    val id: String,
    val name: String,
    val color: String? = null,
    val description: String? = null,
    val cardCount: Int = 0,
    val createdAt: Instant,
    val userId: String? = null
)
```

#### 4.2.4 Template（模板）

```kotlin
@Serializable
data class Template(
    val id: String,
    val name: String,
    val cardType: CardType,
    val defaultContent: String? = null,
    val isSystemTemplate: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val userId: String? = null
)
```

#### 4.2.5 User（用户）

```kotlin
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Instant,
    val preferences: UserPreferences? = null
)

@Serializable
data class UserPreferences(
    val theme: String = "dark",
    val language: String = "en",
    val defaultCardType: CardType? = null,
    val autoSync: Boolean = true,
    val syncInterval: Long = 3600000L
)
```

#### 4.2.6 Statistics（统计信息）

```kotlin
@Serializable
data class Statistics(
    val totalCards: Int,
    val favoriteCards: Int,
    val recentEdits: Int,
    val cardsByType: Map<CardType, Int>,
    val cardsByTag: Map<String, Int>
)
```

#### 4.2.7 SearchFilter（搜索筛选）

```kotlin
@Serializable
data class SearchFilter(
    val query: String? = null,
    val cardTypes: List<CardType> = emptyList(),
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean? = null,
    val sortBy: SortBy = SortBy.CREATED_AT_DESC,
    val dateRange: DateRange? = null
)

@Serializable
enum class SortBy {
    CREATED_AT_ASC,
    CREATED_AT_DESC,
    UPDATED_AT_ASC,
    UPDATED_AT_DESC,
    TITLE_ASC,
    TITLE_DESC
}
```

### 4.3 数据模型特点

1. **不可变性**：所有数据类都是 `data class`，确保不可变
2. **可选字段**：使用默认参数提供可选字段
3. **类型安全**：使用枚举类型（`CardType`、`SortBy`）确保类型安全
4. **序列化支持**：所有模型都支持 JSON 序列化
5. **用户关联**：从数据库版本 2 开始，所有实体都支持 `userId` 字段

---

## 5. 实现细节

### 5.1 序列化配置

**JSON 配置**：

```kotlin
val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
    isLenient = true
    prettyPrint = false
}
```

### 5.2 时间类型转换

**领域模型转 DTO**：

```kotlin
fun Card.toDto(): CardDto {
    return CardDto(
        // ...
        createdAt = createdAt.toString(), // Instant 转 ISO 8601 String
        updatedAt = updatedAt.toString(),
        lastReviewedAt = lastReviewedAt?.toString()
    )
}
```

**DTO 转领域模型**：

```kotlin
fun CardDto.toDomain(): Card {
    return Card(
        // ...
        createdAt = Instant.parse(createdAt), // ISO 8601 String 转 Instant
        updatedAt = Instant.parse(updatedAt),
        lastReviewedAt = lastReviewedAt?.let { Instant.parse(it) }
    )
}
```

### 5.3 枚举类型处理

**领域模型**：使用枚举类型（`CardType`）

**DTO**：使用字符串类型（`String`），便于 API 兼容

**转换**：

```kotlin
// 领域模型转 DTO
val typeString = card.type.name // "QUOTE"

// DTO 转领域模型
val cardType = CardType.valueOf(dto.type) // CardType.QUOTE
```

### 5.4 请求/响应 DTO

**CreateCardRequest**：

```kotlin
@Serializable
data class CreateCardRequest(
    val type: String,
    val title: String? = null,
    val content: String,
    val author: String? = null,
    val source: String? = null,
    val language: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val metadata: CardMetadataDto? = null
)

fun CreateCardRequest.toDomain(): Card
```

**UpdateCardRequest**：

```kotlin
@Serializable
data class UpdateCardRequest(
    val title: String? = null,
    val content: String? = null,
    val author: String? = null,
    val tags: List<String>? = null,
    val isFavorite: Boolean? = null,
    val metadata: CardMetadataDto? = null
)
```

---

## 6. 实施计划

### 6.1 实施阶段

#### 阶段 1：核心数据模型定义（已完成）

- ✅ Card 实体和 CardMetadata
- ✅ Tag 实体
- ✅ Template 实体
- ✅ User 实体和 UserPreferences
- ✅ Statistics 实体
- ✅ SearchFilter 实体

#### 阶段 2：DTO 和转换函数（已完成）

- ✅ CardDto 和转换函数
- ✅ CreateCardRequest 和 UpdateCardRequest
- ✅ 时间类型转换（Instant ↔ String）
- ✅ 枚举类型转换（CardType ↔ String）

#### 阶段 3：序列化支持（已完成）

- ✅ kotlinx.serialization 配置
- ✅ JSON 序列化/反序列化
- ✅ 所有数据模型添加 `@Serializable` 注解

#### 阶段 4：单元测试（已完成）

- ✅ Card 和 CardMetadata 测试
- ✅ CardDto 转换和序列化测试
- ✅ Tag 和 TagStats 测试
- ✅ SearchFilter 和 DateRange 测试
- ✅ User 和 UserPreferences 测试
- ✅ Statistics 和 CardStatistics 测试
- ✅ Template 测试

#### 阶段 5：用户关联支持（已完成）

- ✅ 所有实体添加 `userId` 字段
- ✅ DTO 转换函数支持 `userId`
- ✅ 测试用例更新

### 6.2 里程碑

| 里程碑           | 目标日期   | 状态      |
| ---------------- | ---------- | --------- |
| 核心数据模型完成 | 2026-01-13 | ✅ 已完成 |
| DTO 和转换完成   | 2026-01-13 | ✅ 已完成 |
| 序列化支持完成   | 2026-01-13 | ✅ 已完成 |
| 单元测试完成     | 2026-01-13 | ✅ 已完成 |
| 用户关联支持完成 | 2026-01-13 | ✅ 已完成 |

---

## 7. 风险评估

### 7.1 技术风险

#### 7.1.1 序列化兼容性风险

**风险描述**：API 变更导致序列化/反序列化失败

**影响**：高

**缓解措施**：

- ✅ 使用 `ignoreUnknownKeys = true` 忽略未知字段
- ✅ DTO 字段使用可选类型（`String?`）
- ✅ 提供版本化的 API 端点

#### 7.1.2 时间类型转换风险

**风险描述**：时间格式不一致导致解析失败

**影响**：中

**缓解措施**：

- ✅ 统一使用 ISO 8601 格式
- ✅ 提供错误处理机制
- ✅ 单元测试覆盖时间转换场景

#### 7.1.3 跨平台兼容性风险

**风险描述**：不同平台对数据类型的支持不同

**影响**：低

**缓解措施**：

- ✅ 使用 KMP 原生类型（`Instant`、`String`）
- ✅ 避免使用平台特定类型
- ✅ 跨平台测试

### 7.2 业务风险

#### 7.2.1 数据模型变更风险

**风险描述**：数据模型变更影响现有功能

**影响**：中

**缓解措施**：

- ✅ 使用可选字段支持向后兼容
- ✅ 提供数据迁移机制
- ✅ 版本化数据模型

### 7.3 维护风险

#### 7.3.1 代码重复风险

**风险描述**：领域模型和 DTO 字段重复

**影响**：低

**缓解措施**：

- ✅ 使用转换函数减少重复
- ✅ 代码生成工具（如需要）
- ✅ 定期代码审查

---

## 8. 附录

### 8.1 相关文档

- [MyHub 数据库模块方案设计](../datastore-database/docs/myhub-datastore-database-infra-v1.0.md)
- [MyHub 数据源模块方案设计](../datastore-datasource-local/README.md)
- [kotlinx.serialization 官方文档](https://github.com/Kotlin/kotlinx.serialization)
- [kotlinx.datetime 官方文档](https://github.com/Kotlin/kotlinx-datetime)

### 8.2 代码示例

#### 8.2.1 创建卡片

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

#### 8.2.2 DTO 转换

```kotlin
// 领域模型转 DTO
val dto = card.toDto()

// DTO 转领域模型
val card = dto.toDomain()
```

#### 8.2.3 JSON 序列化

```kotlin
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json.encodeToString(card)
val card = Json.decodeFromString<Card>(json)
```

### 8.3 术语表

| 术语                  | 说明                                                           |
| --------------------- | -------------------------------------------------------------- |
| 领域模型              | 应用内部使用的数据模型，使用强类型（如 `Instant`、`CardType`） |
| DTO                   | 数据传输对象，用于网络传输，使用字符串类型（如 `String`）      |
| kotlinx.serialization | Kotlin 官方序列化框架，支持 KMP                                |
| kotlinx.datetime      | Kotlin 官方时间库，支持 KMP                                    |
| Instant               | 时间戳类型，表示 UTC 时间点                                    |
| ISO 8601              | 国际标准时间格式，如 "2023-01-01T00:00:00Z"                    |

### 8.4 常见问题

#### Q1: 为什么领域模型使用 `Instant`，而 DTO 使用 `String`？

**A**: 领域模型使用 `Instant` 提供类型安全和跨平台兼容性，DTO 使用 `String` 便于 API 兼容和序列化。转换函数负责两者之间的转换。

#### Q2: 如何处理数据模型变更？

**A**: 使用可选字段支持向后兼容，提供数据迁移机制，版本化数据模型。

#### Q3: 是否支持其他序列化格式？

**A**: kotlinx.serialization 支持多种格式（JSON、CBOR、Protobuf），当前使用 JSON，可根据需要扩展。

---
