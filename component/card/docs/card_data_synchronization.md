# 卡片数据同步机制设计

## 📋 问题

当卡片的内容发生变化时，如何通知其他显示该卡片的地方（如 Dashboard、详情页、收藏页等）自动更新？

## 🎯 解决方案概述

基于项目现有的 **Flow 响应式架构**，通过 **Repository 作为单一数据源**，实现卡片数据的自动同步更新。

## 🏗️ 数据同步流程

### 核心机制

```
用户操作（编辑卡片）
    ↓
ViewModel.updateCard(card)
    ↓
Repository.updateCard(card)
    ↓
LocalDataSource.updateCard() → 更新数据库
    ↓
数据库变化触发 Flow 发出新值
    ↓
observeAllCards() 自动发出新的 List<Card>
    ↓
所有监听的地方自动更新
    ↓
UI 自动重组（collectAsState）
```

### 关键组件

1. **Repository 作为单一数据源**
   - 所有更新操作通过 Repository
   - 数据库变化自动触发 Flow 更新

2. **Flow 自动通知所有监听者**
   - `observeAllCards()` → 所有页面
   - `observeCard(id)` → 详情页（建议添加）
   - `observeFavoriteCards()` → 收藏页

3. **Compose 自动重组**
   - `collectAsState()` 监听 Flow
   - 数据变化自动触发 UI 更新

## 📐 现有机制

### Repository 接口

```kotlin
interface ReactiveCardRepository : CardRepository {
    /**
     * 观察所有卡片（响应式）
     */
    fun observeAllCards(): Flow<List<Card>>

    /**
     * 观察收藏的卡片（响应式）
     */
    fun observeFavoriteCards(): Flow<List<Card>>

    /**
     * 根据类型观察卡片（响应式）
     */
    fun observeCardsByType(type: CardType): Flow<List<Card>>

    /**
     * 根据标签观察卡片（响应式）
     */
    fun observeCardsByTag(tag: String): Flow<List<Card>>
}
```

### ViewModel 监听示例

```kotlin
class DashboardViewModel(
    private val cardRepository: ReactiveCardRepository
) {
    init {
        // 监听所有卡片，用于获取最近编辑的卡片
        cardRepository.observeAllCards()
            .onEach { cards ->
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
    }
}
```

## 🔄 多页面同步方案

### 方案 1：通过 observeAllCards() 过滤（当前可用）

**Dashboard 页面**：

```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 监听所有卡片，自动更新
    LazyVerticalGrid(...) {
        items(uiState.recentCards) { card ->
            QuoteCard(
                card = card,
                onEdit = { card ->
                    viewModel.updateCard(card)
                },
                onFavorite = { card ->
                    viewModel.toggleFavorite(card.id)
                }
            )
        }
    }
}
```

**详情页**：

```kotlin
@Composable
fun CardDetailScreen(cardId: String) {
    val cardRepository: ReactiveCardRepository = koinInject()
    
    // 从所有卡片中过滤出当前卡片
    val card by cardRepository.observeAllCards()
        .map { cards -> cards.find { it.id == cardId } }
        .collectAsState(initial = null)
    
    card?.let { card ->
        QuoteCard(
            card = card,
            onEdit = { updatedCard ->
                // 更新卡片
                cardRepository.updateCard(updatedCard)
                // Dashboard 页面自动更新（通过 observeAllCards）
            }
        )
    }
}
```

**优点**：
- ✅ 无需修改 Repository
- ✅ 自动同步更新
- ✅ 实现简单

**缺点**：
- ⚠️ 详情页需要监听所有卡片，效率较低
- ⚠️ 如果卡片数量很大，会有性能问题

### 方案 2：添加 observeCard(id) 方法（推荐）⭐

#### 接口扩展

在 `ReactiveCardRepository` 中添加单个卡片观察方法：

```kotlin
interface ReactiveCardRepository : CardRepository {
    // ... 现有方法
    
    /**
     * 观察单个卡片（响应式）
     * 用于详情页等需要实时更新的场景
     */
    fun observeCard(id: String): Flow<Card?>
}
```

#### 实现

```kotlin
// CardRepositoryImpl.kt
@OptIn(ExperimentalCoroutinesApi::class)
override fun observeCard(id: String): Flow<Card?> {
    return userDataSource.observeUser().flatMapLatest { user ->
        if (user == null) {
            flowOf(null)
        } else {
            // 从 observeAllCards 中过滤，或直接从数据库观察
            localDataSource.observeCards(user.id)
                .map { cards -> cards.find { it.id == id } }
        }
    }
}
```

#### 使用示例

