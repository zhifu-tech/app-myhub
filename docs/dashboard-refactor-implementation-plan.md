# Dashboard 重构实施计划

> **基于**: `dashboard-refactor-v1.0-analysis-review.md`  
> **创建时间**: 2026-01-28  
> **版本**: v1.0

---

## 一、实施决策总结

### ✅ 已确认的决策

1. **Collection 和 Card 关联**：使用长期方案，创建 `collection_card` 表
2. **Collection 预览**：每个 Collection 显示最近 3 条数据
3. **分页加载**：
   - Collections：10 条/页（一屏宽度）
   - Cards：20 条/页（瀑布流 Grid 模式）
4. **Review 逻辑**：展示所有未进行 review 的情况（不限制时间）
5. **Video 卡片**：支持 video 类型（链接+封面/播放按钮）
6. **下拉刷新**：使用 Material 3 的 PullRefresh 控件
7. **测试策略**：暂不考虑单测，后续统一添加

---

## 二、详细实施步骤

### 阶段 1：数据库层（SQLDelight）

#### 1.1 创建 `collection_card` 表

**文件**：`datastore/database/src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/collection_card.sq`

```sql
-- Collection 和 Card 的多对多关联表
CREATE TABLE collection_card (
  collection_id TEXT NOT NULL,
  card_id TEXT NOT NULL,
  created_at TEXT NOT NULL,
  PRIMARY KEY (collection_id, card_id),
  FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE,
  FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX idx_collection_card_collection_id ON collection_card(collection_id);
CREATE INDEX idx_collection_card_card_id ON collection_card(card_id);
CREATE INDEX idx_collection_card_created_at ON collection_card(collection_id, created_at);

-- CRUD 操作
selectCollectionCards:
SELECT card_id FROM collection_card
WHERE collection_id = ?
ORDER BY created_at DESC;

selectCollectionCardCount:
SELECT COUNT(*) as count
FROM collection_card
WHERE collection_id = ?;

selectCollectionCardCounts:
SELECT 
    collection_id,
    COUNT(*) as count
FROM collection_card
WHERE collection_id IN (?)
GROUP BY collection_id;

-- 获取每个 Collection 的最近 3 条卡片
selectCollectionPreviewCards:
SELECT c.*
FROM card c
INNER JOIN collection_card cc ON c.id = cc.card_id
WHERE cc.collection_id = ?
ORDER BY c.updated_at DESC
LIMIT 3;

insertCollectionCard:
INSERT OR REPLACE INTO collection_card (collection_id, card_id, created_at)
VALUES (?, ?, ?);

deleteCollectionCard:
DELETE FROM collection_card
WHERE collection_id = ? AND card_id = ?;

deleteCollectionCardsByCollectionId:
DELETE FROM collection_card
WHERE collection_id = ?;

deleteCollectionCardsByCardId:
DELETE FROM collection_card
WHERE card_id = ?;
```

#### 1.2 创建 `card_metadata_video` 表

**文件**：`datastore/database/src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/card_metadata_video.sq`

```sql
-- Video 卡片元数据表
CREATE TABLE card_metadata_video (
  card_id TEXT PRIMARY KEY NOT NULL,
  video_url TEXT NOT NULL,
  thumbnail_url TEXT,
  duration_seconds INTEGER,
  platform TEXT,
  FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);

-- CRUD 操作
selectVideoMetadataByCardId:
SELECT * FROM card_metadata_video
WHERE card_id = ?;

insertVideoMetadata:
INSERT OR REPLACE INTO card_metadata_video (
    card_id,
    video_url,
    thumbnail_url,
    duration_seconds,
    platform
)
VALUES (?, ?, ?, ?, ?);

updateVideoMetadata:
UPDATE card_metadata_video
SET video_url = ?,
    thumbnail_url = ?,
    duration_seconds = ?,
    platform = ?
WHERE card_id = ?;

deleteVideoMetadata:
DELETE FROM card_metadata_video
WHERE card_id = ?;
```

#### 1.3 优化 Review 查询

