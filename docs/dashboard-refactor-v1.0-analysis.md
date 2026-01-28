# Dashboard 重构分析文档 v1.0

> 依据 `/Users/zzf/Downloads/myhub-home-v1.0` 重构 dashboard  
> 文档版本：v1.0  
> 创建时间：2026-01-28

---

## 一、从 HTML 提取的规格说明

### 1. Top App Bar（顶部应用栏）

**HTML 规格**：
```html
<header>
  <h1>Good evening, Scholar</h1>
  <div>
    <span>You have 5 cards to review today.</span>
    <span>•</span>
    <a>Focus & Review →</a>
  </div>
  <div>
    <button>search</button>
    <button>refresh</button>
    <button>more_vert</button>
  </div>
</header>
```

**规格说明**：
- **问候语**：根据时间动态显示（Good morning/afternoon/evening, Scholar）
- **副标题**：显示待复习卡片数量 "You have {count} cards to review today."
- **操作链接**：紫色文本链接 "Focus & Review →"，点击跳转到复习流程
- **操作按钮**：三个图标按钮（搜索、刷新、更多选项）

**当前实现状态**：✅ 部分实现
- ✅ 问候语已实现（但固定为 "Good evening"）
- ✅ 副标题已实现（但使用 `recentEdits` 而非 `reviewCardsCount`）
- ❌ 缺少 "Focus & Review →" 链接
- ✅ 搜索、刷新按钮已实现
- ❌ 缺少更多选项按钮

---

### 2. Focus & Review 模块（核心行动入口）

**HTML 规格**：
```html
<section>
  <div>FOCUS & REVIEW</div>
  <div>
    <div>10/15</div> <!-- 环形进度 -->
  </div>
  <button>Start Review →</button>
  <button>X</button> <!-- 关闭按钮 -->
</section>
```

**规格说明**：
- **功能标签**：显示 "FOCUS & REVIEW"（小写：focus & review）
- **环形进度指示器**：显示已完成/总数（如 10/15）
- **主按钮**：紫色填充按钮 "Start Review →"
- **关闭按钮**：右上角 X 图标，可关闭此模块
- **容器**：Elevated Card，深色背景

**当前实现状态**：✅ 已实现
- ✅ `FocusReviewModule` 组件已实现
- ✅ 环形进度指示器已实现
- ✅ Start Review 按钮已实现
- ✅ 关闭按钮已实现
- ⚠️ 进度数据为硬编码（10/15），需要从实际数据计算

---

### 3. Asset Collections 模块（内容集合浏览）

**HTML 规格**：
```html
<section>
  <div>
    <h2>Asset Collections</h2>
    <span>info</span> <!-- 信息图标 -->
    <a>View All</a>
  </div>
  <div class="grid grid-cols-4">
    <div>
      <div class="card-grid-preview">
        <!-- 2x2 图片网格 -->
      </div>
      <h3>Literature Classics</h3>
      <p>15 Assets</p>
    </div>
    <!-- 其他 3 个集合卡片 -->
  </div>
</section>
```

**规格说明**：
- **标题**："Asset Collections" + 信息图标（hover 显示提示）
- **操作链接**："View All" 链接（右上角）
- **布局**：响应式网格（移动端 1 列，平板 2 列，桌面 4 列）
- **集合卡片结构**：
  - 图片预览区域（2x2 网格布局，显示集合中的卡片预览图）
  - 集合名称（如 "Literature Classics"）
  - 资产数量（如 "15 Assets"）
- **集合列表**（4 个）：
  1. Literature Classics (15 Assets)
  2. Algorithm Mastery (8 Assets)
  3. AI Trends (12 Assets)
  4. Learning Lab (13 Assets)

**当前实现状态**：❌ 未实现
- ❌ 缺少 `AssetCollectionsModule` 组件
- ❌ 缺少集合卡片组件
- ❌ 缺少集合数据加载逻辑

---

### 4. Latest Captures 模块（最近捕获卡片）

**HTML 规格**：
```html
<section>
  <div>
    <h2>Latest Captures</h2>
    <span>info</span>
  </div>
  <div class="waterfall-grid">
    <!-- 瀑布流卡片 -->
  </div>
</section>
```

**规格说明**：
- **标题**："Latest Captures" + 信息图标
- **布局**：响应式瀑布流（移动端 1 列，平板 2 列，桌面 4 列）
- **卡片类型**（HTML 中出现的）：
  1. **Literature**：引文卡片（带作者、日期）
  2. **Algo**：代码片段卡片（带语法高亮、标签）
  3. **Thought**：想法卡片（带标题、描述、发音）
  4. **Quote**：引用卡片（带作者头像、来源）
  5. **Video Summary**：视频摘要卡片（带缩略图、播放按钮、时长）
  6. **Article**：文章卡片（带标题、摘要、链接、NEW 标记）
  7. **Reminder**：提醒卡片（带时间、待办清单）
  8. **New Capture**：新建占位符卡片（虚线边框、加号图标）

