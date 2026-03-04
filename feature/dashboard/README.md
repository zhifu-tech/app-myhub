# Dashboard 模块架构设计

## 📋 概述

Dashboard 模块是 MyHub 应用的主界面，提供数据概览、统计信息展示和快速访问功能。本模块采用响应式架构设计，基于 Kotlin Flow 实现实时数据更新，支持多平台（Android、iOS、Desktop、Web）统一体验。

## 🎯 设计目标

1. **数据概览**：提供卡片总数、最近编辑、收藏等关键统计信息
2. **响应式更新**：基于 Flow 的实时数据同步和更新
3. **多平台支持**：使用 Compose Multiplatform 实现跨平台 UI
4. **性能优化**：懒加载、分页加载、缓存机制
5. **用户体验**：直观的界面设计，快速访问常用功能
6. **可扩展性**：易于添加新的统计指标和功能模块

## 🏗️ 架构设计

### 分层架构

```text
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  DashboardScreen, DashboardHeader, DashboardToolbar    │
│  StatsCardsRow, StatCard                                │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              ViewModel Layer                             │
│  ┌──────────────────────────────────────────────────┐   │
│  │  DashboardViewModel                          │   │
│  │  - loadDashboardData()                         │   │
│  │  - refresh()                                    │   │
│  │  - sync()                                       │   │
│  │  - observeStatistics()                          │   │
│  │  - observeCards()                               │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Repository Layer                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ReactiveCardRepository                          │   │
│  │  - getAllCards(): List<Card>                     │   │
│  │  - observeAllCards(): Flow<List<Card>>          │   │
│  │  - observeFavoriteCards(): Flow<List<Card>>     │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ReactiveStatisticsRepository                    │   │
│  │  - refreshStatistics(): Statistics              │   │
│  │  - observeStatistics(): Flow<Statistics>        │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌──▼──────────┐ ┌──────────▼──────────┐
│  Local DB   │ │ Remote API  │ │  Cache Layer        │
│  (SQLite)   │ │ (Ktor)      │ │  (In-Memory)        │
└──────────────┘ └─────────────┘ └─────────────────────┘
```

### 数据流

```text
用户操作
   │
   ├─► 刷新按钮 ──► ViewModel.refresh() ──► Repository.getAllCards()
   │                                              │
   │                                              ├─► Remote API ──► 更新本地 DB
   │                                              │
   │                                              └─► 更新 Flow ──► UI 自动更新
   │
   └─► 自动同步 ──► Flow.observe() ──► 实时更新 UI
```

## 📦 核心组件

### 1. DashboardViewModel

```kotlin
/**
 * Dashboard ViewModel
 * 管理 Dashboard 页面的状态和业务逻辑
 */
class DashboardViewModel(
    private val cardRepository: ReactiveCardRepository,
    private val statisticsRepository: ReactiveStatisticsRepository,
    private val coroutineScope: CoroutineScope
) {
    val uiState: StateFlow<DashboardUiState>

    /**
     * 加载 Dashboard 数据
     * 同时监听统计信息和卡片数据的变化
     */
    private fun loadDashboardData()

    /**
     * 刷新 Dashboard 数据
     * 触发统计信息和卡片数据的刷新
     */
    fun refresh()

    /**
     * 同步数据（从服务器拉取最新数据）
     */
    fun sync()

    /**
     * 清除错误状态
     */
    fun clearError()
}
```

**关键特性**：

- **响应式数据流**：使用 Flow 监听数据变化，自动更新 UI
- **多数据源同步**：同时监听统计信息和卡片数据
- **错误处理**：完善的错误捕获和状态管理
- **性能优化**：懒加载、按需刷新

### 2. DashboardUiState