**文件**：`datastore/database/src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/user_card.sq`

**添加索引**：
```sql
-- 优化未复习卡片查询
CREATE INDEX idx_user_card_review_status ON user_card(user_id, last_reviewed_at);
```

**添加查询**：
```sql
-- 查询所有未复习的卡片
selectUnreviewedCards:
SELECT c.*
FROM card c
INNER JOIN user_card uc ON c.id = uc.card_id
WHERE uc.user_id = ?
  AND uc.last_reviewed_at IS NULL
ORDER BY c.created_at DESC;

-- 统计未复习卡片数量
selectUnreviewedCardCount:
SELECT COUNT(*) as total
FROM user_card
WHERE user_id = ?
  AND last_reviewed_at IS NULL;

-- 统计 ReviewProgress
selectReviewProgress:
SELECT 
    COUNT(CASE WHEN last_reviewed_at IS NOT NULL THEN 1 END) as completed,
    COUNT(CASE WHEN last_reviewed_at IS NULL THEN 1 END) as total
FROM user_card
WHERE user_id = ?;
```

#### 1.4 创建数据库迁移脚本

**文件**：`datastore/database/src/commonMain/sqldelight/tech/zhifu/app/myhub/datastore/database/migration_2.sq`

```sql
-- Migration 2: Add collection_card and card_metadata_video tables
-- 执行时间：2026-01-28

-- 创建 collection_card 表
CREATE TABLE IF NOT EXISTS collection_card (
  collection_id TEXT NOT NULL,
  card_id TEXT NOT NULL,
  created_at TEXT NOT NULL,
  PRIMARY KEY (collection_id, card_id),
  FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE,
  FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_collection_card_collection_id ON collection_card(collection_id);
CREATE INDEX IF NOT EXISTS idx_collection_card_card_id ON collection_card(card_id);
CREATE INDEX IF NOT EXISTS idx_collection_card_created_at ON collection_card(collection_id, created_at);

-- 创建 card_metadata_video 表
CREATE TABLE IF NOT EXISTS card_metadata_video (
  card_id TEXT PRIMARY KEY NOT NULL,
  video_url TEXT NOT NULL,
  thumbnail_url TEXT,
  duration_seconds INTEGER,
  platform TEXT,
  FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);

-- 添加 user_card 索引优化 Review 查询
CREATE INDEX IF NOT EXISTS idx_user_card_review_status ON user_card(user_id, last_reviewed_at);
```

**更新数据库版本**：
- 在 `Database.kt` 中更新版本号（如从 1 到 2）

---

### 阶段 2：数据模型层

#### 2.1 更新 Collection 模型

**文件**：`datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/domain/Collection.kt`

```kotlin
data class Collection(
    val id: String,
    val name: String,
    val topic: String? = null,
    val description: String? = null,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    // 新增字段
    val cardCount: Int = 0,                    // 卡片数量
    val previewCards: List<Card> = emptyList()  // 最近 3 条预览卡片
)
```

#### 2.2 创建 VideoMetadata

**文件**：`datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/domain/VideoMetadata.kt`

```kotlin
package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class VideoMetadata(
    val cardId: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int? = null,
    val platform: String? = null  // youtube, xiaohongshu, etc.
) : CardMetadata
```

#### 2.3 更新 DashboardUiState

**文件**：`feature/dashboard/src/commonMain/kotlin/tech/zhifu/app/myhub/feature/dashboard/DashboardUiState.kt`

```kotlin
data class Content(
    val statistics: Statistics = Statistics(),
    val recentCards: List<Card>,
    val favoriteCards: List<Card>,
    val lastSyncTime: Long?,
    val viewType: ViewType = ViewType.GRID,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val reviewProgress: ReviewProgress = ReviewProgress(),
    val showFocusReview: Boolean = true,
    
    // 新增字段
    val collections: List<Collection> = emptyList(),
    val collectionCardCounts: Map<String, Int> = emptyMap(),
    val reviewCardsCount: Int = 0,
    
    // 分页相关
    val hasMoreCards: Boolean = false,
    val isLoadingMoreCards: Boolean = false,
    val cardsPage: Int = 1,
    val cardsPageSize: Int = 20,
    
    val hasMoreCollections: Boolean = false,
    val isLoadingMoreCollections: Boolean = false,
    val collectionsPage: Int = 1,
    val collectionsPageSize: Int = 10
) : DashboardUiState()
```

