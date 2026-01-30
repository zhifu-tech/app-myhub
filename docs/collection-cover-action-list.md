# 合集封面与卡片封面实现行动清单

## 目标

达到设计稿效果：**合集（Collection）的封面由其所包含的卡片（Card）的封面自动聚合而成**。设计稿中合集封面为「一大两小」或「多格内容预览」的拼贴，支持图片与文本/代码混合展示。

## 现状简要

| 层级 | 现状 |
|------|------|
| **Card 域模型** | 无统一「封面」字段；Article 有 `coverImageUrl`，Video 有 `thumbnailUrl`，Code 有 `snippet`，其余类型仅有 `content`。 |
| **Collection 域模型** | 无封面字段；已有 `cards`（预览用）和 `cardCount`。 |
| **数据库** | `card_metadata_article` 有 `cover_image_url`，`card_metadata_video` 有 `thumbnail_url`；`card` / `collection` 表无 cover 列。 |
| **合集预览数据** | `LocalCollectionDataSourceImpl.getCollectionPreviewCards()` 已通过 `card_with_metadata` 视图拉取带元数据的完整 Card，数据足够推导封面。 |
| **UI** | `CollectionCard` 使用 `getContentPreview(maxLength)` 仅做纯文本截断，未区分类型，未使用 Article/Video 的图片。 |

---

## 一、定义「卡片封面」抽象（Card Cover）

**目标**：在应用内统一「一张卡片的封面」的表示，便于合集封面聚合与 UI 展示。

### 1.1 卡片封面数据抽象

- **位置建议**：`component/card` 或 `datastore/model`（若希望与域强绑定可放 model，若仅 UI 用可放 component）。
- **内容**：
  - 定义 `CardCover` 密封类或数据类，例如：
    - `CardCover.Image(url: String)`：可直接用作封面的图片 URL（Article 的 coverImageUrl、Video 的 thumbnailUrl）。
    - `CardCover.TextPreview(text: String, maxLength: Int)`：文本/代码片段预览（Code 的 snippet、或 content 截断，用于 Idea/Quote/Word/Todo 等）。
  - 或简化为：`data class CardCover(val imageUrl: String?, val textPreview: String?)`，二者可只填其一。
- **扩展函数**：`Card.toCover(): CardCover`，按 `Card.type` 与 `metadata` 决定来源：
  - **article**：`metadata.coverImageUrl` 非空 → Image(coverImageUrl)，否则 → TextPreview(summary 或 content)。
  - **video**：`metadata.thumbnailUrl` 非空 → Image(thumbnailUrl)，否则 → TextPreview(content)。
  - **code**：TextPreview(metadata.snippet 或 content)。
  - **idea / quote / word / todo**：TextPreview(content 截断)。
- **依赖**：仅依赖现有 `Card` 与各 `CardMetadata*`，无需改表或 API。

**行动项**：

- [ ] **1.1.1** 在选定模块中新增 `CardCover` 类型（密封类或数据类）及 `Card.toCover()` 实现。
- [ ] **1.1.2** 为各卡片类型编写单元测试，覆盖有图/无图、有 snippet/无 snippet 等分支。

---

## 二、扩展「内容预览」与组件能力（可选但推荐）

**目标**：与现有 `getContentPreview` 并存，为合集封面提供「按类型取封面」的能力，并为后续列表/搜索等场景复用。

### 2.1 内容预览策略

- **当前**：`Card.getContentPreview(maxLength)` 仅做 `content.take(maxLength)`，未按类型优化。
- **建议**：
  - **article**：优先 `metadata.summary`，再 fallback 到 `content`。
  - **code**：优先 `metadata.snippet`，再 `content`。
  - **quote / word**：可优先 `content` 或 `metadata` 中的短文本（如 definition、example）。
  - 其他类型保持或略调截断策略。
- **与封面的关系**：`CardCover.TextPreview` 可直接复用上述逻辑，避免重复实现。

