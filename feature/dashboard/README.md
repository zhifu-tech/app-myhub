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
 * Dashboard UI状态
 */
data class DashboardUiState(
    val statistics: Statistics = Statistics(),
    val recentCards: List<Card> = emptyList(),
    val favoriteCards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastSyncTime: Long? = null
)
```

**状态说明**：

- `statistics`: 统计信息（总卡片数、最近编辑数、收藏数等）
- `recentCards`: 最近编辑的卡片列表（最多 10 个）
- `favoriteCards`: 收藏的卡片列表
- `isLoading`: 加载状态
- `error`: 错误信息
- `lastSyncTime`: 最后同步时间

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
- **DashboardToolbar**: 工具栏（搜索栏、统计卡片、视图切换）
- **StatsCardsRow**: 统计卡片行（移动端显示）
- **LazyVerticalGrid**: 内容卡片网格（响应式布局）

## 📁 模块结构

```text
feature/dashboard/
├── src/
│   └── commonMain/
│       ├── kotlin/
│       │   └── tech/zhifu/app/myhub/dashboard/
│       │       ├── DashboardScreen.kt          # UI 组件
│       │       ├── DashboardViewModel.kt        # ViewModel
│       │       ├── DashboardUiState.kt          # UI 状态
│       │       └── di/
│       │           └── DashboardModule.kt      # 依赖注入模块
│       └── composeResources/
│           └── values/
│               ├── strings.xml                 # 默认字符串资源
│               ├── values-zh-rCN/
│               │   └── strings.xml            # 简体中文
│               ├── values-zh-rTW/
│               │   └── strings.xml            # 繁体中文
│               └── values-ja/
│                   └── strings.xml             # 日语
├── build.gradle.kts                            # 构建配置
└── README.md                                   # 本文档
```

## 🔧 实现细节

### 响应式数据监听

Dashboard 使用 Flow 实现响应式数据更新：

```kotlin
// 监听统计信息
statisticsRepository.observeStatistics()
    .catch { e ->
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = e.message ?: "Failed to load statistics"
        )
    }
    .onEach { statistics ->
        _uiState.value = _uiState.value.copy(
            statistics = statistics,
            lastSyncTime = statistics.lastSyncTime
        )
    }
    .launchIn(coroutineScope)

// 监听所有卡片
cardRepository.observeAllCards()
    .onEach { cards ->
        // 获取最近编辑的卡片（按 updated_at 排序，取前 10 个）
        val recentCards = cards
            .sortedByDescending { it.updatedAt }
            .take(10)

        _uiState.value = _uiState.value.copy(
            recentCards = recentCards,
            isLoading = false
        )
    }
    .launchIn(coroutineScope)

// 监听收藏的卡片
cardRepository.observeFavoriteCards()
    .onEach { favoriteCards ->
        _uiState.value = _uiState.value.copy(
            favoriteCards = favoriteCards
        )
    }
    .launchIn(coroutineScope)
```

### 响应式布局

Dashboard 支持响应式布局，根据窗口大小自动调整列数：

```kotlin
@Composable
fun DashboardScreen(...) {
    val sizeClass = windowSizeClass()
    val columns = when (sizeClass) {
        WindowSizeClass.Compact -> 1   // 移动端：1 列
        WindowSizeClass.Medium -> 2    // 平板：2 列
        WindowSizeClass.Expanded -> 3  // 桌面：3 列
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        ...
    )
}
```

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
    - 重新从服务器获取数据
    - 更新统计信息

4. **同步状态**：
    - 显示最后同步时间
    - 格式化时间显示（刚刚、X 分钟前、X 小时前、X 天前）

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
    Column {
        // 显示统计信息
        Text("Total Cards: ${uiState.statistics.totalCards}")
        
        // 刷新按钮
        Button(onClick = { viewModel.refresh() }) {
            Text("Refresh")
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

- **搜索**：搜索卡片内容、标签或作者
- **刷新**：手动刷新数据
- **视图切换**：列表视图/网格视图（未来功能）
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

```
┌─────────────────────────────────────────┐
│  Dashboard Header                       │
│  - 问候语                               │
│  - 最近编辑数                           │
│  - 最后同步时间                         │
│  - 刷新按钮                             │
├─────────────────────────────────────────┤
│  Dashboard Toolbar                      │
│  - 搜索栏                               │
│  - 统计卡片（移动端）                   │
│  - 视图切换按钮（未来）                 │
├─────────────────────────────────────────┤
│  Content Grid                           │
│  ┌─────┐ ┌─────┐ ┌─────┐               │
│  │Card │ │Card │ │Card │               │
│  └─────┘ └─────┘ └─────┘               │
│  ┌─────┐ ┌─────┐ ┌─────┐               │
│  │Card │ │Card │ │Card │               │
│  └─────┘ └─────┘ └─────┘               │
└─────────────────────────────────────────┘
```

### 响应式设计

- **移动端** (Compact): 单列布局，统计卡片显示在工具栏下方
- **平板** (Medium): 双列布局，统计卡片显示在工具栏中
- **桌面** (Expanded): 三列布局，统计卡片显示在工具栏中

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

## 🚀 未来计划

### 短期计划

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
- ✅ 错误处理

**待实现**：

- 🔄 卡片类型统计图表
- 🔄 待复习卡片提醒
- 🔄 下拉刷新功能
- 🔄 视图切换功能
- 🔄 筛选功能

## 📚 参考

### 相关文档

- [项目整体架构](../../docs/myhub_architecture.md)
- [数据层架构](../../core/datastore/docs/datastore_architecture.md)
- [Settings 模块](../settings/README.md)
- [Card 组件](../../component/card/README.md)

### 设计原则

- **单一职责原则**：ViewModel 只负责状态管理和业务逻辑
- **响应式编程**：使用 Flow 实现数据驱动的 UI 更新
- **可测试性**：ViewModel 可独立测试，不依赖 UI
- **可扩展性**：易于添加新的统计指标和功能模块

## 🔗 相关模块

- **core/datastore-repository-client**: 提供卡片和统计数据的 Repository
- **core/datastore-model**: 提供数据模型（Card、Statistics）
- **component/card**: 提供卡片 UI 组件
- **core/platform**: 提供平台抽象（窗口大小、主题等）