---

### 阶段 3：Repository 层

#### 3.1 更新 CollectionRepository

**文件**：`datastore/repository-client-api/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/repository/collection/CollectionRepository.kt`

```kotlin
interface CollectionRepository {
    // ... 现有方法
    
    // 新增：分页查询 Collections
    fun streamCollectionsPaged(
        userId: String,
        page: Int,
        pageSize: Int,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<CollectionStoreData>>
    
    // 新增：获取 Collection 的预览卡片
    suspend fun getCollectionPreviewCards(collectionId: String): List<Card>
    
    // 新增：获取多个 Collection 的卡片数量
    suspend fun getCollectionCardCounts(collectionIds: List<String>): Map<String, Int>
}
```

#### 3.2 更新 CardRepository

**文件**：`datastore/repository-client-api/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/repository/card/CardRepository.kt`

```kotlin
interface CardRepository {
    // ... 现有方法
    
    // 新增：分页查询 Cards
    fun streamCardsPaged(
        userId: String,
        page: Int,
        pageSize: Int,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<CardStoreData>>
    
    // 新增：查询未复习的卡片
    fun streamUnreviewedCards(
        userId: String,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<CardStoreData>>
    
    // 新增：查询 ReviewProgress
    suspend fun getReviewProgress(userId: String): ReviewProgress
}
```

#### 3.3 更新 LocalDataSource

**文件**：`datastore/datasource-local/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/datasource/LocalCollectionDataSource.kt`

```kotlin
interface LocalCollectionDataSource {
    // ... 现有方法
    
    // 新增：分页查询 Collections
    suspend fun getCollectionsPaged(
        userId: String,
        page: Int,
        pageSize: Int
    ): List<Collection>
    
    // 新增：获取 Collection 的预览卡片
    suspend fun getCollectionPreviewCards(collectionId: String): List<Card>
    
    // 新增：获取多个 Collection 的卡片数量
    suspend fun getCollectionCardCounts(collectionIds: List<String>): Map<String, Int>
}
```

---

### 阶段 4：ViewModel 层

#### 4.1 更新 DashboardViewModel

**文件**：`feature/dashboard/src/commonMain/kotlin/tech/zhifu/app/myhub/feature/dashboard/DashboardViewModel.kt`