```kotlin
// 详情页
@Composable
fun CardDetailScreen(cardId: String) {
    val cardRepository: ReactiveCardRepository = koinInject()
    
    // 直接观察单个卡片，效率更高
    val card by cardRepository.observeCard(cardId)
        .collectAsState(initial = null)
    
    card?.let { card ->
        QuoteCard(
            card = card,
            onEdit = { updatedCard ->
                // 更新卡片
                cardRepository.updateCard(updatedCard)
                // Dashboard 页面自动更新（通过 observeAllCards）
                // 详情页自动更新（通过 observeCard）
            }
        )
    }
}
```

**优点**：
- ✅ 效率更高，只监听单个卡片
- ✅ 自动同步更新
- ✅ 适合详情页场景

**缺点**：
- ⚠️ 需要修改 Repository 接口和实现

## 💡 完整同步场景示例

### 场景 1：Dashboard 编辑卡片 → 详情页自动更新

```kotlin
// Dashboard 页面
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyVerticalGrid(...) {
        items(uiState.recentCards) { card ->
            QuoteCard(
                card = card,
                onEdit = { card ->
                    // 编辑卡片
                    viewModel.updateCard(card)
                    // 导航到详情页
                    navigateTo(Screen.CardDetail(card.id))
                }
            )
        }
    }
}

// 详情页
@Composable
fun CardDetailScreen(cardId: String) {
    val cardRepository: ReactiveCardRepository = koinInject()
    
    // 自动监听卡片变化
    val card by cardRepository.observeCard(cardId)
        .collectAsState(initial = null)
    
    card?.let { card ->
        QuoteCard(
            card = card,
            onEdit = { updatedCard ->
                // 更新卡片
                cardRepository.updateCard(updatedCard)
                // Dashboard 页面自动更新（通过 observeAllCards）
            }
        )
    }
}
```

**数据流**：
1. Dashboard 编辑卡片 → `updateCard()`
2. Repository 更新数据库
3. `observeAllCards()` 发出新值 → Dashboard 更新
4. `observeCard(id)` 发出新值 → 详情页更新

### 场景 2：详情页收藏卡片 → Dashboard 自动更新

```kotlin
// 详情页
QuoteCard(
    card = card,
    onFavorite = { card ->
        cardRepository.toggleFavorite(card.id)
        // Dashboard 的 observeFavoriteCards() 自动更新
    }
)

// Dashboard 自动更新
cardRepository.observeFavoriteCards()
    .onEach { favoriteCards ->
        _uiState.value = _uiState.value.copy(favoriteCards = favoriteCards)
    }
    .launchIn(coroutineScope)
```

**数据流**：
1. 详情页收藏卡片 → `toggleFavorite()`
2. Repository 更新数据库
3. `observeFavoriteCards()` 发出新值 → Dashboard 收藏列表更新
4. `observeAllCards()` 发出新值 → Dashboard 卡片列表更新

### 场景 3：多页面同时显示同一卡片

```kotlin
// Dashboard 显示卡片列表
val cards by cardRepository.observeAllCards()
    .collectAsState(initial = emptyList())

// 收藏页显示收藏的卡片
val favoriteCards by cardRepository.observeFavoriteCards()
    .collectAsState(initial = emptyList())

// 详情页显示单个卡片
val card by cardRepository.observeCard(cardId)
    .collectAsState(initial = null)

// 当卡片更新时：
// 1. observeAllCards() 更新 → Dashboard 更新
// 2. observeFavoriteCards() 更新 → 收藏页更新（如果卡片被收藏）
// 3. observeCard(id) 更新 → 详情页更新
```

## 🔧 数据同步的关键点

### 1. Repository 作为单一数据源

```kotlin
class CardRepositoryImpl : ReactiveCardRepository {
    override suspend fun updateCard(card: Card): Card {
        val userId = requireUserId()
        
        // 更新本地数据库（会自动触发 Flow）
        localDataSource.updateCard(card, userId)
        
        // 同步到远程
        val remoteCard = remoteDataSource.updateCard(...)
        localDataSource.updateCard(remoteCard, userId)
        
        return remoteCard
    }
}
```

**关键**：
- 所有更新操作通过 Repository
- 数据库变化自动触发 Flow
- 确保数据一致性

### 2. Flow 自动通知所有监听者

```kotlin
// LocalDataSource 实现
override fun observeCards(userId: String): Flow<List<Card>> {
    return database.cardQueries.selectAll(userId)
        .asFlow()
        .mapLatest { query ->
            query.awaitAsList().map { row -> row.toCard(userId) }
        }
}
```

**关键**：
- 数据库查询使用 `asFlow()` 转换为 Flow
- 数据库变化自动触发 Flow 发出新值
- 所有监听者自动接收更新

### 3. Compose 自动重组

