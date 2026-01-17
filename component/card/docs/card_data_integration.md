# 卡片数据接入框架设计方案

## 📋 概述

本文档描述了为卡片组件设计的数据接入框架方案。该方案旨在将卡片组件从使用硬编码示例数据迁移到使用真实的 `Card` 数据模型，同时保持向后兼容性。

## 🎯 设计目标

1. **向后兼容**：保留无参数版本，支持示例数据展示
2. **类型安全**：使用现有的 `Card` 数据模型（`tech.zhifu.app.myhub.datastore.model.Card`）
3. **灵活交互**：支持编辑、收藏等回调函数
4. **直接使用 Card**：不创建额外的显示数据模型，直接使用 `Card` 数据模型
5. **可扩展性**：框架设计可扩展到其他卡片组件

## 📐 API 设计

### 方案：单一函数 + 可选参数 ✅

```kotlin
@Composable
fun QuoteCard(
    card: Card? = null,                       // 可选数据，null 时使用示例数据
    onEdit: ((Card) -> Unit)? = null,          // 编辑回调
    onFavorite: ((Card) -> Unit)? = null,      // 收藏回调
    onCardClick: ((Card) -> Unit)? = null,     // 卡片点击回调
    modifier: Modifier = Modifier
)
```

**优点：**

- ✅ 单一入口点，维护简单
- ✅ 向后兼容（card 为 null 时使用示例数据）
- ✅ 避免维护两套逻辑

**实现要点：**

- 在组件内部处理 null 判断
- 使用示例数据作为默认值
- 回调函数使用安全调用操作符 `?.invoke()`

## 🔄 数据映射逻辑

### Card 字段到 UI 的映射

| Card 字段                      | UI 显示位置  | 处理逻辑                         |
| ------------------------------ | ------------ | -------------------------------- |
| `card.content`                 | 引言内容     | 直接显示（带引号格式）           |
| `card.metadata?.quoteAuthor`   | 作者         | 优先使用，否则使用 `card.author` |
| `card.metadata?.quoteCategory` | 分类标签     | 显示为 "LITERATURE" 等格式       |
| `card.updatedAt`               | 日期显示     | 格式化为 "Oct 24, 2023"          |
| `card.isFavorite`              | 收藏按钮状态 | 控制星标图标填充状态             |
| `card.id`                      | 回调参数     | 用于编辑、收藏等操作             |

### 直接使用 Card 数据模型

**不创建额外的显示数据模型**，直接在组件内部使用 `Card`：

```kotlin
@Composable
fun QuoteCard(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 直接使用 Card 的字段
    val author = remember(card) {
        card.metadata?.quoteAuthor
            ?: card.author
            ?: "Unknown Author"
    }

    val category = remember(card) {
        card.metadata?.quoteCategory ?: "GENERAL"
    }

    // 使用 Card 扩展方法格式化日期
    val formattedDate = card.formatUpdatedTime()

    // ... UI 实现
}
```

**优势：**

- ✅ 不增加额外的数据模型
- ✅ 减少数据转换开销
- ✅ 保持数据模型单一
- ✅ 易于维护
- ✅ 通过 CardComponent 接口实现解耦

## 📅 日期格式化

### 需求

将 `kotlin.time.Instant` 格式化为 "Oct 24, 2023" 格式。

### 实现方案

项目已使用 `kotlinx-datetime`，使用它进行跨平台日期格式化：

```kotlin
// component/card/src/commonMain/kotlin/tech/zhifu/app/myhub/component/card/utils/CardDateFormatter.kt

package tech.zhifu.app.myhub.component.card.utils

import kotlin.time.Instant
import kotlinx.datetime.Instant as KxInstant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 格式化卡片日期为 "Oct 24, 2023" 格式
 *
 * 将 kotlin.time.Instant 转换为 kotlinx.datetime.Instant 进行格式化
 */
fun kotlin.time.Instant.formatCardDate(): String {
    // 转换为 kotlinx.datetime.Instant
    val kxInstant = KxInstant.fromEpochSeconds(this.epochSeconds)

    // 转换为本地时间
    val localDateTime = kxInstant.toLocalDateTime(TimeZone.currentSystemDefault())

    // 格式化
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val monthName = monthNames.getOrNull(localDateTime.monthNumber - 1) ?: "Jan"
    val day = localDateTime.dayOfMonth
    val year = localDateTime.year

    return "$monthName $day, $year"
}
```

**关键点：**

- ✅ 使用 `kotlinx.datetime` 进行跨平台格式化
- ✅ `kotlin.time.Instant` 已做平台差异化处理
- ✅ 通过 `fromEpochSeconds()` 转换为 `kotlinx.datetime.Instant`
- ✅ 使用 `toLocalDateTime()` 转换为本地时间
- ✅ 无需平台特定实现

### 依赖要求

确保 `component/card/build.gradle.kts` 包含：