```kotlin
/**
 * 视图类型枚举
 */
enum class ViewType {
    GRID,   // 瀑布流视图
    LIST    // 列表视图
}

/**
 * Dashboard UI状态
 * 
 * 使用 sealed class 表示不同的状态，确保状态互斥和类型安全
 * 
 * 设计说明：
 * - InitialLoading: 首次加载，无数据可显示
 * - Content: 有数据的状态，可以同时显示数据和加载状态（刷新时）
 */
sealed class DashboardUiState {
    /**
     * 初始加载状态
     * 应用正在初始化或首次加载数据，无数据可显示
     */
    data class InitialLoading(
        val lastSyncTime: Long? = null
    ) : DashboardUiState()
    
    /**
     * 内容状态（有数据）
     * 应用已加载完成，可以正常显示数据
     * 支持同时显示数据和加载状态（如刷新时）
     */
    data class Content(
        val statistics: Statistics,
        val recentCards: List<Card>,
        val favoriteCards: List<Card>,
        val lastSyncTime: Long?,
        val viewType: ViewType = ViewType.GRID,
        val isRefreshing: Boolean = false,  // 刷新时仍显示数据
        val error: String? = null            // 错误时仍显示数据
    ) : DashboardUiState()
}
```

**状态说明**：

- **`InitialLoading`**: 初始加载状态，无数据可显示
    - `lastSyncTime`: 最后同步时间（如果有）

- **`Content`**: 内容状态，有数据可正常显示
    - `statistics`: 统计信息（总卡片数、最近编辑数、收藏数等）
    - `recentCards`: 最近编辑的卡片列表（最多 10 个）
    - `favoriteCards`: 收藏的卡片列表
    - `lastSyncTime`: 最后同步时间
    - `viewType`: 视图类型（Grid 或 List）
    - `isRefreshing`: 是否正在刷新（刷新时仍显示数据，只显示加载动画）
    - `error`: 错误信息（如果有，错误时仍显示数据）

**设计优势**：

- **类型安全**：使用 sealed class 确保编译时检查所有状态分支
- **状态互斥**：不可能同时处于多个状态（如 InitialLoading 和 Content）
- **清晰的状态转换**：状态转换逻辑明确，易于理解和维护
- **同时显示数据和加载状态**：刷新时数据保持可见，只显示加载动画，提供更好的用户体验
- **错误时保留数据**：错误时仍可显示已有数据，用户可以继续操作

**设计对比**：

相比传统的 `Loading`、`Ready`、`Error` 分离设计，新的 `InitialLoading` 和 `Content` 设计具有以下优势：

1. **更好的用户体验**：
    - 刷新时数据保持可见，用户不会看到空白屏幕
    - 错误时仍可查看和操作已有数据，而不是完全无法使用

2. **更符合实际场景**：
    - Dashboard 通常已有缓存数据，首次加载和刷新是不同的场景
    - 错误通常是临时性的，不应该完全阻止用户使用应用

3. **状态管理更简单**：
    - 不需要在多个状态间复制数据
    - `Content` 状态统一管理所有数据相关字段
    - `isRefreshing` 和 `error` 作为标志位，不影响数据展示

### 3. DashboardScreen

```kotlin
/**
 * Dashboard 主界面
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinInject<DashboardViewModel>()
)
```

**UI 组件**：

- **DashboardHeader**: 顶部头部（问候语、最近编辑数、同步状态、刷新按钮）
- **DashboardToolbar**: 工具栏（搜索栏、统计卡片、视图切换按钮）
- **StatsCardsRow**: 统计卡片行（移动端显示）
- **DashboardGridView**: 瀑布流网格视图（使用 LazyVerticalStaggeredGrid）
- **DashboardListView**: 列表视图（使用 LazyColumn，支持响应式布局）
    - **ListViewHeader**: 列表表头（桌面端显示列标题）
    - **ListViewItem**: 列表项（使用 Card 扩展方法显示信息）

## 📁 模块结构

```text
feature/dashboard/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   └── tech/zhifu/app/myhub/dashboard/
│   │   │       ├── DashboardScreen.kt          # UI 组件
│   │   │       ├── DashboardViewModel.kt        # ViewModel
│   │   │       ├── DashboardUiState.kt          # UI 状态
│   │   │       └── di/
│   │   │           └── DashboardModule.kt      # 依赖注入模块
│   │   └── composeResources/
│   │       └── values/
│   │           ├── strings.xml                 # 默认字符串资源
│   │           ├── values-zh-rCN/
│   │           │   └── strings.xml            # 简体中文
│   │           ├── values-zh-rTW/
│   │           │   └── strings.xml            # 繁体中文
│   │           └── values-ja/
│   │               └── strings.xml             # 日语
│   └── devMain/                                # 预览支持
│       └── kotlin/
│           └── tech/zhifu/app/myhub/dashboard/
│               └── DashboardScreen.dev.kt      # Preview 函数
├── build.gradle.kts                            # 构建配置
└── README.md                                   # 本文档
```