**当前实现状态**：✅ 部分实现
- ✅ 瀑布流布局已实现
- ✅ 基础卡片组件已实现
- ⚠️ 缺少部分卡片类型（Video Summary、Reminder）
- ❌ 缺少 "New Capture" 占位符卡片

---

## 二、数据结构检查

### 2.1 Collection 模型

**当前模型**：
```kotlin
data class Collection(
    val id: String,
    val name: String,
    val topic: String? = null,
    val description: String? = null,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

**检查结果**：✅ 已满足要求
- ✅ 包含所有必需字段
- ⚠️ **缺少**：集合中的卡片数量（需要计算或缓存）

**问题**：如何获取集合中的卡片数量？
- **方案 A**：通过 `collection_card` 关联表（需要创建）
- **方案 B**：通过 `tag` 关联（当前设计）
- **方案 C**：通过 `topic` 字段筛选（当前设计）

**建议**：需要确认 Collection 和 Card 的关联方式。

---

### 2.2 Card 模型

**当前模型**：
```kotlin
data class Card(
    val id: String,
    val type: CardType,
    val title: String? = null,
    val content: String,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val metadata: CardMetadata? = null,
    val tags: List<Tag> = emptyList()
)
```

**检查结果**：✅ 已满足要求
- ✅ 包含所有必需字段
- ✅ 支持多种卡片类型（quote, code, idea, article, word, todo）
- ⚠️ **缺少**：视频类型（video）支持

**问题**：HTML 中出现的 "Video Summary" 类型是否对应现有的 `video` 类型？
- 需要确认是否需要在 CardType 中添加 `video` 类型。

---

### 2.3 ReviewProgress 模型

**当前模型**：
```kotlin
data class ReviewProgress(
    val completed: Int = 0,
    val total: Int = 0
) {
    val progress: Float
        get() = if (total > 0) completed.toFloat() / total.toFloat() else 0f
}
```

**检查结果**：✅ 已满足要求
- ✅ 包含已完成和总数
- ⚠️ **问题**：如何计算待复习卡片数量？

**问题**：待复习卡片的判定逻辑
- 需要 `user_card.last_reviewed_at` 字段（✅ 已存在）
- 需要定义复习规则（如：超过 7 天未复习的卡片）

---

### 2.4 Collection 和 Card 的关联

**当前数据库设计**：
- ❌ 没有 `collection_card` 表
- ✅ 有 `user_collection` 表（权限关系）
- ✅ 有 `card_tag` 表（标签关联）

**问题**：如何将 Card 关联到 Collection？
- **方案 A**：创建 `collection_card` 表（推荐）
  ```sql
  CREATE TABLE collection_card (
    collection_id TEXT NOT NULL,
    card_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    PRIMARY KEY (collection_id, card_id),
    FOREIGN KEY (collection_id) REFERENCES collection(id),
    FOREIGN KEY (card_id) REFERENCES card(id)
  );
  ```
- **方案 B**：通过 `tag` 关联（当前可能的方式）
- **方案 C**：通过 `topic` 字段筛选（当前可能的方式）

**建议**：需要确认关联方式，建议创建 `collection_card` 表以支持多对多关系。

---

## 三、需要修改的部分

### 3.1 DashboardUiState 修改

**需要添加**：
```kotlin
data class Content(
    // ... 现有字段 ...
    
    // 新增字段
    val collections: List<Collection> = emptyList(),
    val collectionCardCounts: Map<String, Int> = emptyMap(), // collectionId -> cardCount
    val reviewCardsCount: Int = 0, // 待复习卡片数量
    // reviewProgress 已存在，但需要从实际数据计算
)
```

**修改位置**：
- `feature/dashboard/src/commonMain/kotlin/tech/zhifu/app/myhub/feature/dashboard/DashboardUiState.kt`

---

### 3.2 DashboardViewModel 修改

**需要添加**：
```kotlin
class DashboardViewModel {
    // 新增方法
    private fun loadCollections(userId: String)
    private fun calculateReviewProgress(cards: List<Card>, userId: String): ReviewProgress
    private fun calculateCollectionCardCounts(collections: List<Collection>, userId: String): Map<String, Int>
}
```

**需要修改**：
- `loadDashboardData()`：添加集合数据加载
- `updateUiStateWithCards()`：添加集合数据和卡片数量计算

**修改位置**：
- `feature/dashboard/src/commonMain/kotlin/tech/zhifu/app/myhub/feature/dashboard/DashboardViewModel.kt`

---

### 3.3 DashboardScreen 修改

**需要添加组件**：
1. `AssetCollectionsModule`：资产集合模块
2. `CollectionCard`：集合卡片组件
3. `NewCaptureCard`：新建捕获占位符卡片
4. 更新 Top App Bar，添加 "Focus & Review →" 链接

**需要修改**：
- Top App Bar：添加 "Focus & Review →" 链接
- Latest Captures：添加 "New Capture" 占位符卡片
- 支持更多卡片类型（Video Summary、Reminder）

**修改位置**：
- `feature/dashboard/src/commonMain/kotlin/tech/zhifu/app/myhub/feature/dashboard/DashboardScreen.kt`

---

### 3.4 Repository 修改

**需要添加**（如果使用 `collection_card` 表）：
```kotlin
interface CollectionRepository {
    suspend fun getCollectionCardCount(collectionId: String): Int
    suspend fun getCollectionCardCounts(collectionIds: List<String>): Map<String, Int>
}
```

**修改位置**：
- `datastore/repository-client-api/src/commonMain/kotlin/tech/zhifu/app/myhub/datastore/repository/collection/CollectionRepository.kt`

---

## 四、Bootstrap 数据补齐

### 4.1 Collection 数据

**当前状态**：✅ 已满足要求
- ✅ 已有 4 个集合（Literature Classics, Algorithm Mastery, AI Trends, Learning Lab）
- ✅ 数据格式正确

**文件位置**：
- `datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/collection.json`

---

### 4.2 Card 数据

**当前状态**：⚠️ 需要补充
- ✅ 已有 12 张卡片
- ⚠️ 需要确保卡片类型覆盖 HTML 原型中的所有类型
- ⚠️ 需要添加视频类型卡片（如果支持）
- ⚠️ 需要确保卡片日期符合 HTML 原型（Oct 22-24, 2023）

**需要补充的卡片**（根据 HTML 原型）：
1. ✅ Literature 卡片（已有）
2. ✅ Algo 卡片（已有）
3. ✅ Thought 卡片（已有，word 类型）
4. ✅ Quote 卡片（已有）
5. ✅ Article 卡片（已有）
6. ⚠️ Video Summary 卡片（需要添加，如果支持 video 类型）
7. ⚠️ Reminder 卡片（已有，todo 类型，但需要确认格式）

**文件位置**：
- `datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/card.json`
- `datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/card_metadata_*.json`

---

### 4.3 Collection 和 Card 的关联数据

**当前状态**：❌ 缺失
- ❌ 没有 `collection_card` 关联数据
- ⚠️ 需要确认关联方式

**如果使用 `collection_card` 表，需要添加**：
```json
[
  {
    "collectionId": "collection-001",
    "cardId": "card-001"
  },
  // ... 更多关联
]
```

**文件位置**（如果创建）：
- `datastore/bootstrap/src/commonMain/composeResources/files/bootstrap_default/collection_card.json`

---

## 五、待确认的问题

### 5.1 Collection 和 Card 的关联方式

**问题**：如何将 Card 关联到 Collection？

**选项**：
1. **创建 `collection_card` 表**（推荐）
   - 优点：明确的多对多关系，易于查询和维护
   - 缺点：需要数据库迁移
2. **通过 `tag` 关联**
   - 优点：利用现有设计
   - 缺点：不够直观，需要约定 tag 命名规则
3. **通过 `topic` 字段筛选**
   - 优点：简单直接
   - 缺点：不够灵活，一个卡片只能属于一个集合

**建议**：创建 `collection_card` 表，支持多对多关系。

---

### 5.2 待复习卡片的判定逻辑

**问题**：如何判定卡片需要复习？

**选项**：
1. **基于 `user_card.last_reviewed_at`**
   - 规则：超过 N 天未复习的卡片（如 7 天）
   - 优点：基于用户实际行为
   - 缺点：需要初始化 `user_card` 数据
2. **基于 `card.created_at`**
   - 规则：创建后超过 N 天未复习的卡片
   - 优点：简单直接
   - 缺点：不够精确
3. **基于 `card.updated_at`**
   - 规则：更新后超过 N 天未复习的卡片
   - 优点：考虑内容更新
   - 缺点：不够精确

**建议**：使用方案 1，基于 `user_card.last_reviewed_at`，默认规则为 7 天。

---

### 5.3 Focus & Review 的进度计算

**问题**：10 / 15 的含义是什么？

**选项**：
1. **已完成复习 / 总待复习卡片**
   - 已完成：`last_reviewed_at` 在最近 N 天内的卡片
   - 总待复习：需要复习的卡片总数
2. **今日已复习 / 今日待复习**
   - 今日已复习：今天已复习的卡片数
   - 今日待复习：今天需要复习的卡片数
3. **本周已复习 / 本周待复习**
   - 本周已复习：本周已复习的卡片数
   - 本周待复习：本周需要复习的卡片数

**建议**：使用方案 1，基于最近 7 天的复习情况。

---

### 5.4 视频类型支持

**问题**：HTML 中出现的 "Video Summary" 类型是否支持？

**选项**：
1. **添加 `video` 类型**
   - 需要：在 CardType 中添加 `video`
   - 需要：创建 `card_metadata_video` 表
   - 需要：在 Bootstrap 中添加视频卡片数据
2. **使用 `article` 类型**
   - 优点：利用现有设计
   - 缺点：不够语义化
3. **暂不支持**
   - 优点：简化实现
   - 缺点：不符合 HTML 原型

**建议**：如果需要完全符合 HTML 原型，建议添加 `video` 类型支持。

---

## 六、实施计划

### 阶段 1：数据结构确认（待用户确认）

1. ✅ 确认 Collection 和 Card 的关联方式
2. ✅ 确认待复习卡片的判定逻辑
3. ✅ 确认 Focus & Review 的进度计算规则
4. ✅ 确认视频类型支持

### 阶段 2：数据结构修改（待确认后）

1. ⏳ 创建 `collection_card` 表（如果确认）
2. ⏳ 添加视频类型支持（如果确认）
3. ⏳ 更新 Repository 接口

### 阶段 3：Bootstrap 数据补齐

1. ⏳ 补充 Card 数据（确保覆盖所有类型）
2. ⏳ 添加 `collection_card` 关联数据（如果确认）
3. ⏳ 添加视频卡片数据（如果确认）

### 阶段 4：ViewModel 和 State 修改

1. ⏳ 更新 `DashboardUiState`，添加集合相关字段
2. ⏳ 更新 `DashboardViewModel`，添加集合数据加载逻辑
3. ⏳ 实现复习进度计算逻辑
4. ⏳ 实现集合卡片数量计算逻辑

### 阶段 5：UI 组件实现

1. ⏳ 实现 `AssetCollectionsModule` 组件
2. ⏳ 实现 `CollectionCard` 组件
3. ⏳ 更新 Top App Bar，添加 "Focus & Review →" 链接
4. ⏳ 添加 "New Capture" 占位符卡片
5. ⏳ 支持更多卡片类型（Video Summary、Reminder）

### 阶段 6：测试和优化

1. ⏳ 测试所有新功能
2. ⏳ 优化性能和用户体验
3. ⏳ 修复发现的问题

---

## 七、总结

### 已完成的工作
- ✅ HTML 规格提取
- ✅ 现有实现分析
- ✅ 数据结构检查
- ✅ Bootstrap 数据检查
- ✅ 修改清单整理

### 待确认的问题
1. **Collection 和 Card 的关联方式**（最重要）
2. **待复习卡片的判定逻辑**
3. **Focus & Review 的进度计算规则**
4. **视频类型支持**

### 下一步行动
1. **等待用户确认**上述 4 个问题
2. **确认后开始实施**阶段 2-6

---

## 附录：HTML 原型中的关键数据

### 集合数据
```json
[
  { "name": "Literature Classics", "count": 15 },
  { "name": "Algorithm Mastery", "count": 8 },
  { "name": "AI Trends", "count": 12 },
  { "name": "Learning Lab", "count": 13 }
]
```

### 卡片数据示例
```json
[
  {
    "type": "literature",
    "date": "Oct 24, 2023",
    "content": "The more that you read...",
    "author": "Dr. Seuss"
  },
  {
    "type": "algo",
    "date": "Oct 23, 2023",
    "title": "Quick Sort Algorithm",
    "code": "function quickSort(arr) {...}",
    "tags": ["Javascript", "Algorithm"]
  },
  {
    "type": "thought",
    "date": "Oct 22, 2023",
    "title": "Serendipity",
    "pronunciation": "/ ˌserənˈdipədē /"
  },
  {
    "type": "quote",
    "content": "The only way to do great work...",
    "author": "Steve Jobs",
    "source": "Stanford Commencement"
  },
  {
    "type": "video_summary",
    "title": "The Future of Generative AI",
    "duration": "12:45"
  },
  {
    "type": "article",
    "title": "Understanding the Transformer Architecture",
    "mark": "New"
  },
  {
    "type": "reminder",
    "title": "WEEKLY REVIEW",
    "time": "Sunday 9:00 PM",
    "items": ["Archive completed assets", "Tag new captures", "Set goals for next week"]
  }
]
```

---

**文档结束**