```kotlin
dependencies {
    // 数据模型依赖
    implementation(projects.datastoreModel)
    // kotlinx-datetime 用于日期格式化
    implementation(libs.kotlinx.datetime)
}
```

## 🏗️ 文件结构

```text
component/card/src/commonMain/kotlin/tech/zhifu/app/myhub/component/card/
├── QuoteCard.kt                    # 主组件（单一函数）
└── utils/
    └── CardDateFormatter.kt        # 日期格式化工具（通用实现）
```

## 💡 使用示例

### 示例 1：向后兼容（无参数）

```kotlin
@Composable
fun CardGrid() {
    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        item {
            QuoteCard()  // 使用示例数据
        }
    }
}
```

### 示例 2：接入真实数据

```kotlin
@Composable
fun CardGrid(cards: List<Card>) {
    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        items(cards.filter { it.type == CardType.QUOTE }) { card ->
            QuoteCard(
                card = card,
                onEdit = { card ->
                    // 导航到编辑页面
                    navigateToEdit(card.id)
                },
                onFavorite = { card ->
                    // 切换收藏状态
                    viewModel.toggleFavorite(card.id)
                },
                onCardClick = { card ->
                    // 查看详情
                    navigateToDetail(card.id)
                }
            )
        }
    }
}
```

### 示例 3：在 Dashboard 中使用

```kotlin
// feature/dashboard/src/commonMain/kotlin/.../DashboardScreen.kt
LazyVerticalGrid(columns = GridCells.Fixed(columns)) {
    items(uiState.quoteCards) { card ->
        QuoteCard(
            card = card,
            onEdit = { viewModel.editCard(it.id) },
            onFavorite = { viewModel.toggleFavorite(it.id) },
            onCardClick = { viewModel.viewCard(it.id) }
        )
    }
}
```

## 🔧 实现细节

### 1. 默认值处理

```kotlin
// 示例数据（用于向后兼容）
private val defaultQuoteCard = Card(
    id = "demo-quote",
    type = CardType.QUOTE,
    content = "\"The more that you read, the more things you will know. The more that you learn, the more places you'll go.\"",
    author = "Dr. Seuss",
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    metadata = CardMetadata(
        quoteCategory = "LITERATURE",
        quoteAuthor = "Dr. Seuss"
    )
)

@Composable
fun QuoteCard(
    card: Card? = null,
    onEdit: ((Card) -> Unit)? = null,
    onFavorite: ((Card) -> Unit)? = null,
    onCardClick: ((Card) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 使用示例数据或传入的数据
    val displayCard = card ?: defaultQuoteCard

    // ... UI 实现
}
```

### 2. 空值处理

```kotlin
// 在组件内部直接处理空值
val author = displayCard.metadata?.quoteAuthor
    ?: displayCard.author
    ?: "Unknown Author"

val category = displayCard.metadata?.quoteCategory
    ?: "GENERAL"

val formattedDate = displayCard.updatedAt.formatCardDate()
```

### 3. 回调处理

```kotlin
// 编辑按钮点击
if (isHovered && displayCard != defaultQuoteCard) {
    IconButton(
        onClick = {
            onEdit?.invoke(displayCard)
        },
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
    ) {
        Icon(...)
    }
}

// 收藏按钮点击
IconButton(
    onClick = {
        onFavorite?.invoke(displayCard)
    }
) {
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = "Favorite",
        tint = if (displayCard.isFavorite) Color(0xFFFFB020) else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// 卡片点击
Card(
    modifier = modifier
        .fillMaxWidth()
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                if (displayCard != defaultQuoteCard) {
                    onCardClick?.invoke(displayCard)
                }
            }
        ),
    ...
)
```

### 4. 性能优化

```kotlin
@Composable
fun QuoteCard(
    card: Card? = null,
    onEdit: ((Card) -> Unit)? = null,
    onFavorite: ((Card) -> Unit)? = null,
    onCardClick: ((Card) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 使用 remember 缓存显示卡片
    val displayCard = remember(card) {
        card ?: defaultQuoteCard
    }

    // 使用 remember 缓存格式化结果
    val formattedDate = remember(displayCard.updatedAt) {
        displayCard.updatedAt.formatCardDate()
    }

    // 使用 remember 缓存数据提取
    val author = remember(displayCard) {
        displayCard.metadata?.quoteAuthor
            ?: displayCard.author
            ?: "Unknown Author"
    }

    val category = remember(displayCard) {
        displayCard.metadata?.quoteCategory ?: "GENERAL"
    }

    // ... UI 实现
}
```

## 📋 实现步骤

### 阶段 1：准备工作 ✅

1. ✅ 检查依赖关系

   - 确认 `component/card` 已依赖 `datastore:model`
   - 确认已依赖 `kotlinx-datetime`
   - 已在 `build.gradle.kts` 中添加

2. ✅ 创建工具目录
   - 日期格式化功能已集成到 `CardComponents.kt` 中

### 阶段 2：日期格式化工具 ✅