## 🧪 Preview 支持

Dashboard 模块提供了完整的 Preview 支持，位于 `devMain` source set 中。

### Preview 文件结构

```text
src/devMain/kotlin/tech/zhifu/app/myhub/dashboard/
└── DashboardScreen.dev.kt
```

### Preview 特性

Preview 文件包含以下预览场景：

#### DashboardScreen 完整预览

- **浅色主题（网格视图）** - 展示 Dashboard 在浅色主题下的网格布局
- **深色主题（列表视图）** - 展示 Dashboard 在深色主题下的列表布局
- **加载状态** - 展示刷新时的加载动画效果

#### 组件独立预览

- **DashboardHeader** - 头部组件预览
    - 浅色主题
    - 深色主题
    - 加载状态（带旋转动画）

- **DashboardToolbar** - 工具栏组件预览
    - 浅色主题
    - 深色主题

- **StatsCardsRow** - 统计卡片行预览

- **DashboardGridView** - 网格视图预览
    - 紧凑布局（1列）
    - 中等布局（2列）
    - 扩展布局（3列）

- **DashboardListView** - 列表视图预览
    - 紧凑布局（移动端）
    - 扩展布局（桌面端）

### 示例数据生成

Preview 文件提供了示例数据生成函数：

- `createSampleStatistics()` - 创建示例统计信息
- `createSampleCards()` - 创建包含多种卡片类型的示例数据（QUOTE、CODE、IDEA、ARTICLE、DICTIONARY、CHECKLIST）

### 使用 Preview

在 Android Studio 或 IntelliJ IDEA 中：

1. 打开 `DashboardScreen.dev.kt` 文件
2. 点击 Preview 函数左侧的预览图标
3. 查看 Dashboard 在不同主题、布局和状态下的外观

### Preview 环境初始化

Preview 文件会自动初始化必要的 Koin 依赖（如 `cardModule`），确保预览环境正常工作。

```kotlin
// Preview 文件会自动调用 initPreviewKoin() 初始化 Koin
private fun initPreviewKoin() {
    val koin = KoinPlatformTools.defaultContext().getOrNull()
    if (koin == null) {
        startKoin {
            modules(cardModule)
        }
    }
}
```

## 🔧 实现细节

### 响应式数据监听

Dashboard 使用 Flow 实现响应式数据更新，状态管理采用 sealed class 确保类型安全：

```kotlin
// 监听统计信息
statisticsRepository.observeStatistics()
    .catch { e ->
        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                statistics = Statistics(),
                recentCards = emptyList(),
                favoriteCards = emptyList(),
                lastSyncTime = currentState.lastSyncTime,
                error = e.message ?: "Failed to load statistics"
            )
            is DashboardUiState.Content -> currentState.copy(
                error = e.message ?: "Failed to load statistics"
            )
        }
    }
    .onEach { statistics ->
        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                statistics = statistics,
                recentCards = emptyList(),
                favoriteCards = emptyList(),
                lastSyncTime = statistics.lastSyncTime
            )
            is DashboardUiState.Content -> currentState.copy(
                statistics = statistics,
                lastSyncTime = statistics.lastSyncTime
            )
        }
    }
    .launchIn(coroutineScope)

// 监听所有卡片
cardRepository.observeAllCards()
    .onEach { cards ->
        // 获取最近编辑的卡片（按 updated_at 排序，取前 10 个）
        val recentCards = cards
            .sortedByDescending { it.updatedAt }
            .take(10)

        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                statistics = Statistics(),
                recentCards = recentCards,
                favoriteCards = emptyList(),
                lastSyncTime = currentState.lastSyncTime
            )
            is DashboardUiState.Content -> currentState.copy(
                recentCards = recentCards
            )
        }
    }
    .launchIn(coroutineScope)

// 监听收藏的卡片
cardRepository.observeFavoriteCards()
    .onEach { favoriteCards ->
        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                statistics = Statistics(),
                recentCards = emptyList(),
                favoriteCards = favoriteCards,
                lastSyncTime = currentState.lastSyncTime
            )
            is DashboardUiState.Content -> currentState.copy(
                favoriteCards = favoriteCards
            )
        }
    }
    .launchIn(coroutineScope)
```