```kotlin
class DashboardViewModel(
    private val cardRepository: CardRepository,
    private val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository,
    private val coroutineScope: CoroutineScope
) {
    // ... 现有代码
    
    private fun loadDashboardData() {
        userRepository.streamUser()
            .distinctUntilChangedBy { it.id }
            .onEach { user ->
                loadCardsForUser(user.id)
                loadCollectionsForUser(user.id)
                loadReviewProgress(user.id)
            }
            .launchIn(coroutineScope)
    }
    
    private fun loadCollectionsForUser(userId: String) {
        collectionRepository.streamCollectionsPaged(
            userId = userId,
            page = 1,
            pageSize = 10,
            refresh = false
        )
        .onEach { response ->
            when (response) {
                is StoreReadResponse.Data -> {
                    val collections = response.value.collections
                    updateUiStateWithCollections(collections)
                }
                // ... 其他状态处理
            }
        }
        .launchIn(coroutineScope)
    }
    
    private fun loadCardsForUser(userId: String) {
        cardRepository.streamCardsPaged(
            userId = userId,
            page = 1,
            pageSize = 20,
            refresh = false
        )
        .onEach { response ->
            when (response) {
                is StoreReadResponse.Data -> {
                    val cards = response.value.cards
                    updateUiStateWithCards(cards)
                }
                // ... 其他状态处理
            }
        }
        .launchIn(coroutineScope)
    }
    
    private fun loadReviewProgress(userId: String) {
        coroutineScope.launch {
            val progress = cardRepository.getReviewProgress(userId)
            updateUiState { it.copy(reviewProgress = progress) }
        }
    }
    
    fun loadMoreCards() {
        val currentState = _uiState.value as? DashboardUiState.Content ?: return
        if (currentState.isLoadingMoreCards || !currentState.hasMoreCards) return
        
        _uiState.value = currentState.copy(isLoadingMoreCards = true)
        
        cardRepository.streamCardsPaged(
            userId = getCurrentUserId(),
            page = currentState.cardsPage + 1,
            pageSize = currentState.cardsPageSize,
            refresh = false
        )
        .onEach { response ->
            when (response) {
                is StoreReadResponse.Data -> {
                    val newCards = response.value.cards
                    val updatedCards = currentState.recentCards + newCards
                    _uiState.value = currentState.copy(
                        recentCards = updatedCards,
                        cardsPage = currentState.cardsPage + 1,
                        hasMoreCards = newCards.size >= currentState.cardsPageSize,
                        isLoadingMoreCards = false
                    )
                }
                // ... 错误处理
            }
        }
        .launchIn(coroutineScope)
    }
    
    fun loadMoreCollections() {
        // 类似 loadMoreCards 的实现
    }
    
    fun refresh() {
        coroutineScope.launch {
            val currentState = _uiState.value as? DashboardUiState.Content ?: return@launch
            _uiState.value = currentState.copy(isRefreshing = true)
            
            // 刷新所有数据
            val userId = getCurrentUserId()
            cardRepository.streamCardsPaged(userId, 1, 20, refresh = true)
            collectionRepository.streamCollectionsPaged(userId, 1, 10, refresh = true)
            loadReviewProgress(userId)
            
            // 刷新完成后，设置 isRefreshing = false
            _uiState.value = currentState.copy(isRefreshing = false)
        }
    }
}
```

---

### 阶段 5：UI 层

#### 5.1 实现下拉刷新

**文件**：`feature/dashboard/src/commonMain/kotlin/tech/zhifu/app/myhub/feature/dashboard/DashboardScreen.kt`

```kotlin
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is DashboardUiState.Content -> {
            val isRefreshing = state.isRefreshing
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isRefreshing,
                onRefresh = { viewModel.refresh() }
            )
            
            Box(modifier = modifier.pullRefresh(pullRefreshState)) {
                LazyColumn {
                    // Dashboard 内容
                    item {
                        TopAppBar(...)
                    }
                    item {
                        FocusReviewModule(...)
                    }
                    item {
                        AssetCollectionsModule(
                            collections = state.collections,
                            onLoadMore = { viewModel.loadMoreCollections() },
                            isLoadingMore = state.isLoadingMoreCollections
                        )
                    }
                    item {
                        LatestCapturesModule(
                            cards = state.recentCards,
                            onLoadMore = { viewModel.loadMoreCards() },
                            isLoadingMore = state.isLoadingMoreCards,
                            hasMore = state.hasMoreCards
                        )
                    }
                }
                
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
        // ... 其他状态
    }
}
```

#### 5.2 实现分页加载

**文件**：`feature/dashboard/src/commonMain/kotlin/tech/zhifu/app/myhub/feature/dashboard/DashboardScreen.kt`

```kotlin
@Composable
fun LatestCapturesModule(
    cards: List<Card>,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(300.dp),
        modifier = modifier
    ) {
        items(cards) { card ->
            CardComponent(card = card)
        }
        
        // 加载更多指示器
        if (hasMore) {
            item {
                if (isLoadingMore) {
                    CircularProgressIndicator()
                } else {
                    // 滚动到底部时自动加载
                    LaunchedEffect(Unit) {
                        onLoadMore()
                    }
                }
            }
        }
    }
}
```

#### 5.3 实现 VideoCardComponent

**文件**：`component/card/src/commonMain/kotlin/tech/zhifu/app/myhub/component/card/VideoCardComponent.kt`