1. ✅ 日期格式化功能已实现
   - 使用 `kotlinx-datetime` 进行跨平台格式化
   - 支持多语言月份名称（通过 Compose Resources）
   - 已集成到 `CardComponents.kt` 中

### 阶段 3：QuoteCard 重构 ✅

1. ✅ 函数签名已更新为单一函数 + 必需参数
   - `card: Card` 为必需参数（不再使用可选参数）
   - 所有回调函数都有默认值
2. ✅ 直接使用 `Card` 数据模型
3. ✅ 实现回调处理逻辑
4. ✅ 使用 `remember` 优化性能

### 阶段 4：CardComponent 接口架构 ✅

1. ✅ 创建 CardComponent 接口
2. ✅ 实现所有 Component 类
3. ✅ 更新 CardModule 使用接口
4. ✅ 实现 Card 扩展方法通过接口获取

### 阶段 5：测试和文档 ✅

1. ✅ 更新 README 文档
2. ✅ 添加使用示例
3. ✅ 添加单元测试
4. ✅ 创建 Mock 工具类
5. ✅ 验证所有功能

## ⚠️ 注意事项

### 1. 依赖关系

确保 `component/card/build.gradle.kts` 包含：

```kotlin
dependencies {
    // 数据模型依赖
    implementation(projects.datastoreModel)
    // kotlinx-datetime 用于日期格式化
    implementation(libs.kotlinx.datetime)
}
```

### 2. 日期格式化

- ✅ 使用 `kotlinx-datetime` 进行跨平台格式化
- ✅ `kotlin.time.Instant` 已做平台差异化处理
- ✅ 通过 `fromEpochSeconds()` 转换为 `kotlinx.datetime.Instant`
- ✅ 无需平台特定实现（expect/actual）

### 3. 空值处理

- `card` 参数可能为 null（使用示例数据）
- `metadata` 可能为 null
- `quoteAuthor` 和 `quoteCategory` 可能为 null
- 提供合理的默认值

### 4. 性能考虑

- 使用 `remember` 缓存计算结果
- 避免在 Composable 中进行复杂计算
- 数据提取在 `remember` 中进行

### 5. 向后兼容

- `card` 参数默认为 null，使用示例数据
- 确保现有代码无需修改
- 回调函数使用安全调用操作符

### 6. 直接使用 Card 的优势

- ✅ **不增加数据模型**：避免创建 QuoteDisplayData 等额外模型
- ✅ **减少转换开销**：直接使用 Card，无需数据转换
- ✅ **保持单一数据源**：Card 是唯一的数据模型
- ✅ **易于维护**：减少代码复杂度

## 🔄 后续扩展

该框架设计可扩展到其他卡片组件：

### CodeCard

```kotlin
@Composable
fun CodeCard(
    card: Card? = null,
    onEdit: ((Card) -> Unit)? = null,
    onCardClick: ((Card) -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

### IdeaCard

```kotlin
@Composable
fun IdeaCard(
    card: Card? = null,
    onEdit: ((Card) -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

### ArticleCard

```kotlin
@Composable
fun ArticleCard(
    card: Card? = null,
    onReadSource: ((Card) -> Unit)? = null,
    onCardClick: ((Card) -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

### DictionaryCard

```kotlin
@Composable
fun DictionaryCard(
    card: Card? = null,
    onPronounce: ((Card) -> Unit)? = null,
    onCardClick: ((Card) -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

### ChecklistCard

```kotlin
@Composable
fun ChecklistCard(
    card: Card? = null,
    onItemToggle: ((String, Boolean) -> Unit)? = null,
    onEdit: ((Card) -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

## 📊 方案优势

### 单一函数设计的优势

1. **维护简单**：只需维护一个函数，逻辑集中
2. **代码清晰**：单一入口点，易于理解
3. **向后兼容**：通过可选参数实现兼容
4. **减少重复**：避免维护两套相似的逻辑

### 直接使用 Card 的优势

1. **不增加模型**：避免创建额外的显示数据模型
2. **减少转换**：直接使用 Card，无需数据转换
3. **保持单一数据源**：Card 是唯一的数据模型
4. **易于维护**：减少代码复杂度

### 使用 kotlinx-datetime 的优势

1. **跨平台支持**：无需平台特定实现
2. **统一接口**：所有平台使用相同的 API
3. **项目已使用**：项目已引入 kotlinx-datetime
4. **易于维护**：单一实现，无需维护多平台代码

## ✅ 方案确认

采用方案：**单一函数 + 可选参数 + 直接使用 Card + kotlinx-datetime**

理由：

1. **维护简单**：单一函数，避免维护两套逻辑
2. **直接使用 Card**：不增加额外的数据模型，保持代码简洁
3. **跨平台日期格式化**：使用 kotlinx-datetime，无需平台特定实现
4. **项目一致性**：与项目现有架构保持一致

**文档版本：** v1.0  
**创建日期：** 2025 年  
**最后更新：** 2026 年