### 响应式布局

Dashboard 支持响应式布局，根据窗口大小自动调整列数和布局：

```kotlin
@Composable
fun DashboardScreen(...) {
    val sizeClass = windowSizeClass()
    val columns = when {
        sizeClass.isCompact -> 1   // 移动端：1 列
        sizeClass.isMedium -> 2     // 平板：2 列
        sizeClass.isExpanded -> 3   // 桌面：3 列
    }

    // 根据状态类型显示不同的 UI
    when (val state = uiState) {
        is DashboardUiState.InitialLoading -> {
            // 显示初始加载指示器（无数据）
        }
        is DashboardUiState.Content -> {
            // 显示数据内容
            DashboardHeader(
                isLoading = state.isRefreshing  // 刷新时显示加载动画
            )
            
            // 根据视图类型显示不同的布局
            when (state.viewType) {
                ViewType.GRID -> DashboardGridView(...)
                ViewType.LIST -> DashboardListView(...)
            }
            
            // 显示错误信息（如果有且不在刷新中）
            if (state.error != null && !state.isRefreshing) {
                // 显示错误提示
            }
        }
    }
}
```

**响应式特性**：

- **Grid 视图**：根据窗口大小自动调整列数（1/2/3 列）
- **List 视图**：
    - 移动端：只显示图标、标题和内容预览
    - 桌面端：显示完整信息（图标、标题、内容、标签、时间、操作按钮）

### 数据同步策略

1. **初始化加载**：

    - 从服务器获取所有卡片
    - 刷新统计信息
    - 开始监听数据变化

2. **自动更新**：

    - 通过 Flow 监听本地数据库变化
    - 实时更新 UI 状态

3. **手动刷新**：

    - 用户点击刷新按钮
    - 设置 `isRefreshing = true`（数据保持可见，显示加载动画）
    - 重新从服务器获取数据
    - 更新统计信息
    - 数据返回后立即更新 UI
    - 确保动画至少持续 2 秒
    - 刷新完成后设置 `isRefreshing = false`

4. **同步状态**：
    - 显示最后同步时间
    - 格式化时间显示（刚刚、X 分钟前、X 小时前、X 天前）

5. **错误处理**：
    - 错误时设置 `error` 字段，但数据保持可见
    - 用户可以继续查看和操作已有数据
    - 提供重试机制清除错误状态

## 💡 使用示例

### 1. 在应用中使用 Dashboard

```kotlin
// 在导航配置中
@Composable
fun AppNavigation() {
    NavHost(...) {
        composable("dashboard") {
            DashboardScreen()
        }
    }
}
```

### 2. 依赖注入配置

```kotlin
// 在 Koin 模块中（已配置在 DashboardModule.kt）
fun dashboardModule() = module {
    // Dashboard ViewModel
    factoryOf(::DashboardViewModel)
}
```

### 3. 自定义 Dashboard