```kotlin
class VideoCardComponent : CardComponent {
    @Composable
    override fun CardContent(
        card: Card,
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null
    ) {
        val metadata = card.metadata as? VideoMetadata
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick?.invoke() },
            onClick = { /* 打开视频链接 */ }
        ) {
            Column {
                // 封面图或播放按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    if (metadata?.thumbnailUrl != null) {
                        AsyncImage(
                            model = metadata.thumbnailUrl,
                            contentDescription = "Video thumbnail"
                        )
                    }
                    
                    // 播放按钮
                    IconButton(
                        onClick = { /* 打开视频链接 */ },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play video"
                        )
                    }
                }
                
                // 标题和时长
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = card.title ?: "Video")
                    if (metadata?.durationSeconds != null) {
                        Text(
                            text = formatDuration(metadata.durationSeconds),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
```

---

### 阶段 6：Bootstrap 数据

#### 6.1 添加 collection_card 关联数据

**文件**：`datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/collection_card.json`

```json
[
  {
    "collectionId": "collection-001",
    "cardId": "card-001"
  },
  {
    "collectionId": "collection-001",
    "cardId": "card-002"
  }
  // ... 更多关联
]
```

#### 6.2 添加 video 卡片数据

**文件**：`datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/card.json`

```json
[
  {
    "id": "card-video-001",
    "type": "video",
    "title": "The Future of Generative AI",
    "content": "A comprehensive overview of generative AI technologies",
    "userId": "user-001",
    "createdAt": "2023-10-24T10:00:00Z",
    "updatedAt": "2023-10-24T10:00:00Z"
  }
]
```

**文件**：`datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/card_metadata_video.json`

```json
[
  {
    "cardId": "card-video-001",
    "videoUrl": "https://www.youtube.com/watch?v=...",
    "thumbnailUrl": "https://img.youtube.com/vi/.../maxresdefault.jpg",
    "durationSeconds": 765,
    "platform": "youtube"
  }
]
```

---

## 三、实施检查清单

### 数据库层
- [ ] 创建 `collection_card.sq` 文件
- [ ] 创建 `card_metadata_video.sq` 文件
- [ ] 添加 `user_card` 索引优化 Review 查询

### 数据模型层
- [ ] 更新 `Collection` 模型，添加 `previewCards` 字段
- [ ] 创建 `VideoMetadata` 数据类
- [ ] 更新 `DashboardUiState.Content`，添加分页字段

### Repository 层
- [ ] 更新 `CollectionRepository` 接口，添加分页方法
- [ ] 更新 `CardRepository` 接口，添加分页和 Review 方法
- [ ] 实现 LocalDataSource 的新方法
- [ ] 更新 Store5 的 SourceOfTruth

### ViewModel 层
- [ ] 实现分页加载逻辑
- [ ] 实现下拉刷新逻辑
- [ ] 使用响应式流加载 Collections
- [ ] 实现 ReviewProgress 加载

### UI 层
- [ ] 实现 Material 3 PullRefresh
- [ ] 实现分页加载（滚动到底部）
- [ ] 实现 `AssetCollectionsModule` 组件
- [ ] 实现 `CollectionCard` 组件（显示最近 3 条预览）
- [ ] 实现 `VideoCardComponent` 组件
- [ ] 更新 Top App Bar

### Bootstrap 数据
- [ ] 添加 `collection_card` 关联数据
- [ ] 添加 video 卡片数据
- [ ] 添加 `card_metadata_video` 数据

---

## 四、注意事项

1. **数据库迁移**：
   - 确保迁移脚本正确执行
   - 测试迁移前后的数据完整性

2. **性能优化**：
   - 使用索引加速查询
   - 分页加载避免一次性加载大量数据
   - 缓存统计结果

3. **响应式数据流**：
   - 使用 Store5 的响应式流
   - 避免在 ViewModel 中做同步查询
   - 使用 Flow 组合多个数据源

4. **错误处理**：
   - 处理网络错误
   - 处理数据库错误
   - 显示友好的错误提示

---

**文档结束**
