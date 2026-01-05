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
8. **解耦架构**：通过 CardComponent 接口实现组件解耦，符合开闭原则

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
│  │  Map<CardType, CardComponent> (Koin 注入)        │  │
│  │  ┌──────────────┐ ┌──────────────┐              │  │
│  │  │QuoteCard     │ │CodeCard      │              │  │
│  │  │Component     │ │Component     │ ...          │  │
│  │  └──────────────┘ └──────────────┘              │  │
│  └──────────────────────────────────────────────────┘  │
│                     │                                    │
│  ┌──────────────────▼────────────────────────────────┐  │
│  │  CardComponent 接口                                │  │
│  │  - CardComponent()                                 │  │
│  │  - getDisplayTitle()                               │  │
│  │  - getTypeIconColor()                              │  │
│  │  - getTypeIconText()                               │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Material3 Foundation                      │
│  Card, Surface, Text, Icon, etc.                       │
└─────────────────────────────────────────────────────────┘
```

### 基于 CardComponent 接口的解耦架构

Card Component 模块使用 **CardComponent 接口**实现组件类型的解耦，符合**开闭原则**和**单一职责原则**：

#### 核心组件

1. **CardComponent 接口** - 卡片组件接口

   - 文件：`CardComponent.kt`
   - 定义统一的组件接口，包含：
     - `CardComponent()` - 渲染卡片组件
     - `getDisplayTitle()` - 获取显示标题
     - `getTypeIconColor()` - 获取图标颜色
     - `getTypeIconText()` - 获取图标文本

2. **CardComponents** - 统一卡片组件入口

   - 文件：`CardComponents.kt`
   - 通过 Koin 注入组件映射表
   - 根据 `Card.type` 自动选择对应的组件
   - 提供 Card 扩展方法和属性

3. **CardModule** - Koin 模块
   - 文件：`di/CardModule.kt`
   - 注册所有卡片类型到 CardComponent 实现的映射
   - 集中管理所有卡片组件注册

4. **具体 Component 实现**
   - `QuoteCardComponent` - 引言卡片组件实现
   - `CodeCardComponent` - 代码卡片组件实现
   - `IdeaCardComponent` - 想法卡片组件实现
   - `ArticleCardComponent` - 文章卡片组件实现
   - `DictionaryCardComponent` - 字典卡片组件实现
   - `ChecklistCardComponent` - 清单卡片组件实现

#### 工作流程

```text
CardComponent(card)
    ↓
通过 Koin 注入 Map<CardType, CardComponent>
    ↓
根据 card.type 查找对应的 Component 实例
    ↓
调用 Component.CardComponent() 渲染对应的卡片组件
```

#### 优势

- ✅ **符合开闭原则**：新增卡片类型只需创建新的 Component 实现，无需修改使用代码
- ✅ **完全解耦**：使用接口实现组件与使用方的解耦
- ✅ **集中管理**：所有卡片组件注册集中在一个地方
- ✅ **类型安全**：编译时检查，运行时错误提示清晰
- ✅ **易于测试**：可以轻松 Mock 或替换组件映射
- ✅ **职责分离**：每种卡片类型的显示逻辑、图标样式等都在对应的 Component 中

### 组件设计原则

1. **单一职责**：每个卡片组件只负责一种类型的卡片展示
2. **独立文件**：每个组件独立为单独文件，便于维护和测试
3. **统一接口**：所有组件遵循 CardComponent 接口
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
       onCardClick: (Card) -> Unit = {},
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
2. **有复杂的状态管理**
3. **有业务逻辑处理**
4. **需要数据同步**

## 📦 核心组件

### CardComponent 接口

定义所有卡片组件必须实现的接口。

**文件：** `CardComponent.kt`

**接口定义：**

```kotlin
internal interface CardComponent {
    @Composable
    fun CardComponent(
        card: Card,
        onEdit: (Card) -> Unit = {},
        onFavorite: (Card) -> Unit = {},
        onCardClick: (Card) -> Unit = {},
        modifier: Modifier = Modifier,
    )