```kotlin
@Composable
fun CustomDashboardScreen() {
    val viewModel: DashboardViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // 自定义 UI 实现
    when (val state = uiState) {
        is DashboardUiState.InitialLoading -> {
            // 显示初始加载状态（无数据）
            CircularProgressIndicator()
        }
        is DashboardUiState.Content -> {
            Column {
                // 显示统计信息
                Text("Total Cards: ${state.statistics.totalCards}")

                // 刷新按钮（刷新时显示加载动画）
                Button(
                    onClick = { viewModel.refresh() },
                    enabled = !state.isRefreshing
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Refresh")
                    }
                }
                
                // 显示错误信息（如果有）
                if (state.error != null) {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

## 📊 功能特性

### 统计信息展示

- **总卡片数**：显示用户的所有卡片总数
- **最近编辑**：显示最近编辑的卡片数量
- **收藏数**：显示收藏的卡片数量
- **待复习卡片**：显示需要复习的卡片数量（未来功能）

### 数据概览

- **最近编辑的卡片**：按更新时间排序，显示最近 10 个编辑的卡片
- **收藏的卡片**：显示所有收藏的卡片
- **卡片类型概览**：显示不同类型卡片的数量分布（未来功能）

### 交互功能

- **搜索**：搜索卡片内容、标签或作者（实时搜索，支持多语言占位符）
- **刷新**：手动刷新数据（带动画效果，刷新时数据保持可见）
    - 刷新时设置 `isRefreshing = true`，显示加载动画
    - 数据返回后立即更新，但动画至少持续 2 秒
    - 刷新完成后设置 `isRefreshing = false`
- **视图切换**：列表视图/网格视图切换（✅ 已实现）
    - Grid 视图：瀑布流布局，适合浏览大量卡片
    - List 视图：列表布局，适合快速查看和操作
- **卡片操作**：编辑、收藏、点击查看（在列表视图中，桌面端显示操作按钮）
- **筛选**：按类型、标签筛选卡片（未来功能）

## 🌐 国际化支持

Dashboard 模块支持多语言：

- **英语** (en): 默认语言
- **简体中文** (zh-rCN)
- **繁体中文** (zh-rTW)
- **日语** (ja)

所有字符串资源都通过 `composeResources` 管理，支持编译时类型安全。

## 🎨 UI 设计

### 布局结构

#### Grid 视图布局

```text
┌─────────────────────────────────────────┐
│  Dashboard Header                       │
│  - 问候语                               │
│  - 最近编辑数                           │
│  - 最后同步时间                         │
│  - 刷新按钮                             │
├─────────────────────────────────────────┤
│  Dashboard Toolbar                      │
│  - 搜索栏                               │
│  - 统计卡片（桌面端）                   │
│  - 视图切换按钮                         │
├─────────────────────────────────────────┤
│  Stats Cards Row（移动端）              │
│  ┌─────┐ ┌─────┐ ┌─────┐               │
│  │Total│ │Edit │ │Fav  │               │
│  └─────┘ └─────┘ └─────┘               │
├─────────────────────────────────────────┤
│  Content Grid (Staggered)               │
│  ┌─────┐ ┌─────┐ ┌─────┐               │
│  │Card │ │Card │ │Card │               │
│  └─────┘ └─────┘ └─────┘               │
│  ┌─────┐ ┌─────┐ ┌─────┐               │
│  │Card │ │Card │ │Card │               │
│  └─────┘ └─────┘ └─────┘               │
└─────────────────────────────────────────┘
```

#### List 视图布局

```text
┌─────────────────────────────────────────┐
│  Dashboard Header                       │
│  - 问候语                               │
│  - 最近编辑数                           │
│  - 最后同步时间                         │
│  - 刷新按钮                             │
├─────────────────────────────────────────┤
│  Dashboard Toolbar                      │
│  - 搜索栏                               │
│  - 统计卡片（桌面端）                   │
│  - 视图切换按钮                         │
├─────────────────────────────────────────┤
│  List Header（桌面端）                   │
│  Name & Content | Tags | Time | Actions │
├─────────────────────────────────────────┤
│  List Items                             │
│  ┌───────────────────────────────────┐ │
│  │ [Icon] Title | Preview | Tags | ...│ │
│  └───────────────────────────────────┘ │
│  ┌───────────────────────────────────┐ │
│  │ [Icon] Title | Preview | Tags | ...│ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 响应式设计

#### Grid 视图

- **移动端** (Compact): 单列布局，统计卡片显示在工具栏下方
- **平板** (Medium): 双列布局，统计卡片显示在工具栏中
- **桌面** (Expanded): 三列布局，统计卡片显示在工具栏中

#### List 视图

- **移动端** (Compact):
    - 只显示图标、标题和内容预览
    - 隐藏标签、时间和操作按钮
    - 简化布局，优化移动端体验
- **桌面端** (Medium/Expanded):
    - 显示完整信息：图标、标题、内容预览、标签、时间、操作按钮
    - 显示列表表头（列标题）
    - 支持 hover 效果和快速操作

## 🔄 数据同步

### 同步流程

1. **应用启动**：

    - 自动从服务器获取最新数据
    - 开始监听本地数据库变化

2. **数据更新**：

    - 本地数据库变化 → Flow 触发 → UI 自动更新
    - 服务器数据变化 → 手动刷新 → 更新本地数据库 → Flow 触发 → UI 更新

