# Card Component 模块架构设计

## 📋 概述

Card Component 模块提供了 MyHub 应用中使用的各种卡片组件。这些组件遵循 Material Design 3 设计规范，支持深色/浅色主题，并提供了丰富的交互效果。每个卡片组件都独立为单独文件，便于维护、测试和扩展。

## 🎯 设计目标

1. **组件化设计**：每个卡片组件独立文件，便于维护和测试
2. **Material Design 3**：遵循 Material 3 设计规范，使用 Material3 组件
3. **主题支持**：自动适配深色/浅色主题
4. **交互体验**：提供 hover 效果、点击反馈等交互
5. **可扩展性**：易于接入真实数据和扩展新功能
6. **跨平台支持**：基于 Compose Multiplatform，支持 Android、iOS、Desktop、Web
7. **测试友好**：每个组件可独立测试

## 🏗️ 架构设计

### 组件化架构

```text
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  DashboardScreen, CardGrid, etc.                        │
│  └─ 使用 CardComponent (统一入口)                        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Card Component Layer                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  CardComponent (统一组件，通过 Koin DI 路由)    │  │
│  └──────────────────────────────────────────────────┘  │
│                     │                                    │
│  ┌──────────────────▼────────────────────────────────┐  │
│  │  CardComponentFactory Map (Koin 注入)            │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐         │  │
│  │  │QuoteCard │ │CodeCard  │ │IdeaCard  │ ...     │  │
│  │  └──────────┘ └──────────┘ └──────────┘         │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Material3 Foundation                      │
│  Card, Surface, Text, Icon, etc.                       │
└─────────────────────────────────────────────────────────┘
```

### 基于 Koin DI 的组件注册机制

Card Component 模块使用 **Koin 依赖注入**实现组件类型的自动路由，符合**开闭原则**：

#### 核心组件

1. **CardComponentFactory** - 卡片组件工厂类型

   - 文件：`CardComponentFactory.kt`
   - 定义统一的组件工厂函数签名

2. **CardComponent** - 统一卡片组件

   - 文件：`component/CardComponent.kt`
   - 通过 Koin 注入组件映射表
   - 根据 `Card.type` 自动选择对应的组件

3. **CardModule** - Koin 模块
   - 文件：`di/CardModule.kt`
   - 注册所有卡片类型到组件工厂的映射
   - 集中管理所有卡片组件注册

#### 工作流程

```text
CardComponent(card)
    ↓
通过 Koin 注入 Map<CardType, CardComponentFactory>
    ↓
根据 card.type 查找对应的工厂函数
    ↓
调用工厂函数渲染对应的卡片组件
```

#### 优势

- ✅ **符合开闭原则**：新增卡片类型只需在 Koin 模块中注册，无需修改使用代码
- ✅ **完全解耦**：使用 Koin DI 实现组件与使用方的解耦
- ✅ **集中管理**：所有卡片组件注册集中在一个地方
- ✅ **类型安全**：编译时检查，运行时错误提示清晰
- ✅ **易于测试**：可以轻松 Mock 或替换组件映射

### 组件设计原则

1. **单一职责**：每个卡片组件只负责一种类型的卡片展示
2. **独立文件**：每个组件独立为单独文件，便于维护和测试
3. **统一接口**：所有组件遵循相同的 API 模式（`modifier` 参数）
4. **主题适配**：使用 `isSystemInDarkTheme()` 自动适配主题
5. **交互统一**：所有组件支持 hover 效果和点击交互
6. **纯函数组件**：卡片组件是展示型组件，不包含业务逻辑，数据通过参数传入，事件通过回调传出

### MVVM 架构原则

#### 架构分层

Card Component 模块遵循清晰的架构分层原则：