```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinInject()) {
    // collectAsState() 监听 Flow，自动触发重组
    val uiState by viewModel.uiState.collectAsState()
    
    // UI 自动更新
    LazyVerticalGrid(...) {
        items(uiState.recentCards) { card ->
            QuoteCard(card = card)
        }
    }
}
```

**关键**：
- `collectAsState()` 在 Composable 中监听 Flow
- Flow 发出新值时自动触发重组
- UI 自动反映最新数据

## 🚀 建议的改进

### 1. 添加 observeCard(id) 方法

**接口扩展**：

```kotlin
interface ReactiveCardRepository : CardRepository {
    // ... 现有方法
    
    /**
     * 观察单个卡片（响应式）
     * 用于详情页等需要实时更新的场景
     */
    fun observeCard(id: String): Flow<Card?>
}
```

**实现**：

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
override fun observeCard(id: String): Flow<Card?> {
    return userDataSource.observeUser().flatMapLatest { user ->
        if (user == null) {
            flowOf(null)
        } else {
            localDataSource.observeCards(user.id)
                .map { cards -> cards.find { it.id == id } }
        }
    }
}
```

**优势**：
- 提高详情页效率
- 减少不必要的过滤操作
- 更好的性能表现

### 2. 优化数据更新流程

```kotlin
override suspend fun updateCard(card: Card): Card {
    val userId = requireUserId()
    
    // 更新本地数据库（会自动触发 Flow）
    val updatedCard = card.copy(updatedAt = Clock.System.now())
    localDataSource.updateCard(updatedCard, userId)
    
    // 同步到远程（异步，不阻塞）
    coroutineScope.launch {
        try {
            val remoteCard = remoteDataSource.updateCard(...)
            localDataSource.updateCard(remoteCard, userId)
        } catch (e: Exception) {
            // 错误处理
        }
    }
    
    return updatedCard
}
```

**关键**：
- 先更新本地，立即触发 Flow
- 远程同步异步进行
- 确保 UI 快速响应

### 3. 考虑添加批量更新优化

```kotlin
/**
 * 批量更新卡片
 * 用于同时更新多个卡片的场景
 */
suspend fun updateCards(cards: List<Card>): List<Card> {
    val userId = requireUserId()
    
    // 批量更新本地数据库
    cards.forEach { card ->
        localDataSource.updateCard(card, userId)
    }
    
    // 批量同步到远程
    // ...
    
    return cards
}
```

## 📊 方案对比

| 特性 | 方案 1：observeAllCards() 过滤 | 方案 2：observeCard(id) |
|------|------------------------------|------------------------|
| 实现复杂度 | ✅ 简单 | ⚠️ 需要修改 Repository |
| 性能 | ⚠️ 需要监听所有卡片 | ✅ 只监听单个卡片 |
| 适用场景 | 卡片列表页面 | 详情页 |
| 自动同步 | ✅ 支持 | ✅ 支持 |
| 推荐度 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

## ✅ 最佳实践

### 1. 页面级别使用 observeAllCards()

```kotlin
// Dashboard、收藏页等列表页面
val cards by cardRepository.observeAllCards()
    .collectAsState(initial = emptyList())
```

### 2. 详情页使用 observeCard(id)

```kotlin
// 详情页
val card by cardRepository.observeCard(cardId)
    .collectAsState(initial = null)
```

### 3. 确保更新操作通过 Repository

```kotlin
// ✅ 正确：通过 Repository 更新
cardRepository.updateCard(card)

// ❌ 错误：直接操作数据库
localDataSource.updateCard(card, userId)
```

### 4. 使用 Flow 实现响应式更新

```kotlin
// ✅ 正确：使用 Flow 监听
cardRepository.observeAllCards()
    .onEach { cards -> /* 更新 UI */ }
    .launchIn(coroutineScope)

// ❌ 错误：轮询检查
while (true) {
    val cards = cardRepository.getAllCards()
    // 更新 UI
    delay(1000)
}
```

## 🎯 总结

### 核心机制

1. **Repository 作为单一数据源**
   - 所有更新操作通过 Repository
   - 确保数据一致性

2. **Flow 自动通知所有监听者**
   - 数据库变化自动触发 Flow
   - 所有监听者自动接收更新

3. **Compose 自动重组**
   - `collectAsState()` 监听 Flow
   - 数据变化自动触发 UI 更新

### 优势

- ✅ **自动同步**：一处更新，多处自动更新
- ✅ **解耦设计**：页面间不直接依赖
- ✅ **高效更新**：基于 Flow 的响应式更新
- ✅ **可扩展性**：易于添加新的观察点

### 建议

1. 添加 `observeCard(id)` 方法，提高详情页效率
2. 优化数据更新流程，确保快速响应
3. 考虑批量更新优化，提升性能

---

**文档版本：** v1.0  
**创建日期：** 2024年  
**最后更新：** 2024年


