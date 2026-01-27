# Dashboard 重构分析文档

## 一、从 HTML 提取的规格说明

### 1. Top App Bar
- **问候语**: "Good evening, Scholar"（根据时间变化）
- **副标题**: "You have 5 cards to review today. · Focus & Review →"
- **操作按钮**: 搜索、刷新、更多（三个图标按钮）

### 2. Focus & Review 模块（缺失）
- **功能标签**: "FOCUS & REVIEW"
- **环形进度**: 显示 10 / 15（已完成/总数）
- **主按钮**: "Start Review →"

### 3. Asset Collections 模块（缺失）
- **标题**: "Asset Collections" + 信息图标
- **操作**: "View All" 链接
- **内容**: 4 个集合卡片，横向排列
  - Literature Classics (15 Assets)
  - Algorithm Mastery (8 Assets)
  - AI Trends (12 Assets)
  - Learning Lab (13 Assets)
- **卡片结构**: 
  - 图片预览（2x2 网格布局）
  - 集合名称
  - 资产数量

### 4. Latest Captures 模块（部分实现）
- **标题**: "Latest Captures" + 信息图标
- **布局**: 瀑布流（响应式：1列/2列/4列）
- **卡片类型**:
  - Literature（引文）
  - Algo（代码）
  - Thought（想法）
  - Quote（引用）
  - Video Summary（视频摘要）
  - Article（文章）
  - Reminder（提醒）
  - New Capture（新建占位符）

## 二、需要修改的部分

### 1. Bootstrap 数据补齐

#### 1.1 Collection 数据
**当前**: 只有 1 个集合（入门合集）
**需要**: 添加 4 个集合
- Literature Classics
- Algorithm Mastery
- AI Trends
- Learning Lab

#### 1.2 Card 数据
**当前**: 有 8 张卡片
**需要**: 
- 确保有足够的卡片来展示 Latest Captures
- 需要支持多种卡片类型（Literature, Algo, Thought, Quote, Video Summary, Article, Reminder）

### 2. DashboardViewModel 修改

**需要添加**:
- `reviewCardsCount: Int` - 待复习卡片数量
- `reviewProgress: Pair<Int, Int>` - 复习进度（已完成/总数）
- `collections: List<Collection>` - 集合列表
- `collectionCardCounts: Map<String, Int>` - 每个集合的卡片数量

**需要修改**:
- `loadDashboardData()` - 添加集合数据加载
- 添加 `loadCollections()` 方法

### 3. DashboardUiState 修改

**需要添加**:
- `reviewCardsCount: Int`
- `reviewProgress: Pair<Int, Int>`
- `collections: List<Collection>`
- `collectionCardCounts: Map<String, Int>`

### 4. DashboardScreen 修改

**需要添加**:
- `FocusReviewModule` 组件
- `AssetCollectionsModule` 组件
- 更新 Top App Bar，添加 "Focus & Review →" 链接
- 完善 Latest Captures 模块（支持更多卡片类型，添加 New Capture 占位符）

### 5. 数据结构检查

**Collection 模型**: ✅ 已满足要求
- `id`, `name`, `topic`, `description`, `userId`, `createdAt`, `updatedAt`

**Card 模型**: ✅ 已满足要求
- `id`, `type`, `title`, `content`, `userId`, `createdAt`, `updatedAt`, `tags`

**需要确认**:
- Collection 和 Card 之间的关系（如何获取集合中的卡片数量）
- 待复习卡片的判定逻辑（需要 `user_card.last_reviewed_at` 字段）

## 三、实施计划

1. ✅ 分析 HTML 规格和设计文档
2. ⏳ 补齐 Bootstrap 数据（Collection 和 Card）
3. ⏳ 更新 DashboardViewModel 和 DashboardUiState
4. ⏳ 实现 Focus & Review 模块
5. ⏳ 实现 Asset Collections 模块
6. ⏳ 更新 Top App Bar
7. ⏳ 完善 Latest Captures 模块

## 四、待确认的问题

1. **Collection 和 Card 的关系**: 如何获取集合中的卡片数量？
   - 是否需要 `collection_card` 表？
   - 还是通过 `tag` 或其他方式关联？

2. **待复习卡片的判定**: 
   - 如何判定卡片需要复习？
   - 是否需要 `user_card.last_reviewed_at` 字段？

3. **Focus & Review 的进度计算**:
   - 10 / 15 的含义是什么？
   - 如何计算已完成和总数？