3. **错误处理**：
    - 网络错误：显示错误信息，使用本地缓存数据
    - 数据错误：记录日志，使用默认值

## 🔧 Card 扩展方法使用

Dashboard 使用 Card 扩展方法来统一获取卡片显示信息：

```kotlin
@Composable
private fun ListViewItem(card: Card) {
    // @Composable 函数需要提前定义
    val cardTitle = card.getDisplayTitle()
    val formattedDate = card.formatUpdatedTime()

    // 多次使用的值提前定义
    val iconColor = card.typeIconColor

    // 只使用一次的值直接使用
    // card.getContentPreview()
    // card.typeIconText
}
```

**使用的扩展方法**：

- `Card.getDisplayTitle()`: 获取显示标题（支持多语言）
- `Card.getContentPreview()`: 获取内容预览
- `Card.typeIconColor`: 获取图标颜色
- `Card.typeIconText`: 获取图标文本
- `Card.formatUpdatedTime()`: 格式化更新时间（支持多语言）

## 🚀 未来计划

### 短期计划

- [ ] 实现搜索功能逻辑（当前仅 UI）
- [ ] 添加卡片类型统计图表
- [ ] 添加待复习卡片提醒
- [ ] 优化加载性能（分页加载）
- [ ] 添加下拉刷新功能

### 中期计划

- [ ] 添加自定义 Dashboard 布局
- [ ] 添加数据导出功能
- [ ] 添加数据可视化图表
- [ ] 添加快速操作（创建卡片、搜索等）

### 长期计划

- [ ] 添加 Dashboard 小部件（Widget）
- [ ] 添加数据分析和洞察
- [ ] 添加个性化推荐
- [ ] 添加多用户 Dashboard 切换

## 📝 当前状态

**已完成**：

- ✅ Dashboard 基础 UI 实现
- ✅ 统计信息展示
- ✅ 响应式数据监听
- ✅ 多平台支持（Android、iOS、Desktop、Web）
- ✅ 国际化支持
- ✅ 响应式布局
- ✅ 错误处理（错误时保留数据显示）
- ✅ 视图切换功能（Grid/List）
- ✅ 列表视图实现（ListViewItem、ListViewHeader）
- ✅ 搜索功能优化（多语言占位符、样式优化）
- ✅ Card 扩展方法集成（getDisplayTitle、getContentPreview、typeIconColor、typeIconText、formatUpdatedTime）
- ✅ WindowSizeClass 扩展方法（isCompact、isMedium、isExpanded）
- ✅ 状态管理优化（sealed class 设计，支持同时显示数据和加载状态）
- ✅ 刷新功能优化（刷新时数据保持可见，只显示加载动画）
- ✅ Preview 支持（devMain source set）

**待实现**：

- 🔄 卡片类型统计图表
- 🔄 待复习卡片提醒
- 🔄 下拉刷新功能
- 🔄 筛选功能
- 🔄 搜索功能实现（当前仅 UI，待接入搜索逻辑）

## 📚 参考

### 相关文档

- [项目整体架构](../../docs/myhub_architecture.md)
- [数据层架构](../../datastore/docs/datastore_architecture.md)
- [Settings 模块](../settings/README.md)
- [Card 组件](../../component/card/README.md)

### 设计原则

- **单一职责原则**：ViewModel 只负责状态管理和业务逻辑
- **响应式编程**：使用 Flow 实现数据驱动的 UI 更新
- **可测试性**：ViewModel 可独立测试，不依赖 UI
- **可扩展性**：易于添加新的统计指标和功能模块

## 🔗 相关模块

- **datastore/repository-client**: 提供卡片和统计数据的 Repository
- **datastore/model**: 提供数据模型（Card、Statistics）
- **component/card**: 提供卡片 UI 组件
- **core/platform**: 提供平台抽象（窗口大小、主题等）

## 📦 依赖配置

### Preview 支持

Dashboard 模块的 Preview 功能需要以下依赖：

```kotlin
dependencies {
    // Preview 支持
    implementation(compose.components.uiToolingPreview)
}

dependencies {
    "androidRuntimeClasspath"(compose.uiTooling)
}
```

这些依赖已在 `build.gradle.kts` 中配置，确保 Preview 功能正常工作。