**行动项**：

- [ ] **2.1.1** 扩展 `getContentPreview(maxLength)` 或新增 `getTextPreviewForCover(maxLength)`，按类型选择摘要/片段来源。
- [ ] **2.1.2** 若在 `CardComponent` 中增加「封面预览」能力（如返回 `CardCover`），在接口上声明并在各类型组件中实现（或统一在 `Card.toCover()` 中实现，组件只读）。

---

## 三、合集封面：由卡片封面聚合

**目标**：Collection 不持久化封面字段，在展示时由 `collection.cards` 前 N 条卡片的封面动态生成。

### 3.1 聚合逻辑

- **输入**：`Collection.cards`（已由 `getCollectionPreviewCards` 等提供，带完整 metadata）。
- **输出**：用于合集卡片 UI 的「封面槽位」数据，例如：
  - 方案 A：列表 `List<CardCover>`，长度最多 3（对应一大两小或两格内容）。
  - 方案 B：`CollectionCoverPreview` 数据类，包含 1 个主槽位 + 2 个次槽位，每个槽位为 `CardCover`。
- **规则**：
  - 取 `cards.take(3)`（或设计稿规定的数量），依次得到 `CardCover`。
  - 若某卡为 `CardCover.Image`，对应格可渲染图片；若为 `CardCover.TextPreview`，渲染文本/代码片段。
  - 不足 3 张时，剩余格显示占位符（如设计稿中的灰色块或图标）。

**行动项**：

- [ ] **3.1.1** 在 feature/dashboard 或 shared 处实现 `Collection.coverPreviews(): List<CardCover>`（或等价的 `CollectionCoverPreview`），内部调用 `cards.take(3).map { it.toCover() }`。
- [ ] **3.1.2** 确定设计稿中「一大两小」对应的槽位顺序（例如 index 0 为主图/主预览，1、2 为次格），在文档或代码注释中固定约定。

---

## 四、UI：合集卡片封面区域

**目标**：`CollectionCard` 的封面区域与设计稿一致，支持图片 + 文本/代码混合及占位。

### 4.1 布局与视觉

- **布局**：当前已有一大两小（左侧 weight 2，右侧两格各 weight 1）；保持或微调为设计稿比例。
- **每格内容**：
  - 若该槽位为 `CardCover.Image(url)`：使用 `AsyncImage`/项目现有图片组件加载 `url`，圆角、裁剪与设计稿一致。
  - 若为 `CardCover.TextPreview(text)`：显示短文本/代码片段，样式与当前 `CollectionCard` 内文本一致（可复用或略调字体/行数）。
  - 无卡片或空槽位：占位块（背景色 + 可选图标），与设计稿「Learning Lab」等空格一致。
- **深色背景**：当前已用 `Color(0xFF1E1F23)`，按设计稿可保留或统一到主题。

**行动项**：

- [ ] **4.1.1** 在 `CollectionCard` 中，将 `collection.cards` 的展示从「仅 getContentPreview 文本」改为使用 `Collection.coverPreviews()`（或等价结构）。
- [ ] **4.1.2** 每个槽位根据 `CardCover` 类型分支：图片用图片组件，文本用 Text/代码样式；无内容时渲染占位块。
- [ ] **4.1.3** 统一圆角、间距、最大行数/字数，使之符合设计稿（Asset Collections 与第二张设计稿的合集列表）。
- [ ] **4.1.4** 若有「10/15 Start Review」等与合集无关的模块，保持不动；仅改合集卡片的封面区域。

---

## 五、服务端与 API（可选）

**目标**：若希望服务端在列表接口中直接返回「封面信息」，可扩展 DTO；否则客户端完全从已有 `cards` 推导即可。

### 5.1 当前 API

