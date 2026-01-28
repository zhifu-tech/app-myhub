# Dashboard 重构任务清单

> **创建时间**: 2026-01-28  
> **状态**: 进行中

---

## 任务概览

### 阶段 1：数据库层（SQLDelight）✅ 进行中

- [ ] **任务 1.1**: 创建 `collection_card.sq` 文件
  - 创建表结构
  - 添加索引
  - 添加 CRUD 查询（select, insert, delete）
  - 添加预览卡片查询（最近 3 条）

- [ ] **任务 1.2**: 创建 `card_metadata_video.sq` 文件
  - 创建表结构
  - 添加 CRUD 查询

- [ ] **任务 1.3**: 更新 `user_card.sq` 文件
  - 添加索引 `idx_user_card_review_status`
  - 添加查询 `selectUnreviewedCards`
  - 添加查询 `selectUnreviewedCardCount`
  - 添加查询 `selectReviewProgress`

### 阶段 2：数据模型层

- [ ] **任务 2.1**: 更新 `Collection` 模型
  - 添加 `cardCount: Int` 字段
  - 添加 `previewCards: List<Card>` 字段

- [ ] **任务 2.2**: 创建 `VideoMetadata` 数据类
  - 创建文件 `datastore/model/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/model/domain/VideoMetadata.kt`
  - 实现 `CardMetadata` 接口

- [ ] **任务 2.3**: 更新 `DashboardUiState`
  - 添加 `collections: List<Collection>` 字段
  - 添加 `collectionCardCounts: Map<String, Int>` 字段
  - 添加 `reviewCardsCount: Int` 字段
  - 添加分页相关字段（hasMoreCards, isLoadingMoreCards, cardsPage, cardsPageSize）
  - 添加 Collections 分页字段（hasMoreCollections, isLoadingMoreCollections, collectionsPage, collectionsPageSize）

### 阶段 3：Repository 层

- [ ] **任务 3.1**: 更新 `LocalCollectionDataSource` 接口
  - 添加 `getCollectionsPaged(userId, page, pageSize)` 方法
  - 添加 `getCollectionPreviewCards(collectionId)` 方法
  - 添加 `getCollectionCardCounts(collectionIds)` 方法

- [ ] **任务 3.2**: 实现 `LocalCollectionDataSourceImpl`
  - 实现分页查询
  - 实现预览卡片查询
  - 实现卡片数量查询

- [ ] **任务 3.3**: 更新 `LocalCardDataSource` 接口
  - 添加 `getCardsPaged(userId, page, pageSize)` 方法
  - 添加 `getUnreviewedCards(userId)` 方法
  - 添加 `getReviewProgress(userId)` 方法

- [ ] **任务 3.4**: 实现 `LocalCardDataSourceImpl` 新方法
  - 实现分页查询
  - 实现未复习卡片查询
  - 实现 ReviewProgress 查询

- [ ] **任务 3.5**: 更新 `CollectionRepository` 接口（客户端）
  - 添加分页查询方法
  - 添加预览卡片方法

- [ ] **任务 3.6**: 更新 `CardRepository` 接口（客户端）
  - 添加分页查询方法
  - 添加未复习卡片查询方法
  - 添加 ReviewProgress 查询方法

### 阶段 4：ViewModel 层

- [ ] **任务 4.1**: 更新 `DashboardViewModel`
  - 添加 `collectionRepository` 依赖
  - 实现 `loadCollectionsForUser()` 方法
  - 实现 `loadReviewProgress()` 方法
  - 实现 `loadMoreCards()` 方法
  - 实现 `loadMoreCollections()` 方法
  - 更新 `refresh()` 方法支持下拉刷新

### 阶段 5：UI 层

- [ ] **任务 5.1**: 实现下拉刷新
  - 添加 Material 3 PullRefresh 依赖
  - 在 `DashboardScreen` 中实现下拉刷新

- [ ] **任务 5.2**: 实现分页加载
  - 在 `LatestCapturesModule` 中实现滚动到底部加载更多
  - 在 `AssetCollectionsModule` 中实现滚动到底部加载更多

- [ ] **任务 5.3**: 实现 `AssetCollectionsModule` 组件
  - 创建组件文件
  - 实现 Collection 列表展示
  - 实现每个 Collection 显示最近 3 条预览

- [ ] **任务 5.4**: 实现 `CollectionCard` 组件
  - 创建组件文件
  - 实现 Collection 卡片 UI
  - 显示预览卡片（2x2 网格或列表）

