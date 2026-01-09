# MyHub Dashboard 设计文档

本文档定义了 MyHub Dashboard 功能模块的设计规范，基于 Stitch 设计稿（`feature/dashboard/docs/myhub_feature_dashboard_stitch/`）实现，严格遵循 Material Design 3 规范。

## 📋 目录

- [概述](#概述)
- [响应式布局](#响应式布局)
- [统计卡片](#统计卡片)
- [卡片样式](#卡片样式)
- [响应式布局规则](#响应式布局规则)
- [Material Design 3 适配](#material-design-3-适配)
- [HTML 到 Compose 映射](#html-到-compose-映射)
- [实现检查清单](#实现检查清单)
- [相关文件](#相关文件)

## 📖 概述

Dashboard 是 MyHub 应用的主界面，展示用户的卡片集合。设计遵循以下原则：

1. **响应式设计**：根据窗口大小自动调整布局（1 列/2 列/3 列）
2. **Material Design 3**：严格遵循 M3 设计规范
3. **性能优化**：使用 Lazy 布局实现高效滚动
4. **用户体验**：流畅的动画和交互反馈

### 设计稿参考

- **Stitch 设计文档**：`feature/dashboard/docs/myhub_feature_dashboard_stitch/`
- **HTML 原型**：`feature/dashboard/docs/myhub_feature_dashboard_stitch/code.html`
- **设计截图**：
  - `dashboard-1-columns.png` - 1 列布局
  - `dashboard-2-columns.png` - 2 列布局
  - `dashboard-3-columns.png` - 3 列布局
  - `screen.png` - 完整屏幕截图

## 📐 响应式布局

### 列数规则

根据窗口大小类（WindowSizeClass）自动调整卡片列数：

| 窗口大小类 | 列数 | HTML 类        | 断点          | 使用场景        |
| ---------- | ---- | -------------- | ------------- | --------------- |
| Compact    | 1    | `columns-1`    | < 600dp       | 移动端手机      |
| Medium     | 2    | `md:columns-2` | 600dp - 840dp | 平板/大屏手机   |
| Expanded   | 3    | `lg:columns-3` | > 840dp       | 桌面端/大屏平板 |

**实现代码：**

```kotlin
val columns = when {
    sizeClass.isCompact -> 1
    sizeClass.isMedium -> 2
    sizeClass.isExpanded -> 3
    else -> 3
}
```

### 间距规则

- **水平间距（gap）**：`24.dp` (对应 HTML `gap-6`)
- **垂直间距（space-y）**：`24.dp` (对应 HTML `space-y-6`)
- **内容内边距**：`24.dp` (对应 HTML `px-6 py-6`)

**实现代码：**

```kotlin
LazyVerticalStaggeredGrid(
    horizontalArrangement = Arrangement.spacedBy(24.dp), // gap-6
    verticalItemSpacing = 24.dp, // space-y-6
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp) // px-6 py-6
)
```

## 📊 统计卡片

### 显示规则

统计卡片根据窗口大小类显示在不同位置：

| 窗口大小类      | 显示位置                                    | HTML 规则                     | 实现方式                                                   |
| --------------- | ------------------------------------------- | ----------------------------- | ---------------------------------------------------------- |
| Compact (1 列)  | Grid 内部，作为第一个 item，随列表滚动      | `grid grid-cols-3 gap-4 mb-8` | `StatsCardsRow` 在 `LazyVerticalStaggeredGrid` 第一个 item |
| Medium (2 列)   | Grid 上方，作为独立一行，显示在所有卡片头部 | 无 HTML 规则                  | 统计信息在 Grid 上方独立显示，不随列表滚动                 |
| Expanded (3 列) | 工具栏中，与搜索栏同一行                    | `hidden lg:flex`              | 统计信息显示在工具栏右侧，与搜索栏同一行                   |

**关键规则：**

- **1 列**：统计卡片在 Grid 内部，作为第一个 item，**随卡片列表上下滑动**
- **2 列**：统计信息在 Grid 上方，作为独立一行，**不随列表滚动**
- **3 列**：统计信息在工具栏中，与搜索栏同一行，**不随列表滚动**

### 统计卡片样式

#### 1 列：统计卡片（Grid 内部）

- **布局**：3 列网格（`grid grid-cols-3`）
- **间距**：`16.dp` (对应 HTML `gap-4`)
- **底部间距**：`32.dp` (对应 HTML `mb-8`)
- **圆角**：`12.dp` (`rounded-xl`)
- **边框**：`1.dp` + `outline.copy(alpha = 0.5f)` (对应 HTML `border border-slate-200`)
- **阴影**：`1.dp` elevation (对应 HTML `shadow-sm`)
- **内边距**：`16.dp` (对应 HTML `p-4`)
- **背景色**：`MaterialTheme.colorScheme.surface` (白色，对应 HTML `bg-white`)

#### 2 列和 3 列：统计信息（工具栏中）

- **布局**：水平排列，带分隔符
- **圆角**：`12.dp` (`rounded-xl`)
- **边框**：`1.dp` + `outline.copy(alpha = 0.5f)`
- **高度**：`44.dp` (与搜索栏高度一致)
- **内边距**：`16.dp` (水平)
- **背景色**：`MaterialTheme.colorScheme.surface`
- **统计项样式**：
  - 圆点：`8.dp` 圆形，颜色分别为 `emerald-500` 和 `amber-500`
  - 文本：`labelSmall` 字体，`onSurfaceVariant` 颜色
  - 分隔符：`1.dp` 垂直线，`outline.copy(alpha = 0.5f)`

**实现代码：**

```kotlin
// 1列：统计卡片
@Composable
fun StatsCardsRow(statistics: Statistics, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp), // mb-8 = 32.dp
        horizontalArrangement = Arrangement.spacedBy(16.dp) // gap-4 = 16.dp
    ) {
        StatCard(..., modifier = Modifier.weight(1f))
        StatCard(..., modifier = Modifier.weight(1f))
        StatCard(..., modifier = Modifier.weight(1f))
    }
}

// 2列和3列：统计信息（工具栏中）
@Composable
fun DashboardToolbar(...) {
    if (sizeClass.isMedium || sizeClass.isExpanded) {
        Surface(...) {
            Row(...) {
                // 统计项 1
                Row(...) {
                    Box(...) // 圆点
                    Text(...) // 统计文本
                }
                // 分隔符
                Box(...) // 分隔线
                // 统计项 2
                Row(...) {
                    Box(...) // 圆点
                    Text(...) // 统计文本
                }
            }
        }
    }
}
```

## 🎨 卡片样式

### 基础样式

所有卡片遵循以下基础样式规则：

| 属性           | HTML 类                   | Compose 实现                                     | Material 3 规范          |
| -------------- | ------------------------- | ------------------------------------------------ | ------------------------ |
| 圆角           | `rounded-xl`              | `RoundedCornerShape(12.dp)`                      | Medium 圆角              |
| 边框           | `border border-slate-200` | `BorderStroke(1.dp, outline.copy(alpha = 0.5f))` | Outline 颜色，50% 透明度 |
| 阴影（默认）   | `shadow-sm`               | `elevation = 1.dp`                               | Elevation 1              |
| 阴影（Hover）  | `hover:shadow-md`         | `elevation = 4.dp`                               | Elevation 4              |
| 背景色（默认） | `bg-white`                | `MaterialTheme.colorScheme.surface`              | Surface 颜色             |
| 内边距（标准） | `p-6`                     | `padding(24.dp)`                                 | 标准内边距               |
| 内边距（紧凑） | `p-5`                     | `padding(20.dp)`                                 | 紧凑内边距               |

### 卡片类型特定样式

#### QuoteCard（引用卡片）

- **背景色**：`#fdfbf7` (浅米色)
  - 浅色模式：`Color(0xFFfdfbf7)`
  - 深色模式：`Color(0xFF1e2025)` (深灰色)
- **内边距**：`24.dp` (`p-6`)
- **分类标签**：
  - 背景：`bg-amber-100` (`Color(0xFFFEF3C7)`)
  - 文字：`text-amber-700` (`Color(0xFF92400E)`)
  - 内边距：`px-2 py-1` (`8.dp` 水平，`4.dp` 垂直)
  - 圆角：`4.dp` (`rounded`)
- **引用文本**：
  - 字体：Serif 字体，斜体
  - 大小：`text-xl` (`titleLarge`)
  - 颜色：`text-slate-800` (`Color(0xFF1e293b)`)
  - 行高：`leading-relaxed` (1.5 倍)
- **分隔线**：`border-slate-200`，`8.dp` 垂直间距

#### IdeaCard（想法卡片）

- **背景色**：`#FEF9E7` (yellow-50)
  - 浅色模式：`Color(0xFFFEF9E7)`
  - 深色模式：`Color(0xFF2A261C)` (深棕色)
- **内边距**：`20.dp` (`p-5`)
- **边框**：`#FDE68A` (yellow-200)，`1.dp` 宽度
- **圆点**：
  - 大小：`8.dp` (`w-2 h-2`)
  - 颜色：`#F59E0B` (yellow-500)
  - 形状：`rounded-full`
- **标签**：
  - 文字：`text-yellow-700` (`Color(0xFF92400E)`)
  - 字体：`text-xs` (`labelSmall`)，`font-medium`
- **时间戳**：`text-yellow-700/60`，带 `schedule` 图标

#### CodeCard（代码卡片）

- **内边距**：`24.dp` (`p-6`)
- **代码块**：
  - 背景：`bg-slate-50` (`surfaceVariant.copy(alpha = 0.5f)`)
  - 边框：`border-slate-100` (`outline.copy(alpha = 0.2f)`)，`1.dp` 宽度
  - 内边距：`12.dp` (`p-3`)
  - 圆角：`8.dp` (`rounded-lg`)
  - 字体：Monospace，`bodySmall`
- **底部标识条**：`#3b82f6` (blue-500)，高度 `4.dp` (`h-1`)
- **标签样式**：`rounded-full`，`bg-slate-100` (`surfaceVariant`)

#### DictionaryCard（字典卡片）

- **内边距**：`24.dp` (`p-6`)
- **单词**：
  - 字体：Serif
  - 大小：`text-2xl` (`headlineMedium`)
  - 字重：`font-bold`
- **音标**：
  - 样式：斜体 (`italic`)
  - 颜色：`text-slate-500` (`onSurfaceVariant`)
  - 大小：`text-sm` (`bodySmall`)
  - 间距：`4.dp` (`mt-1`)
- **例句**：
  - 背景：`bg-slate-50` (`surfaceVariant.copy(alpha = 0.5f)`)
  - 边框：左侧 `2.dp` primary 颜色边框 (`border-l-2 border-primary`)
  - 内边距：`12.dp` (`p-3`)
  - 圆角：`8.dp` (`rounded-lg`)
  - 样式：斜体，`text-slate-500`

#### ArticleCard（文章卡片）

- **头部渐变**：`from-indigo-500 via-purple-500 to-pink-500`
  - 颜色列表：`[Color(0xFF6366f1), Color(0xFFa855f7), Color(0xFFec4899)]`
  - 方向：水平渐变 (`horizontalGradient`)
- **头部高度**：`128.dp` (`h-32`)
- **遮罩层**：`bg-black/20` (`Color.Black.copy(alpha = 0.2f)`)
- **标题**：白色，`titleLarge`，`font-bold`，`16.dp` 内边距
- **内容内边距**：`20.dp` (`p-5`)
- **摘要**：`bodySmall`，最多 3 行 (`line-clamp-3`)

### 卡片交互效果

- **Hover 阴影提升**：`shadow-sm` → `shadow-md` (1.dp → 4.dp)
- **编辑按钮显示**：Hover 时从 `opacity-0` 变为 `opacity-100`
- **点击交互**：使用 `clickable` modifier，无视觉反馈（`indication = null`）

## 📱 响应式布局规则

### Header 区域

- **位置**：Sticky（固定在顶部）
- **背景**：`bg-background-light/80` + `backdrop-blur-md` (80% 透明度 + 背景模糊)
- **布局**：
  - 移动端：垂直布局（`flex-col`）
  - 桌面端：水平布局（`md:flex-row md:items-center justify-between`)

### 工具栏区域

- **搜索栏**：
  - 移动端：全宽（`w-full`）
  - 桌面端：`flex-1` (占据剩余空间)
- **统计信息**：
  - 1 列：不显示在工具栏（显示在 Grid 内部，随列表滚动）
  - 2 列：显示在 Grid 上方，作为独立一行，不随列表滚动
  - 3 列：显示在工具栏右侧，与搜索栏同一行
- **视图切换**：始终显示在右侧

### 内容区域布局

**Grid 视图实现：**

```kotlin
// 2列时：统计信息在Grid上方显示，作为独立一行
if (sizeClass.isMedium && viewType == ViewType.GRID) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        StatsCardsRow(statistics = statistics)
    }
}

// 1列：统计卡片在Grid内部，作为第一个item，随列表滚动
// 2列和3列：统计信息不在Grid内部显示
DashboardGridView(
    cards = cards,
    columns = columns,
    statistics = if (sizeClass.isCompact) statistics else null, // 仅1列时显示统计卡片
    sizeClass = sizeClass,
    ...
)

// DashboardGridView 内部直接使用 LazyVerticalStaggeredGrid
// 注意：不能在 LazyColumn 中嵌套 LazyVerticalStaggeredGrid，会导致无限高度约束错误
LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(columns)) {
    // 1列：统计卡片作为第一个item，随列表滚动
    if (statistics != null && sizeClass.isCompact) {
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                StatsCardsRow(statistics = statistics)
            }
        }
    }
    items(cards) { ... }
}
```

## 🎯 Material Design 3 适配

### 颜色系统

| HTML 颜色              | Compose 颜色                                           | Material 3 语义  | 说明            |
| ---------------------- | ------------------------------------------------------ | ---------------- | --------------- |
| `primary: #2563eb`     | `MaterialTheme.colorScheme.primary`                    | Primary          | 主色            |
| `bg-white`             | `MaterialTheme.colorScheme.surface`                    | Surface          | 表面颜色        |
| `border-slate-200`     | `MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)` | Outline          | 边框颜色        |
| `text-slate-900`       | `MaterialTheme.colorScheme.onSurface`                  | OnSurface        | 文本颜色        |
| `text-slate-500`       | `MaterialTheme.colorScheme.onSurfaceVariant`           | OnSurfaceVariant | 次要文本颜色    |
| `emerald-500: #10b981` | `Color(0xFF10b981)`                                    | -                | 统计卡片绿点    |
| `amber-500: #f59e0b`   | `Color(0xFFf59e0b)`                                    | -                | 统计卡片橙点    |
| `yellow-500: #F59E0B`  | `Color(0xFFF59E0B)`                                    | -                | Idea 卡片圆点   |
| `blue-500: #3b82f6`    | `Color(0xFF3b82f6)`                                    | -                | Code 卡片底部条 |

### 字体系统

- **Display 字体**：Inter (对应 HTML `font-display`)
- **Serif 字体**：Noto Serif SC (对应 HTML `font-serif`)
- **Mono 字体**：JetBrains Mono (对应 HTML `font-mono`)

### 圆角系统

- **Small**：`4.dp` (`rounded`)
- **Medium**：`8.dp` (`rounded-lg`)
- **Large**：`12.dp` (`rounded-xl`)
- **Full**：`9999px` (`rounded-full`)

### 阴影系统

- **Elevation 1**：`1.dp` (对应 `shadow-sm`)
- **Elevation 2**：`2.dp`
- **Elevation 4**：`4.dp` (对应 `shadow-md`)
- **Elevation 8**：`8.dp` (对应 `shadow-lg`)

## 📋 HTML 到 Compose 映射

### 布局映射

| HTML 类                               | Compose 实现                        | 说明       |
| ------------------------------------- | ----------------------------------- | ---------- |
| `columns-1 md:columns-2 lg:columns-3` | `StaggeredGridCells.Fixed(columns)` | 响应式列数 |
| `gap-6`                               | `Arrangement.spacedBy(24.dp)`       | 水平间距   |
| `space-y-6`                           | `verticalItemSpacing = 24.dp`       | 垂直间距   |
| `px-6`                                | `padding(horizontal = 24.dp)`       | 水平内边距 |
| `py-4`                                | `padding(vertical = 16.dp)`         | 垂直内边距 |
| `p-6`                                 | `padding(24.dp)`                    | 标准内边距 |
| `p-5`                                 | `padding(20.dp)`                    | 紧凑内边距 |
| `p-4`                                 | `padding(16.dp)`                    | 小内边距   |
| `mb-8`                                | `padding(bottom = 32.dp)`           | 底部间距   |
| `gap-4`                               | `Arrangement.spacedBy(16.dp)`       | 小间距     |

### 样式映射

| HTML 类                   | Compose 实现                                                  | 说明           |
| ------------------------- | ------------------------------------------------------------- | -------------- |
| `rounded-xl`              | `RoundedCornerShape(12.dp)`                                   | 圆角           |
| `rounded-lg`              | `RoundedCornerShape(8.dp)`                                    | 中等圆角       |
| `rounded-full`            | `RoundedCornerShape(9999px)`                                  | 完全圆角       |
| `border border-slate-200` | `BorderStroke(1.dp, outline.copy(alpha = 0.5f))`              | 边框           |
| `shadow-sm`               | `elevation = 1.dp`                                            | 默认阴影       |
| `hover:shadow-md`         | `elevation = 4.dp` (hover)                                    | Hover 阴影     |
| `bg-white`                | `MaterialTheme.colorScheme.surface`                           | 白色背景       |
| `bg-[#fdfbf7]`            | `Color(0xFFfdfbf7)`                                           | Quote 卡片背景 |
| `bg-yellow-50`            | `Color(0xFFFEF9E7)`                                           | Idea 卡片背景  |
| `border-yellow-200`       | `Color(0xFFFDE68A)`                                           | Idea 卡片边框  |
| `bg-slate-50`             | `MaterialTheme.colorScheme.surfaceVariant`                    | 浅灰色背景     |
| `bg-slate-200`            | `MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)` | 中等灰色背景   |

### 字体映射

| HTML 类              | Compose 实现                                  | Material 3 Typography |
| -------------------- | --------------------------------------------- | --------------------- |
| `text-2xl font-bold` | `typography.headlineMedium + FontWeight.Bold` | Headline Medium       |
| `text-xl`            | `typography.titleLarge`                       | Title Large           |
| `text-lg`            | `typography.titleMedium`                      | Title Medium          |
| `text-sm`            | `typography.bodySmall`                        | Body Small            |
| `text-xs`            | `typography.labelSmall`                       | Label Small           |
| `font-serif`         | `FontFamily.Serif`                            | Serif 字体            |
| `font-mono`          | `FontFamily.Monospace`                        | Monospace 字体        |

## ✅ 实现检查清单

### 已实现

- [x] 响应式列数（1/2/3 列）
- [x] 卡片间距（24.dp）
- [x] 统计卡片 1 列显示（Grid 内部，随列表滚动）
- [x] 统计信息 2 列显示（Grid 上方，作为独立一行）
- [x] 统计信息 3 列显示（工具栏中，与搜索栏同一行）
- [x] 卡片基础样式（圆角、边框、阴影）
- [x] Header 响应式布局（移动端垂直，桌面端水平）
- [x] 工具栏响应式布局
- [x] Grid/List 视图切换
- [x] QuoteCard 背景色（#fdfbf7）
- [x] IdeaCard 背景色和边框（yellow-50 / yellow-200）
- [x] Hover 效果（阴影提升）
- [x] 编辑按钮 Hover 显示
- [x] 修复 LazyColumn 嵌套 LazyVerticalStaggeredGrid 导致的无限高度约束错误

### 待完善

- [ ] 卡片类型特定样式细节调整
- [ ] 渐变背景动画效果（Article 卡片）
- [ ] 卡片加载动画
- [ ] 搜索功能实现
- [ ] 筛选和排序功能

## 🔗 相关文件

### 源代码

- `feature/dashboard/src/commonMain/kotlin/.../DashboardScreen.kt` - Dashboard 主界面
- `feature/dashboard/src/commonMain/kotlin/.../DashboardViewModel.kt` - Dashboard ViewModel
- `feature/dashboard/src/commonMain/kotlin/.../DashboardUiState.kt` - Dashboard UI 状态

### 组件

- `component/card/` - 卡片组件实现
- `component/card/src/commonMain/kotlin/.../CardStyles.kt` - 卡片样式配置
- `component/card/src/commonMain/kotlin/.../CardComponent.kt` - 卡片组件统一入口

### 平台支持

- `core/platform-compose/src/commonMain/kotlin/.../WindowSize.kt` - 窗口大小类定义

### 设计文档

- `feature/dashboard/docs/myhub_feature_dashboard_stitch/` - Stitch 设计文档
  - `code.html` - HTML 原型
  - `dashboard-1-columns.png` - 1 列布局截图
  - `dashboard-2-columns.png` - 2 列布局截图
  - `dashboard-3-columns.png` - 3 列布局截图
  - `screen.png` - 完整屏幕截图

## 📚 参考

- **Material Design 3**：https://m3.material.io/
- **Compose Multiplatform**：https://www.jetbrains.com/lp/compose-multiplatform/
- **Stitch 设计文档**：`feature/dashboard/docs/myhub_feature_dashboard_stitch/`

## 📝 更新日志

### v1.0.2 (当前版本)

- ✅ 调整统计信息显示规则：2 列时显示在 Grid 上方，作为独立一行
- ✅ 严格校准所有卡片样式，对齐设计稿：
  - QuoteCard：背景色、分类标签（amber-100/amber-700）、引用文本样式
  - IdeaCard：背景色（yellow-50）、边框（yellow-200）、圆点（yellow-500）
  - CodeCard：代码块背景（slate-50）、底部蓝色条（4.dp）
  - DictionaryCard：单词字体、音标样式、例句样式
  - ArticleCard：渐变头部、遮罩、间距
- ✅ 更新设计文档，记录所有样式细节和间距规则

### v1.0.1

- ✅ 调整统计信息显示规则：2 列时显示在工具栏中，与搜索栏同一行
- ✅ 1 列时统计卡片在 Grid 内部，随列表滚动
- ✅ 2 列和 3 列时统计信息在工具栏中，不随列表滚动

### v1.0

- ✅ 完成响应式布局实现（1/2/3 列）
- ✅ 完成统计卡片布局（1 列在 Grid 内部，3 列在工具栏）
- ✅ 完成卡片基础样式实现
- ✅ 完成 Header 和工具栏响应式布局
- ✅ 修复 LazyColumn 嵌套 LazyVerticalStaggeredGrid 导致的无限高度约束错误
- ✅ 完成设计文档重构，基于 Stitch 设计稿