```text
┌─────────────────────────────────────────┐
│  页面级别 (Screen Level)                │
│  DashboardScreen, CardDetailScreen     │
│  └─ 使用 MVVM ✅                        │
│     - DashboardViewModel               │
│     - CardDetailViewModel              │
│     - 管理业务状态                      │
│     - 与 Repository 交互                │
└─────────────────────────────────────────┘
           │
           │ 传递数据和回调
           ▼
┌─────────────────────────────────────────┐
│  组件级别 (Component Level)            │
│  QuoteCard, CodeCard, etc.            │
│  └─ 纯函数组件 ✅                        │
│     - 数据通过参数传入                  │
│     - 事件通过回调传出                  │
│     - 不包含业务逻辑                    │
│     - 不直接访问 Repository             │
└─────────────────────────────────────────┘
```

#### 为什么卡片组件不使用 MVVM？

**卡片组件是展示型组件（Presentational Component）**，具有以下特性：

1. **纯函数设计**

   ```kotlin
   @Composable
   fun QuoteCard(
       card: Card,                    // 数据输入（来自父组件）
       onEdit: (Card) -> Unit = {},   // 事件输出（回调给父组件）
       onFavorite: (Card) -> Unit = {},
       modifier: Modifier = Modifier
   ) {
       // 只有 UI 展示逻辑，没有业务逻辑
   }
   ```

2. **无状态管理**

   - 状态来自参数，不内部管理
   - 不持有业务状态
   - 不依赖 ViewModel

3. **高度可复用**
   - 不依赖特定数据源
   - 可在任何页面使用
   - 易于测试和维护

#### MVVM 的适用场景

**MVVM 应该用在页面级别（Screen Level）**，适用于：

1. **需要与 Repository 交互**

   ```kotlin
   class DashboardViewModel(
       private val cardRepository: CardRepository
   ) {
       val uiState: StateFlow<DashboardUiState>
       fun loadCards()
       fun toggleFavorite(cardId: String)
   }
   ```

2. **有复杂的状态管理**

   - 多个数据源的组合
   - 状态转换和计算
   - 加载、错误、成功等状态

3. **有业务逻辑处理**

   - 数据验证
   - 业务规则
   - 数据转换

4. **需要数据同步**
   - 与服务器同步
   - 本地缓存管理
   - 实时更新

#### 实际架构示例

**页面级别（使用 MVVM）**：

```kotlin
// feature/dashboard/DashboardViewModel.kt
class DashboardViewModel(
    private val cardRepository: CardRepository
) {
    val uiState: StateFlow<DashboardUiState>

    fun toggleFavorite(cardId: String) {
        // 业务逻辑：更新收藏状态
        cardRepository.updateCard(...)
    }
}

// feature/dashboard/DashboardScreen.kt
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyVerticalGrid(...) {
        items(uiState.cards) { card ->
            // 组件级别：纯函数，不需要 ViewModel
            QuoteCard(
                card = card,
                onFavorite = { viewModel.toggleFavorite(it.id) }
            )
        }
    }
}
```

**组件级别（纯函数）**：

```kotlin
// component/card/QuoteCard.kt
@Composable
fun QuoteCard(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 只有 UI 展示，没有业务逻辑
    Card(...) {
        IconButton(onClick = { onFavorite(card) }) {
            Icon(...)
        }
    }
}
```

#### 设计原则总结

| 层级   | 组件类型       | 是否使用 MVVM | 职责                                           |
| ------ | -------------- | ------------- | ---------------------------------------------- |
| 页面级 | Screen         | ✅ 使用       | 管理业务状态、与 Repository 交互、处理业务逻辑 |
| 组件级 | Card Component | ❌ 不使用     | UI 展示、数据展示、事件回调                    |

**判断标准**：

- ✅ **需要 MVVM**：需要与 Repository 交互、有复杂状态管理、有业务逻辑处理
- ❌ **不需要 MVVM**：纯 UI 展示、数据来自参数、事件通过回调、无业务逻辑

#### 架构优势

采用这种分层架构的优势：

1. **职责清晰**

   - 页面负责业务逻辑
   - 组件负责 UI 展示
   - 各司其职，易于理解

2. **易于测试**

   - 组件是纯函数，输入输出明确
   - ViewModel 可独立测试
   - 无需 Mock 复杂依赖

3. **高度复用**

   - 组件可在多处使用
   - 不依赖特定数据源
   - 易于组合和扩展

4. **解耦设计**
   - 组件与业务逻辑分离
   - 组件不依赖 Repository
   - 便于维护和重构