- [ ] **任务 5.5**: 实现 `VideoCardComponent`
  - 创建组件文件
  - 实现视频卡片 UI（封面+播放按钮）
  - 实现点击跳转到视频链接

- [ ] **任务 5.6**: 更新 Top App Bar
  - 添加 "Focus & Review →" 链接

- [ ] **任务 5.7**: 添加 "New Capture" 占位符卡片

### 阶段 6：Bootstrap 数据

- [ ] **任务 6.1**: 创建 `collection_card.json` Bootstrap 数据文件
- [ ] **任务 6.2**: 添加 video 卡片数据到 `card.json`
- [ ] **任务 6.3**: 创建 `card_metadata_video.json` Bootstrap 数据文件

---

## 当前任务状态

**当前阶段**: 阶段 5 - UI 层 ✅ 已完成

**已完成**:
- ✅ 阶段 1：数据库层（SQLDelight）
  - ✅ 任务 1.1: 创建 `collection_card.sq` 文件
  - ✅ 任务 1.2: 创建 `card_metadata_video.sq` 文件
  - ✅ 任务 1.3: 更新 `user_card.sq` 文件
  - ✅ 更新 `card_with_metadata.sq` 视图，添加 video metadata
- ✅ 阶段 2：数据模型层
  - ✅ 任务 2.1: 更新 `Collection` 模型
  - ✅ 任务 2.2: 创建 `CardMetadataVideo` 数据类
  - ✅ 任务 2.3: 更新 `DashboardUiState`
- ✅ 阶段 3：Repository 层
  - ✅ 任务 3.1: 更新 `LocalCollectionDataSource` 接口
  - ✅ 任务 3.2: 实现 `LocalCollectionDataSourceImpl` 新方法
  - ✅ 任务 3.3: 更新 `LocalCardDataSource` 接口
  - ✅ 任务 3.4: 实现 `LocalCardDataSourceImpl` 新方法
  - ✅ 更新 `toDomain` 扩展函数，支持 video metadata
  - ✅ 更新 `insertCard` 方法，支持 video metadata 插入
- ✅ 阶段 4：ViewModel 层
  - ✅ 任务 4.1: 更新 `DashboardViewModel`，添加依赖
  - ✅ 实现 `loadCollectionsForUser()` 方法
  - ✅ 实现 `loadReviewProgress()` 方法
  - ✅ 实现 `loadMoreCards()` 方法
  - ✅ 实现 `loadMoreCollections()` 方法
  - ✅ 更新 `refresh()` 方法支持下拉刷新
  - ✅ 更新 `updateUiStateWithCards()` 支持分页
  - ✅ 更新 `DashboardModule` 依赖注入
- ✅ 阶段 5：UI 层
  - ✅ 任务 5.1: 实现下拉刷新（Material 3 PullRefresh）
  - ✅ 任务 5.2: 更新 Top App Bar，添加 "Focus & Review →" 链接和更多选项按钮
  - ✅ 任务 5.3: 实现 `AssetCollectionsModule` 组件
  - ✅ 任务 5.4: 实现 `CollectionCard` 组件（2x2 预览网格）
  - ✅ 任务 5.5: 实现 `VideoCardComponent` 组件
  - ✅ 任务 5.6: 添加 `NewCaptureCard` 占位符卡片
  - ✅ 任务 5.7: 实现分页加载（滚动到底部自动加载）
  - ✅ 重构 DashboardScreen，使用 LazyColumn 布局
  - ✅ 注册 VideoCardComponent 到 CardModule

**下一个阶段**: 阶段 6 - Bootstrap 数据 ✅ 已完成

**已完成**:
- ✅ 阶段 6：Bootstrap 数据
  - ✅ 任务 6.1: 创建 `collection_card.json` Bootstrap 数据文件
  - ✅ 任务 6.2: 添加 video 卡片数据到 `card.json`
  - ✅ 任务 6.3: 创建 `card_metadata_video.json` Bootstrap 数据文件
  - ✅ 任务 6.4: 更新 `DefaultBootstrapConfigBuilder` 支持加载 collection_card 和 video metadata
  - ✅ 创建 `CollectionCard` 数据类
  - ✅ 更新 `BootstrapConfig` 添加 `collectionCards` 字段
  - ✅ 更新 `Bootstrap.kt` 插入 collection_card 数据
  - ✅ 在 `LocalCollectionDataSource` 接口中添加 `insertCollectionCard` 方法

**所有阶段已完成！** 🎉

---

**文档结束**