- **Collection 列表/详情**：返回 `CollectionResponse`（id, name, topic, description, userId, createdAt, updatedAt），不包含 `cards` 或 cover。
- **合集内卡片**：由客户端本地通过 `getCollectionPreviewCards` 等从 DB 查询带 metadata 的 Card；服务端若未在列表里带 cards，则客户端必须本地组装修正。

### 5.2 可选扩展

- 若服务端在「获取合集列表」时一并返回每条合集的 `previewCards`（含 metadata），则客户端可直接用这些 cards 计算封面，无需额外请求。
- 若不在服务端返回 previewCards，则保持现状：客户端通过 Store/Repository 拉取 collections 时已带 `cards`，封面完全在客户端由 `Card.toCover()` + `Collection.coverPreviews()` 计算。

**行动项**：

- [ ] **5.2.1** 决定合集列表 API 是否返回 `previewCards`；若不返回，确认客户端在 Dashboard 等场景下已能拿到带 `cards` 的 `Collection`（当前实现已满足）。
- [ ] **5.2.2** 若未来在服务端增加 `collection.coverUrls` 等字段，再在 `CollectionResponse`、DB、同步逻辑中增加对应项；当前阶段可不做。

---

## 六、数据层与同步

**目标**：不新增持久化字段的前提下，利用现有数据完成封面展示。

### 6.1 数据库与迁移

- **结论**：无需在 `card` 或 `collection` 表增加 cover 列；封面由 `card_with_metadata` 视图中已有的 `article_cover_image_url`、`video_thumbnail_url`、`code_snippet` 等与 `content` 推导。
- **行动项**：无迁移任务；仅需确认各端（Android/iOS/Web/JVM）使用的 DB 与视图与当前一致。

### 6.2 同步与一致性

- 若卡片或元数据通过同步更新，封面会随 `cards` 和 metadata 自动更新，无需单独同步「封面」。
- **行动项**：无需为封面增加同步逻辑；若后续增加服务端 cover 字段，再在 sync 中处理。

---

## 七、测试与验收

- [ ] **7.1** 单元测试：`Card.toCover()` 各类型、有/无图片、有/无 snippet、空 content。
- [ ] **7.2** 单元测试：`Collection.coverPreviews()` 在 0/1/2/3 张卡片时的槽位数量与顺序。
- [ ] **7.3** UI 验收：合集列表与设计稿一致（一大两小、图片+文本混合、占位符）；多合集、少卡片的合集均有一致表现。
- [ ] **7.4** 回归：Dashboard 其他区块（最近捕获、Focus & Review 等）无回归。

---

## 八、实施顺序建议

1. **阶段 1（数据与抽象）**：完成 §1 卡片封面抽象与 `Card.toCover()`，以及 §2.1 的预览策略扩展。
2. **阶段 2（合集聚合）**：完成 §3.1 合集封面聚合逻辑与槽位约定。
3. **阶段 3（UI）**：完成 §4.1 的 `CollectionCard` 改造与占位、样式。
4. **阶段 4（可选）**：若需要，再考虑 §5 服务端返回 previewCards 或未来 cover 字段，以及 §6.2 同步。
5. **收尾**：§7 测试与验收。

---

## 九、小结表

| 模块 | 工作内容 | 是否必须 |
|------|----------|----------|
| **component/card 或 model** | 定义 `CardCover`、实现 `Card.toCover()` | 必须 |
| **component/card** | 扩展 `getContentPreview`/按类型预览策略 | 推荐 |
| **feature/dashboard** | `Collection.coverPreviews()` 或等价聚合 | 必须 |
| **feature/dashboard** | `CollectionCard` 使用封面聚合、图片+文本+占位 | 必须 |
| **服务端 API** | 扩展 Collection 返回 previewCards/cover | 可选 |
| **数据库** | 新增 cover 相关列 | 不需要 |
| **同步** | 封面专用同步 | 不需要 |

按上述清单逐项完成即可在应用内实现「卡片封面 → 合集封面自动聚合」并贴近设计稿效果。