## 📦 核心组件

### Card 扩展方法

提供卡片显示相关的扩展方法和属性，支持多语言和统一的数据展示。

**文件：** `Card.kt`

**扩展方法：**

#### 1. `Card.getDisplayTitle()` - 获取显示标题

根据卡片类型返回合适的标题，支持多语言。

```kotlin
@Composable
fun Card.getDisplayTitle(): String
```

**特性：**

- 如果卡片有 `title`，直接返回
- 如果没有 `title`，根据卡片类型返回默认标题
- 支持多语言（通过 Compose Resources）
- Quote 类型优先使用 `metadata?.quoteAuthor`

**使用示例：**

```kotlin
@Composable
fun ListViewItem(card: Card) {
    val cardTitle = card.getDisplayTitle()
    Text(text = cardTitle)
}
```

#### 2. `Card.getContentPreview()` - 获取内容预览

截取卡片内容的前 N 个字符作为预览。

```kotlin
fun Card.getContentPreview(maxLength: Int = 80): String
```

**特性：**

- 默认截取前 80 个字符
- 如果内容超过 `maxLength`，自动添加 "..."
- 纯函数，计算成本低

**使用示例：**

```kotlin
val preview = card.getContentPreview()
// 或自定义长度
val shortPreview = card.getContentPreview(maxLength = 50)
```

#### 3. `Card.typeIconColor` - 获取图标颜色

根据卡片类型返回对应的图标颜色。

```kotlin
val Card.typeIconColor: Color
```

**颜色映射：**