    @Composable
    fun getDisplayTitle(card: Card): String

    fun getTypeIconColor(): Color

    fun getTypeIconText(): String
}
```

**实现示例：**

```kotlin
class QuoteCardComponent : CardComponent {
    @Composable
    override fun CardComponent(
        card: Card,
        onEdit: (Card) -> Unit,
        onFavorite: (Card) -> Unit,
        onCardClick: (Card) -> Unit,
        modifier: Modifier
    ) = QuoteCard(
        card = card,
        onEdit = onEdit,
        onFavorite = onFavorite,
        onCardClick = onCardClick,
        modifier = modifier
    )

    @Composable
    override fun getDisplayTitle(card: Card): String {
        return card.metadata?.quoteAuthor
            ?: stringResource(Res.string.component_card_type_quote)
    }

    override fun getTypeIconColor(): Color {
        return Color(0xFF8B5CF6) // purple
    }

    override fun getTypeIconText(): String {
        return "Q"
    }
}
```

### CardComponents - 统一入口

统一的卡片组件入口，根据 `Card.type` 自动选择对应的组件进行渲染。

**文件：** `CardComponents.kt`

**特性：**

- 通过 Koin DI 注入组件映射表
- 自动路由到对应的卡片组件
- 统一的 API 接口
- 符合开闭原则：新增卡片类型无需修改使用代码
- 提供 Card 扩展方法和属性

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

### Card 扩展方法和属性

提供卡片显示相关的扩展方法和属性，支持多语言和统一的数据展示。

**文件：** `CardComponents.kt`

#### 1. `Card.displayTitle` - 获取显示标题

根据卡片类型返回合适的标题，支持多语言。

```kotlin
val Card.displayTitle: String
    @Composable
    get() = toComponent().getDisplayTitle(this)
```

**特性：**

- 如果卡片有 `title`，直接返回
- 如果没有 `title`，根据卡片类型返回默认标题
- 支持多语言（通过 Compose Resources）
- Quote 类型优先使用 `metadata?.quoteAuthor`
- 通过 CardComponent 接口获取，实现解耦

**使用示例：**

```kotlin
@Composable
fun ListViewItem(card: Card) {
    val cardTitle = card.displayTitle
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
    @Composable
    get() = toComponent().getTypeIconColor()
```

**特性：**

- 通过 CardComponent 接口获取，实现解耦
- 每种卡片类型有独立的颜色定义

**颜色映射：**

- `QUOTE` → 紫色 (#8B5CF6)
- `CODE` → 绿色 (#10B981)
- `IDEA` → 琥珀色 (#F59E0B)
- `ARTICLE` → 蓝色 (#3B82F6)
- `DICTIONARY` → 青色 (#06B6D4)
- `CHECKLIST` → 红色 (#EF4444)

**使用示例：**

```kotlin
@Composable
fun ListViewItem(card: Card) {
    val iconColor = card.typeIconColor
    Box(
        modifier = Modifier.background(iconColor.copy(alpha = 0.2f))
    ) {
        // 图标内容
    }
}
```

#### 4. `Card.typeIconText` - 获取图标文本

根据卡片类型返回对应的图标文本标识。

```kotlin
val Card.typeIconText: String
    @Composable
    get() = toComponent().getTypeIconText()
```

**特性：**

- 通过 CardComponent 接口获取，实现解耦
- 每种卡片类型有独立的文本定义

**文本映射：**

- `QUOTE` → "Q"
- `CODE` → "C"
- `IDEA` → "I"
- `ARTICLE` → "A"
- `DICTIONARY` → "D"
- `CHECKLIST` → "✓"

**使用示例：**

```kotlin
@Composable
fun ListViewItem(card: Card) {
    val iconText = card.typeIconText
    Text(
        text = iconText,
        color = card.typeIconColor
    )
}
```

#### 5. `Card.formatUpdatedTime()` - 格式化更新时间

格式化卡片的更新时间为 "Oct 24, 2023" 格式，支持多语言。

```kotlin
@Composable
fun Card.formatUpdatedTime(): String
```

**使用示例：**

```kotlin
@Composable
fun CardItem(card: Card) {
    Text(text = card.formatUpdatedTime())
}
```

#### 6. `Card.formatCreatedTime()` - 格式化创建时间

格式化卡片的创建时间为 "Oct 24, 2023" 格式，支持多语言。

```kotlin
@Composable
fun Card.formatCreatedTime(): String
```

### CardModule - Koin 模块

注册所有卡片类型到 CardComponent 实现的映射。

**文件：** `di/CardModule.kt`

**注册示例：**

```kotlin
val cardModule = module {
    single<Map<CardType, CardComponent>> {
        mapOf(
            CardType.QUOTE to QuoteCardComponent(),
            CardType.CODE to CodeCardComponent(),
            CardType.IDEA to IdeaCardComponent(),
            CardType.ARTICLE to ArticleCardComponent(),
            CardType.DICTIONARY to DictionaryCardComponent(),
            CardType.CHECKLIST to ChecklistCardComponent()
        )
    }
}
```

**新增卡片类型：**

只需：
1. 创建对应的 `XXXCardComponent` 类实现 `CardComponent` 接口
2. 在 `CardModule.kt` 中添加一行注册即可

无需修改使用代码。

### 具体卡片组件

#### 1. QuoteCard - 引言卡片

用于展示名言、引言等文本内容，支持分类标签和作者信息。

**文件：** `QuoteCard.kt`

**Component 实现：** `QuoteCardComponent.kt`

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

#### 2. CodeCard - 代码卡片

用于展示代码片段，支持语法高亮和标签。

**文件：** `CodeCard.kt`

**Component 实现：** `CodeCardComponent.kt`

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
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
)
```

#### 3. IdeaCard - 想法卡片

用于展示想法、灵感等内容，使用黄色主题。

**文件：** `IdeaCard.kt`

**Component 实现：** `IdeaCardComponent.kt`

**特性：**

- 黄色主题背景（浅色：#fef3c7，深色：#2A261C）
- 黄色边框
- 时间戳显示（如 "Added 2 hours ago"）
- 圆点标识

**API：**

```kotlin
@Composable
fun IdeaCard(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
)
```

#### 4. ArticleCard - 文章卡片

用于展示文章摘要，支持渐变头部和来源链接。

**文件：** `ArticleCard.kt`

**Component 实现：** `ArticleCardComponent.kt`

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
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
)
```

#### 5. DictionaryCard - 字典卡片

用于展示单词定义，支持发音和例句。

**文件：** `DictionaryCard.kt`

**Component 实现：** `DictionaryCardComponent.kt`

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
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
)
```

#### 6. ChecklistCard - 清单卡片

用于展示待办事项列表，支持勾选状态。

**文件：** `ChecklistCard.kt`

**Component 实现：** `ChecklistCardComponent.kt`

**特性：**

- 清单标题和图标
- 可勾选的清单项（`ChecklistItem` 私有组件）
- 选中状态显示（删除线）
- Hover 效果（边框高亮）

**API：**