- `QUOTE` → 紫色 (#8B5CF6)
- `CODE` → 绿色 (#10B981)
- `IDEA` → 琥珀色 (#F59E0B)
- `ARTICLE` → 蓝色 (#3B82F6)
- `DICTIONARY` → 青色 (#06B6D4)
- `CHECKLIST` → 红色 (#EF4444)

**使用示例：**

```kotlin
val iconColor = card.typeIconColor
Box(
    modifier = Modifier.background(iconColor.copy(alpha = 0.2f))
) {
    // 图标内容
}
```

#### 4. `Card.typeIconText` - 获取图标文本

根据卡片类型返回对应的图标文本标识。

```kotlin
val Card.typeIconText: String
```

**文本映射：**

- `QUOTE` → "Q"
- `CODE` → "C"
- `IDEA` → "I"
- `ARTICLE` → "A"
- `DICTIONARY` → "D"
- `CHECKLIST` → "✓"

**使用示例：**

```kotlin
val iconText = card.typeIconText
Text(
    text = iconText,
    color = card.typeIconColor
)
```

### 多语言支持

Card 扩展方法支持多语言，通过 Compose Resources 实现。

**资源文件结构：**

```
composeResources/
├── values/strings.xml              # 英语（默认）
├── values-zh-rCN/strings.xml      # 简体中文
├── values-zh-rTW/strings.xml       # 繁体中文
└── values-ja/strings.xml           # 日语
```

**资源键：**

- `component_card_type_quote` - Quote
- `component_card_type_code` - Code Snippet
- `component_card_type_idea` - Idea
- `component_card_type_article` - Article
- `component_card_type_dictionary` - Word
- `component_card_type_checklist` - Checklist

**在 Dashboard 中的使用：**

```kotlin
@Composable
private fun ListViewItem(card: Card) {
    // 使用扩展方法获取显示信息
    val cardTitle = card.getDisplayTitle()  // 支持多语言
    val contentPreview = card.getContentPreview()
    val iconColor = card.typeIconColor
    val iconText = card.typeIconText

    // 使用这些信息渲染 UI
    Row {
        // 图标
        Box(background = iconColor) {
            Text(text = iconText)
        }
        // 标题和预览
        Column {
            Text(text = cardTitle)
            Text(text = contentPreview)
        }
    }
}
```

### CardComponent - 统一卡片组件

统一的卡片组件入口，根据 `Card.type` 自动选择对应的组件进行渲染。

**文件：** `component/CardComponent.kt`

**特性：**

- 通过 Koin DI 注入组件映射表
- 自动路由到对应的卡片组件
- 统一的 API 接口
- 符合开闭原则：新增卡片类型无需修改使用代码

**API：**

```kotlin
@Composable
fun CardComponent(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
)
```

**使用示例：**

```kotlin
items(cards) { card ->
    CardComponent(
        card = card,
        onEdit = { viewModel.editCard(it.id) },
        onFavorite = { viewModel.toggleFavorite(it.id) },
        onCardClick = { viewModel.viewCard(it.id) }
    )
}
```

### CardComponentFactory - 卡片组件工厂

定义卡片组件工厂函数类型，用于 Koin 模块注册。

**文件：** `CardComponentFactory.kt`

**定义：**

```kotlin
typealias CardComponentFactory = @Composable (
    card: Card,
    onEdit: (Card) -> Unit,
    onFavorite: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier
) -> Unit
```

### CardModule - Koin 模块

注册所有卡片类型到组件工厂的映射。

**文件：** `di/CardModule.kt`

**注册示例：**

```kotlin
val cardModule = module {
    single<Map<CardType, CardComponentFactory>> {
        mapOf(
            CardType.QUOTE to { card, onEdit, onFavorite, onCardClick, modifier ->
                QuoteCard(card, onEdit, onFavorite, onCardClick, modifier)
            },
            // ... 其他类型
        )
    }
}
```

**新增卡片类型：**
只需在 `CardModule.kt` 中添加一行注册即可，无需修改使用代码。

### 1. QuoteCard - 引言卡片

用于展示名言、引言等文本内容，支持分类标签和作者信息。

**文件：** `QuoteCard.kt`

**特性：**

- 特殊的背景色（浅色：#fdfbf7，深色：#1e2025）
- Serif 字体显示引言内容
- 分类标签（如 "LITERATURE"）
- 作者信息和收藏按钮
- Hover 时显示编辑按钮

**API：**

```kotlin
@Composable
fun QuoteCard(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
)
```

**数据绑定：**

- `card.content` → 引言内容
- `card.metadata?.quoteAuthor` 或 `card.author` → 作者
- `card.metadata?.quoteCategory` → 分类标签
- `card.updatedAt` → 日期（自动格式化为 "Oct 24, 2023"）
- `card.isFavorite` → 收藏状态

### 2. CodeCard - 代码卡片

用于展示代码片段，支持语法高亮和标签。

**文件：** `CodeCard.kt`

**特性：**

- 代码块使用等宽字体（Monospace）
- 深色代码背景（#282c34）
- 标签系统（如 #javascript, #algorithms）
- 底部蓝色标识条（1dp 高度）
- 代码图标标识

**API：**

```kotlin
@Composable
fun CodeCard(
    modifier: Modifier = Modifier
)
```

### 3. IdeaCard - 想法卡片

用于展示想法、灵感等内容，使用黄色主题。

**文件：** `IdeaCard.kt`

**特性：**

- 黄色主题背景（浅色：#fef3c7，深色：#2A261C）
- 黄色边框
- 时间戳显示（如 "Added 2 hours ago"）
- 圆点标识

**API：**

```kotlin
@Composable
fun IdeaCard(
    modifier: Modifier = Modifier
)
```

### 4. ArticleCard - 文章卡片

用于展示文章摘要，支持渐变头部和来源链接。

**文件：** `ArticleCard.kt`

**特性：**

- 渐变头部（Indigo → Purple → Pink）
- 半透明遮罩效果
- 文章摘要文本（最多 3 行）
- "Read Source" 链接按钮
- 作者头像组（重叠显示）

**API：**

```kotlin
@Composable
fun ArticleCard(
    modifier: Modifier = Modifier
)
```

### 5. DictionaryCard - 字典卡片

用于展示单词定义，支持发音和例句。

**文件：** `DictionaryCard.kt`

**特性：**

- Serif 字体显示单词
- 音标显示（IPA）
- 单词定义
- 例句展示（带左边框高亮）
- 发音按钮

**API：**

```kotlin
@Composable
fun DictionaryCard(
    modifier: Modifier = Modifier
)
```

### 6. ChecklistCard - 清单卡片

用于展示待办事项列表，支持勾选状态。

**文件：** `ChecklistCard.kt`

**特性：**

- 清单标题和图标
- 可勾选的清单项（`ChecklistItem` 私有组件）
- 选中状态显示（删除线）
- Hover 效果（边框高亮）

**API：**

```kotlin
@Composable
fun ChecklistCard(
    modifier: Modifier = Modifier
)

// 私有组件
@Composable
private fun ChecklistItem(
    text: String,
    checked: Boolean
)
```

## 📁 模块结构

```text
component/card/
├── README.md                    # 本文档
├── build.gradle.kts            # 构建配置
├── docs/                       # 设计文档
│   ├── card_data_integration.md
│   └── card_data_synchronization.md
└── src/commonMain/
    ├── composeResources/          # 多语言资源文件
    │   ├── values/                # 默认语言（英语）
    │   │   └── strings.xml
    │   ├── values-zh-rCN/         # 简体中文
    │   │   └── strings.xml
    │   ├── values-zh-rTW/         # 繁体中文
    │   │   └── strings.xml
    │   └── values-ja/             # 日语
    │       └── strings.xml
    └── kotlin/tech/zhifu/app/myhub/
        ├── component/
        │   └── CardComponent.kt    # 统一卡片组件（通过 Koin DI 路由）
        └── component/card/
            ├── Card.kt              # Card 扩展方法（显示信息、多语言支持）
            ├── CardComponentFactory.kt  # 卡片组件工厂类型定义
            ├── QuoteCard.kt           # 引言卡片组件（已支持数据驱动）
            ├── CodeCard.kt            # 代码卡片组件
            ├── IdeaCard.kt            # 想法卡片组件
            ├── ArticleCard.kt         # 文章卡片组件
            ├── DictionaryCard.kt      # 字典卡片组件
            ├── ChecklistCard.kt        # 清单卡片组件（包含 ChecklistItem）
            ├── di/
            │   └── CardModule.kt      # Koin 模块（注册所有卡片组件）
            └── utils/
                └── CardDateFormatter.kt # 日期格式化工具
```

## 🔧 实现细节

### 统一的视觉风格

所有卡片组件共享以下设计特性：

- **圆角**：12dp 圆角（`RoundedCornerShape(12.dp)`）
- **边框**：1dp 边框，50% 透明度（`MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)`）
- **阴影**：默认 1dp，hover 时 4dp
- **间距**：内边距 20-24dp
- **主题适配**：使用 `isSystemInDarkTheme()` 自动适配深色/浅色主题

### 交互效果实现

所有卡片组件使用统一的交互实现模式：

```kotlin
@Composable
fun ExampleCard(modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { /* TODO */ }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 4.dp else 1.dp
        )
    ) {
        // 卡片内容
    }
}
```

**关键特性：**

- **Hover 检测**：使用 `MutableInteractionSource` 和 `collectIsHoveredAsState()`
- **动态阴影**：hover 时阴影从 1dp 提升到 4dp
- **点击交互**：所有卡片支持点击（当前为 TODO，待接入真实数据）

### 主题适配

所有组件使用 `isSystemInDarkTheme()` 检测当前主题：

```kotlin
val isDark = isSystemInDarkTheme()
val backgroundColor = if (isDark) {
    Color(0xFF1e2025)  // 深色主题颜色
} else {
    Color(0xFFfdfbf7)  // 浅色主题颜色
}
```

## 💡 使用示例

### 1. 使用统一组件（推荐）

使用 `CardComponent` 统一组件，自动根据卡片类型路由到对应的组件：

```kotlin
import tech.zhifu.app.myhub.component.CardComponent

@Composable
fun CardGrid(cards: List<Card>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(cards) { card ->
            CardComponent(
                card = card,
                onEdit = { /* 编辑逻辑 */ },
                onFavorite = { /* 收藏逻辑 */ },
                onCardClick = { /* 点击逻辑 */ }
            )
        }
    }
}
```

**优势：**

- ✅ 无需手动判断卡片类型
- ✅ 新增卡片类型无需修改使用代码
- ✅ 符合开闭原则

### 2. 在 Dashboard 中使用

```kotlin
// feature/dashboard/src/commonMain/kotlin/.../DashboardScreen.kt
import tech.zhifu.app.myhub.component.CardComponent

LazyVerticalGrid(
    columns = GridCells.Fixed(columns),
    contentPadding = PaddingValues(24.dp),
    horizontalArrangement = Arrangement.spacedBy(24.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
) {
    items(uiState.recentCards) { card ->
        CardComponent(
            card = card,
            onEdit = { viewModel.editCard(it.id) },
            onFavorite = { viewModel.toggleFavorite(it.id) },
            onCardClick = { viewModel.viewCard(it.id) }
        )
    }
}
```

### 3. 直接使用单个组件

如果需要直接使用特定的卡片组件（如 QuoteCard）：

```kotlin
import tech.zhifu.app.myhub.component.card.QuoteCard

@Composable
fun QuoteCardList(cards: List<Card>) {
    LazyColumn {
        items(cards.filter { it.type == CardType.QUOTE }) { card ->
            QuoteCard(
                card = card,
                onEdit = { /* 编辑逻辑 */ },
                onFavorite = { /* 收藏逻辑 */ },
                onCardClick = { /* 点击逻辑 */ }
            )
        }
    }
}
```

### 4. 新增卡片类型

假设新增 `NoteCard`：

1. **创建组件文件**：`NoteCard.kt`

   ```kotlin
   @Composable
   fun NoteCard(
       card: Card,
       onEdit: (Card) -> Unit = {},
       onFavorite: (Card) -> Unit = {},
       onCardClick: (Card) -> Unit = {},
       modifier: Modifier = Modifier
   ) {
       // 组件实现
   }
   ```

2. **在 CardModule 中注册**：

   ```kotlin
   // di/CardModule.kt
   CardType.NOTE to { card, onEdit, onFavorite, onCardClick, modifier ->
       NoteCard(card, onEdit, onFavorite, onCardClick, modifier)
   }
   ```

3. **完成**：`CardComponent` 自动支持新类型，无需修改使用代码

## 🔄 数据接入状态

### 当前状态

- ✅ **QuoteCard**：已支持数据驱动 API，使用 `Card` 数据模型
- ⏳ **其他卡片组件**：暂未实现数据驱动 API，仍使用示例数据

### 已实现的组件

#### QuoteCard

已完全接入真实数据，支持：

- 使用 `Card` 数据模型
- 数据映射（content、author、category、date、isFavorite）
- 日期格式化（使用 `kotlinx-datetime`）
- 回调处理（onEdit、onFavorite、onCardClick）

**使用示例：**

```kotlin
QuoteCard(
    card = myCard,
    onEdit = { /* 编辑逻辑 */ },
    onFavorite = { /* 收藏逻辑 */ },
    onCardClick = { /* 点击逻辑 */ }
)
```

### 待实现的组件

其他卡片组件（CodeCard、IdeaCard、ArticleCard、DictionaryCard、ChecklistCard）计划按照相同模式接入数据。

### 未来 API 设计

计划为每个组件添加数据模型参数，支持真实数据绑定：

#### QuoteCard

```kotlin
@Composable
fun QuoteCard(
    quote: Quote,                    // 数据模型
    onEdit: (Quote) -> Unit = {},     // 编辑回调
    onFavorite: (Quote) -> Unit = {}, // 收藏回调
    modifier: Modifier = Modifier
)

data class Quote(
    val id: String,
    val content: String,
    val author: String,
    val category: String,
    val date: Long,
    val isFavorite: Boolean
)
```

#### CodeCard

```kotlin
@Composable
fun CodeCard(
    code: CodeSnippet,                // 数据模型
    onEdit: (CodeSnippet) -> Unit = {},
    modifier: Modifier = Modifier
)

data class CodeSnippet(
    val id: String,
    val title: String,
    val code: String,
    val language: String,
    val tags: List<String>
)
```

#### IdeaCard

```kotlin
@Composable
fun IdeaCard(
    idea: Idea,                       // 数据模型
    onEdit: (Idea) -> Unit = {},
    modifier: Modifier = Modifier
)

data class Idea(
    val id: String,
    val content: String,
    val createdAt: Long
)
```

#### ArticleCard

```kotlin
@Composable
fun ArticleCard(
    article: Article,                 // 数据模型
    onReadSource: (Article) -> Unit = {},
    modifier: Modifier = Modifier
)

data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val sourceUrl: String?,
    val authors: List<String>
)
```

#### DictionaryCard

```kotlin
@Composable
fun DictionaryCard(
    word: Word,                       // 数据模型
    onPronounce: (Word) -> Unit = {},
    modifier: Modifier = Modifier
)

data class Word(
    val id: String,
    val word: String,
    val pronunciation: String,
    val definition: String,
    val example: String?
)
```

#### ChecklistCard

```kotlin
@Composable
fun ChecklistCard(
    checklist: Checklist,            // 数据模型
    onItemToggle: (String, Boolean) -> Unit = {},
    modifier: Modifier = Modifier
)

data class Checklist(
    val id: String,
    val title: String,
    val items: List<ChecklistItem>
)

data class ChecklistItem(
    val id: String,
    val text: String,
    val checked: Boolean
)
```

## 🧪 测试

### 测试计划

Card Component 模块计划包含完整的测试套件，覆盖所有组件和交互。

### 测试结构（计划）

```text
component/card/src/commonTest/kotlin/tech/zhifu/app/myhub/component/card/
├── QuoteCardTest.kt              # 引言卡片测试
├── CodeCardTest.kt               # 代码卡片测试
├── IdeaCardTest.kt               # 想法卡片测试
├── ArticleCardTest.kt            # 文章卡片测试
├── DictionaryCardTest.kt         # 字典卡片测试
├── ChecklistCardTest.kt          # 清单卡片测试
└── test/
    └── MockHelpers.kt            # Mock 工具类
```

### 测试覆盖范围（计划）

#### UI 渲染测试

- ✅ 组件正常渲染
- ✅ 深色/浅色主题适配
- ✅ 不同屏幕尺寸适配
- ✅ 数据绑定正确显示

#### 交互行为测试

- ✅ Hover 效果（阴影变化）
- ✅ 点击交互
- ✅ 编辑按钮显示/隐藏
- ✅ 状态切换（如 ChecklistItem 的勾选状态）

#### 数据绑定测试

- ✅ 空数据处理
- ✅ 长文本截断
- ✅ 特殊字符处理
- ✅ 日期格式化

### 测试工具（计划）

**MockHelpers** - 共享的 Mock 对象

- `createTestQuote()` - 创建测试引言
- `createTestCodeSnippet()` - 创建测试代码片段
- `createTestIdea()` - 创建测试想法
- `createTestArticle()` - 创建测试文章
- `createTestWord()` - 创建测试单词
- `createTestChecklist()` - 创建测试清单

### 运行测试（计划）

```bash
# 运行所有平台的测试
./gradlew :component:card:allTests

# 运行特定平台的测试
./gradlew :component:card:jvmTest
./gradlew :component:card:jsTest
./gradlew :component:card:iosSimulatorArm64Test
```

## 🚀 落地计划

### 阶段 1：基础组件搭建 ✅

- [x] 创建所有卡片组件文件
- [x] 实现统一的视觉风格
- [x] 实现 hover 效果和交互
- [x] 支持深色/浅色主题
- [x] 组件独立为单独文件

### 阶段 2：Koin DI 组件注册机制 ✅

- [x] 创建 CardComponentFactory 类型定义
- [x] 创建统一的 CardComponent 组件
- [x] 创建 CardModule Koin 模块
- [x] 在主 Koin 配置中引入 cardModule
- [x] 更新 DashboardScreen 使用 CardComponent
- [x] 实现基于 Koin DI 的组件自动路由

### 阶段 3：数据模型设计和接入 🔄

- [x] 定义卡片数据模型（使用 `Card` 数据模型）
- [x] 设计组件 API（参数、回调）
- [x] QuoteCard 实现数据驱动 API
- [x] 创建日期格式化工具（CardDateFormatter）
- [x] 在 Dashboard 中接入真实数据
- [ ] 其他卡片组件实现数据驱动 API
- [x] 实现编辑、收藏等交互回调
- [x] 优化性能（使用 remember 缓存）

### 阶段 4：单元测试 🔄

- [ ] 添加 UI 渲染测试
- [ ] 添加交互行为测试
- [ ] 添加数据绑定测试
- [ ] 创建 Mock 工具类
- [ ] 所有平台测试通过

### 阶段 5：预览支持 🔄

- [ ] 添加 Compose Preview 支持
- [ ] 为每个组件创建预览函数
- [ ] 支持深色/浅色主题预览
- [ ] 支持不同数据状态预览

### 阶段 6：动画和可访问性 🔄

- [ ] 添加卡片进入动画
- [ ] 添加状态切换动画
- [ ] 增强可访问性支持（Content Description）
- [ ] 支持键盘导航

## 📝 当前状态

**已完成**：

- ✅ 所有卡片组件已独立为单独文件
- ✅ 统一的视觉风格和交互效果
- ✅ 深色/浅色主题支持
- ✅ Hover 效果和动态阴影
- ✅ 组件化架构设计
- ✅ 基于 Koin DI 的组件注册机制
- ✅ CardComponent 统一组件
- ✅ QuoteCard 数据驱动 API 实现
- ✅ 日期格式化工具（CardDateFormatter）
- ✅ Dashboard 中接入真实数据
- ✅ Card 扩展方法（getDisplayTitle、getContentPreview、typeIconColor、typeIconText）
- ✅ 多语言支持（通过 Compose Resources）
- ✅ 列表视图支持（DashboardListView）

**进行中**：

- 🔄 其他卡片组件实现数据驱动 API（CodeCard、IdeaCard 等）

**待开始**：

- ⏳ 单元测试编写
- ⏳ Compose Preview 支持
- ⏳ 动画效果增强
- ⏳ 可访问性支持

## 🛠️ 依赖

```kotlin
dependencies {
    // Compose UI 依赖
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)  // 多语言资源支持
    // Material Icons 扩展
    implementation(compose.materialIconsExtended)

    // 数据模型依赖
    implementation(projects.core.datastoreModel)
    // kotlinx-datetime 用于日期格式化
    implementation(libs.kotlinx.datetime)

    // 依赖注入
    implementation(libs.koin.core)
    implementation(libs.koin.compose.viewmodel)
}
```

**Gradle 配置：**

```kotlin
compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.component.card.resources"
    generateResClass = always
}
```

## 📚 相关文档

- [Dashboard 模块 README](../../feature/dashboard/README.md)
- [Material Design 3 文档](https://m3.material.io/)
- [Compose Multiplatform 文档](https://www.jetbrains.com/lp/compose-multiplatform/)
- [项目整体架构](../../docs/myhub_architecture.md)

### 设计文档

- [卡片数据接入框架设计方案](./docs/card_data_integration.md) - 卡片组件数据接入的详细设计方案
- [卡片数据同步机制设计](./docs/card_data_synchronization.md) - 卡片内容变化时的多页面同步机制

## 🔗 设计参考

### 参考设计

- **Material Design 3**：遵循 Material 3 设计规范
- **Dashboard UI**：参考 `/Users/zzf/Downloads/stitch_dashboard/code.html`
- **卡片样式**：还原参考设计中的卡片样式和交互效果

### 设计原则

- **单一职责原则**：每个卡片组件只负责一种类型
- **开闭原则**：对扩展开放，对修改关闭
- **依赖倒置原则**：依赖 Material3 抽象组件
- **接口隔离原则**：每个组件独立，互不依赖

## 🤝 贡献

在修改卡片组件时，请确保：

1. ✅ 遵循 Material Design 3 设计规范
2. ✅ 支持深色/浅色主题
3. ✅ 添加适当的交互效果
4. ✅ 保持组件独立（不依赖其他卡片组件）
5. ✅ 通过 lint 检查
6. ✅ 更新相关文档

---

**最后更新：** 2024 年