```kotlin
@Composable
fun ChecklistCard(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
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
└── src/
    ├── commonMain/
    │   ├── composeResources/          # 多语言资源文件
    │   │   ├── values/                # 默认语言（英语）
    │   │   │   └── strings.xml
    │   │   ├── values-zh-rCN/         # 简体中文
    │   │   │   └── strings.xml
    │   │   ├── values-zh-rTW/         # 繁体中文
    │   │   │   └── strings.xml
    │   │   └── values-ja/             # 日语
    │   │       └── strings.xml
    │   └── kotlin/tech/zhifu/app/myhub/
    │       └── component/card/
    │           ├── CardComponent.kt           # CardComponent 接口定义
    │           ├── CardComponents.kt         # 统一入口和 Card 扩展方法
    │           ├── QuoteCard.kt              # 引言卡片组件
    │           ├── QuoteCardComponent.kt     # 引言卡片 Component 实现
    │           ├── CodeCard.kt               # 代码卡片组件
    │           ├── CodeCardComponent.kt      # 代码卡片 Component 实现
    │           ├── IdeaCard.kt               # 想法卡片组件
    │           ├── IdeaCardComponent.kt      # 想法卡片 Component 实现
    │           ├── ArticleCard.kt            # 文章卡片组件
    │           ├── ArticleCardComponent.kt   # 文章卡片 Component 实现
    │           ├── DictionaryCard.kt         # 字典卡片组件
    │           ├── DictionaryCardComponent.kt # 字典卡片 Component 实现
    │           ├── ChecklistCard.kt          # 清单卡片组件
    │           ├── ChecklistCardComponent.kt # 清单卡片 Component 实现
    │           └── di/
    │               └── CardModule.kt         # Koin 模块（注册所有卡片组件）
    └── devMain/                              # 预览支持
        └── kotlin/zhifu/app/myhub/component/card/
            ├── QuoteCard.dev.kt              # QuoteCard 预览
            ├── ArticleCard.dev.kt            # ArticleCard 预览
            ├── CodeCard.dev.kt               # CodeCard 预览
            ├── IdeaCard.dev.kt               # IdeaCard 预览
            ├── ChecklistCard.dev.kt         # ChecklistCard 预览
            └── DictionaryCard.dev.kt        # DictionaryCard 预览
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
fun ExampleCard(
    card: Card,
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCardClick(card) }
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
- **点击交互**：所有卡片支持点击回调

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
- `component_card_month_jan` ~ `component_card_month_dec` - 月份名称

## 💡 使用示例

### 1. 使用统一组件（推荐）

使用 `CardComponent` 统一组件，自动根据卡片类型路由到对应的组件：

```kotlin
import tech.zhifu.app.myhub.component.card.CardComponent

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
import tech.zhifu.app.myhub.component.card.CardComponent

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

### 3. 使用 Card 扩展方法和属性

```kotlin
@Composable
fun ListViewItem(card: Card) {
    // 使用扩展方法和属性获取显示信息
    val cardTitle = card.displayTitle  // 支持多语言
    val contentPreview = card.getContentPreview()
    val iconColor = card.typeIconColor
    val iconText = card.typeIconText
    val updatedTime = card.formatUpdatedTime()

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
            Text(text = updatedTime)
        }
    }
}
```

### 4. 直接使用单个组件

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

### 5. 新增卡片类型

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

2. **创建 Component 实现**：`NoteCardComponent.kt`

   ```kotlin
   class NoteCardComponent : CardComponent {
       @Composable
       override fun CardComponent(
           card: Card,
           onEdit: (Card) -> Unit,
           onFavorite: (Card) -> Unit,
           onCardClick: (Card) -> Unit,
           modifier: Modifier
       ) = NoteCard(
           card = card,
           onEdit = onEdit,
           onFavorite = onFavorite,
           onCardClick = onCardClick,
           modifier = modifier
       )

       @Composable
       override fun getDisplayTitle(card: Card): String {
           return card.title ?: stringResource(Res.string.component_card_type_note)
       }

       override fun getTypeIconColor(): Color {
           return Color(0xFF6366F1) // indigo
       }

       override fun getTypeIconText(): String {
           return "N"
       }
   }
   ```

3. **在 CardModule 中注册**：

   ```kotlin
   // di/CardModule.kt
   CardType.NOTE to NoteCardComponent()
   ```

4. **完成**：`CardComponent` 自动支持新类型，无需修改使用代码

## 🧪 Preview 支持

所有卡片组件都提供了 Preview 支持，位于 `devMain` source set 中。

### Preview 文件结构

```
src/devMain/kotlin/zhifu/app/myhub/component/card/
├── QuoteCard.dev.kt
├── ArticleCard.dev.kt
├── CodeCard.dev.kt
├── IdeaCard.dev.kt
├── ChecklistCard.dev.kt
└── DictionaryCard.dev.kt
```

### Preview 特性

每个 Preview 文件包含：

- **浅色主题预览** - 展示组件在浅色主题下的外观
- **深色主题预览** - 展示组件在深色主题下的外观
- **不同状态预览** - 如收藏状态、长文本等
- **示例数据生成** - 使用 `createSampleXXXCard()` 辅助函数

### 使用 Preview

在 Android Studio 或 IntelliJ IDEA 中：

1. 打开对应的 `.dev.kt` 文件
2. 点击 Preview 函数左侧的预览图标
3. 查看组件在不同主题和状态下的外观

## 🚀 落地计划

### 阶段 1：基础组件搭建 ✅

- [x] 创建所有卡片组件文件
- [x] 实现统一的视觉风格
- [x] 实现 hover 效果和交互
- [x] 支持深色/浅色主题
- [x] 组件独立为单独文件

### 阶段 2：CardComponent 接口架构 ✅

- [x] 创建 CardComponent 接口
- [x] 创建所有 Component 实现
- [x] 创建 CardModule Koin 模块
- [x] 在主 Koin 配置中引入 cardModule
- [x] 更新 DashboardScreen 使用 CardComponent
- [x] 实现基于接口的组件自动路由

### 阶段 3：Card 扩展方法和属性 ✅

- [x] 实现 `displayTitle` 扩展属性
- [x] 实现 `typeIconColor` 扩展属性
- [x] 实现 `typeIconText` 扩展属性
- [x] 实现 `getContentPreview()` 扩展方法
- [x] 实现 `formatUpdatedTime()` 扩展方法
- [x] 实现 `formatCreatedTime()` 扩展方法
- [x] 所有扩展方法通过 CardComponent 接口获取，实现解耦

### 阶段 4：Preview 支持 ✅

- [x] 添加 Compose Preview 支持
- [x] 为每个组件创建预览函数
- [x] 支持深色/浅色主题预览
- [x] 支持不同数据状态预览

### 阶段 5：单元测试 🔄

- [ ] 添加 UI 渲染测试
- [ ] 添加交互行为测试
- [ ] 添加数据绑定测试
- [ ] 创建 Mock 工具类
- [ ] 所有平台测试通过

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
- ✅ CardComponent 接口架构
- ✅ 所有 Component 实现
- ✅ Card 扩展方法和属性（通过接口解耦）
- ✅ 多语言支持（通过 Compose Resources）
- ✅ Preview 支持（devMain source set）
- ✅ Dashboard 中接入真实数据

**进行中**：

- 🔄 单元测试编写

**待开始**：

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
    implementation(compose.materialIconsExtended)
    implementation(compose.components.uiToolingPreview)  // Preview 支持

    // 数据模型依赖
    implementation(projects.core.datastoreModel)
    implementation(projects.core.platformCompose)
    
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
- **Dashboard UI**：参考设计中的卡片样式和交互效果

### 设计原则

- **单一职责原则**：每个卡片组件只负责一种类型
- **开闭原则**：对扩展开放，对修改关闭
- **依赖倒置原则**：依赖 CardComponent 接口抽象
- **接口隔离原则**：每个组件独立，互不依赖

## 🤝 贡献

在修改卡片组件时，请确保：

1. ✅ 遵循 Material Design 3 设计规范
2. ✅ 支持深色/浅色主题
3. ✅ 添加适当的交互效果
4. ✅ 保持组件独立（不依赖其他卡片组件）
5. ✅ 实现 CardComponent 接口
6. ✅ 在 CardModule 中注册
7. ✅ 添加 Preview 支持（devMain）
8. ✅ 通过 lint 检查
9. ✅ 更新相关文档

---

**最后更新：** 2024 年
