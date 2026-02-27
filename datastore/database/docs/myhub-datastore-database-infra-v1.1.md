# MyHub 数据库模块方案设计 v1.1

**方案名称**：Datastore Database Infra v1.1  
**文档版本**：v1.1  
**文档类型**：技术方案设计文档  
**创建日期**：2026-02-03  
**最后更新**：2026-02-04  
**作者**：MyHub Development Team  
**评审状态**：🟡 待评审  
**方案状态**：📝 进行中  

**依据文档**：[卡片数据模型设计 — 基于产品视角](../../../docs/产品/myhub-卡片数据模型设计-基于产品视角.md)。本版 Schema 与领域模型（Card / CardMetadata / CardReview / collection_card）对齐，不兼顾 v1.0 迁移成本。

---

## 📋 文档目录

1. [修改历史](#修改历史)
2. [方案状态摘要](#-方案状态摘要)
3. [问题背景](#1-问题背景)
4. [设计目标](#2-设计目标)
5. [技术调研](#3-技术调研)
6. [架构设计](#4-架构设计)
7. [实现细节](#5-实现细节)
8. [实施计划](#6-实施计划)
9. [风险评估](#7-风险评估)
10. [附录](#8-附录)

---

## 📊 方案状态摘要

**当前状态**：

- **评审状态**：🟡 待评审（v1.1 设计对齐产品模型文档，待评审后实施）
- **方案状态**：📝 进行中 - v1.1 为相对 v1.0 的 Schema 升级，按「卡片数据模型设计—基于产品视角」重定义 card / card_metadata / card_review
- **依据**：`docs/产品/myhub-卡片数据模型设计-基于产品视角.md`

**v1.1 相对 v1.0 的主要变更**：

- **card**：增加 `type`（review/do/material）、`source`（link/extract/own）、`carriers`（JSON）、`user_id`；移除 `title`、`content`（归属 card_metadata），标签改由 `card_tag` 关联表承载。
- **card_metadata**：由单表大 JSON 改为**按块独立表**（content / attribution / carrier_image / link / site / carrier_video / code / lexicon / execution），一卡在某块有数据则对应表有一行；新增块类型时**仅新增表**，符合开闭原则、利于扩展。
- **card_review**：**新增**独立表，复看不落 card；新建时根据 reviewSuggestions 插入，需要时按 card_id 获取。
- **collection_card**：保留，表示「已加入的卡集」；建议加入的卡集不落库，仅创建/再分析流程中返回。

---

## 修改历史

| 版本   | 日期         | 修改内容                         | 修改原因 |
|--------|--------------|----------------------------------|----------|
| v1.0   | 2026-01-20   | 全新方案设计                     | 新建     |
| v1.1   | 2026-02-03   | 对齐产品模型文档：card 壳化、单表 metadata、card_review 独立表 | 依据「卡片数据模型设计—基于产品视角」重定义 Schema |

---

## 1. 问题背景

### 1.1 用户场景

在 v1.0 基线之上，产品与领域模型已按「卡片数据模型设计—基于产品视角」重定义，数据库 Schema 需与之对齐，确保：

1. **Card 壳化**：Card 表只保留身份、类型、来源、载体、用户与时间；content/title 归属 metadata，复看归属 card_review，标签由 `card_tag` 表承载。
2. **类型与来源**：`type` 仅三种（review/do/material），`source` 三种（link/extract/own），`carriers` 有序 JSON 表示主次载体。
3. **元数据按块独立表**：每种块对应一张表（如 card_metadata_content、card_metadata_link、card_metadata_carrier_video 等），一卡在某块有数据则该表有一行；新增块类型时**仅新增表**，不改现有表，符合开闭原则、元数据变化快时利于扩展。
4. **复看独立表**：复看不存 card，存 card_review；新建时根据 reviewSuggestions 插入，需要时再按 card_id 获取。
5. **建议卡集不落库**：已加入的卡集仅通过 collection_card 表示；建议加入的卡集仅在创建/再分析 API 中返回。

### 1.2 依据文档

- [卡片数据模型设计 — 基于产品视角](../../../docs/产品/myhub-卡片数据模型设计-基于产品视角.md)：领域模型（Card / CardMetadata / CardReview）、字段归属与可选性、存储形态（第五节）。

### 1.3 影响范围

- **card.sq**：列与索引变更；移除 title/content，增加 type/source/carriers/user_id，标签通过 card_tag 关联。
- **card_metadata**：由单表大 JSON 改为**按块独立表**（多张 card_metadata_* 表），每块一表。
- **新增 card_review.sq**：复看独立表。
- **废弃**：card_metadata_article、card_metadata_code、card_metadata_idea、card_metadata_quote、card_metadata_todo、card_metadata_word；card_with_metadata 视图若保留需按新表重写。

---

## 2. 设计目标

### 2.1 功能目标

- ✅ **Card 壳化**：card 表仅 id、type、source、carriers、user_id、created_at、updated_at；无 title/content/review，标签用 card_tag 关系表。
- ✅ **元数据按块独立表**：每种 metadata 块对应一张表（content / attribution / link / site / carrier_video 等）；新增块类型时仅新增表，不改现有表，符合开闭原则。
- ✅ **复看独立表**：card_review 存复看配置；新建时根据 reviewSuggestions 插入，需要时按 card_id 查询。
- ✅ **事实与主观分离**：Card 为事实；user_card 为主观关系；collection_card 为「已加入的卡集」关系。
- ✅ **索引与 CRUD**：为新列与表提供索引及基础 CRUD。

### 2.2 非功能目标

- ✅ **与领域模型一致**：Schema 与产品模型文档中「最优存储与 API 设计」一一对应。
- ✅ **跨平台一致**：继续使用 SQLDelight + SQLite，KMP 统一 Schema。

### 2.3 模块特性说明

`datastore/database` 仅负责 **Schema、索引与查询定义**，不包含业务逻辑或平台驱动实现。

---

## 3. 技术调研

与 v1.0 一致：SQLDelight 管理 Schema 与查询，SQLite 为引擎；v1.1 仅调整表结构与列，不更换技术栈。

---

## 4. 架构设计

### 4.1 模块结构（v1.1）

```text
datastore/database/
├── src/
│   └── commonMain/
│       └── sqldelight/
│           └── tech/zhifu/app/myhub/datastore/database/
│               ├── card.sq              -- 卡片主表（壳）
│               ├── card_metadata_content.sq
│               ├── card_metadata_attribution.sq
│               ├── card_metadata_carrier_image.sq
│               ├── card_metadata_link.sq
│               ├── card_metadata_site.sq
│               ├── card_metadata_carrier_video.sq
│               ├── card_metadata_code.sq
│               ├── card_metadata_lexicon.sq
│               ├── card_metadata_execution.sq
│               ├── card_review.sq       -- 复看独立表（新增）
│               ├── card_tag.sq
│               ├── card_template.sq
│               ├── collection.sq
│               ├── collection_card.sq
│               ├── tag.sq
│               ├── user.sq
│               ├── user_card.sq
│               ├── user_card_type_statistics.sq
│               ├── user_collection.sq
│               ├── user_preferences.sq
│               ├── user_statistics.sq
│               ├── (sync_*.sq / bookkeeper.sq 等按需保留)
│               └── (移除 card_metadata_article/code/idea/quote/todo/word、card_with_metadata 或重写)
└── build.gradle.kts
```

### 4.2 数据库 Schema（v1.1）

#### 4.2.1 核心事实与关系表

**user 表**：与 v1.0 一致（略）。

**user_preferences 表**：与 v1.0 一致（略）。其中 `default_card_type` 可选改为与新区间一致（review/do/material）。

**card 表（v1.1）**：

- 仅保留「壳」字段：id、type、source、carriers、user_id、created_at、updated_at。
- 无 title、content、review；content/title 在 card_metadata 的 metadata JSON 内（content/summary/title 等可选块）。

```sql
CREATE TABLE card (
    id TEXT PRIMARY KEY NOT NULL,
    type TEXT NOT NULL,                    -- 'review' | 'do' | 'material'
    source TEXT NOT NULL,                  -- 'link' | 'extract' | 'own'
    carriers TEXT NOT NULL,                -- JSON array, ordered, first = primary carrier e.g. ["text","image"]
    user_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

**user_card 表**：与 v1.0 一致（主观关系：is_favorite, last_reviewed_at 等）。

**collection 表**：与 v1.0 一致。

**user_collection 表**：与 v1.0 一致。

**tag 表**：与 v1.0 一致。

**card_tag 表**：与 v1.0 一致（多对多；作为标签关系的唯一事实来源）。

**collection_card 表**：与 v1.0 一致，表示「已加入的卡集」；建议加入的卡集不落库。

**card_template 表**：与 v1.0 一致；type 可选与新区间一致。

#### 4.2.2 元数据表（v1.1：按块独立表，符合开闭原则）

元数据**不采用单表大 JSON**：单表存整块 JSON 不利于扩展，且元数据是变化很快的区域，不符合开闭原则。改为**按块独立表**：每种块对应一张表，一卡在该块有数据则对应表有一行；**新增块类型时仅新增表**，不改现有表，利于扩展。

各表均以 **card_id** 为主键、外键引用 **card(id) ON DELETE CASCADE**；应用层按 card_id 从各表读取后组装为领域 CardMetadata。

**card_metadata_content**（正文/标题/摘要）：

```sql
CREATE TABLE card_metadata_content (
    card_id TEXT PRIMARY KEY NOT NULL,
    content TEXT,
    title TEXT,
    summary TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_attribution**（作者/出处/语言）：

```sql
CREATE TABLE card_metadata_attribution (
    card_id TEXT PRIMARY KEY NOT NULL,
    author TEXT,
    source TEXT,
    language TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_carrier_image**（载体为图：封面/缩略图）：

```sql
CREATE TABLE card_metadata_carrier_image (
    card_id TEXT PRIMARY KEY NOT NULL,
    cover_image_url TEXT,
    thumbnail_url TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_link**（来源为 Link：仅 url）：

```sql
CREATE TABLE card_metadata_link (
    card_id TEXT PRIMARY KEY NOT NULL,
    url TEXT NOT NULL,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_site**（站点信息）：

```sql
CREATE TABLE card_metadata_site (
    card_id TEXT PRIMARY KEY NOT NULL,
    md_site_id TEXT,
    md_site_name TEXT,
    md_site_fav_icon TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_carrier_video**（载体为视频）：

```sql
CREATE TABLE card_metadata_carrier_video (
    card_id TEXT PRIMARY KEY NOT NULL,
    url TEXT,
    duration INTEGER,
    platform TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_code**（代码片段）：

```sql
CREATE TABLE card_metadata_code (
    card_id TEXT PRIMARY KEY NOT NULL,
    language TEXT,
    snippet TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_lexicon**（词条）：

```sql
CREATE TABLE card_metadata_lexicon (
    card_id TEXT PRIMARY KEY NOT NULL,
    pronunciation TEXT,
    definition TEXT,
    example TEXT,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**card_metadata_execution**（type=Do：状态/优先级/截止/步骤）：

```sql
CREATE TABLE card_metadata_execution (
    card_id TEXT PRIMARY KEY NOT NULL,
    status TEXT,
    priority TEXT,
    due_at TEXT,
    completed_at TEXT,
    steps TEXT,                            -- JSON: List<StepItem>
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

**设计理由**：

1. **独立表**：每块一张表，按 card_id 查询/写入，应用层组装 CardMetadata；无需单表大 JSON。
2. **开闭原则**：元数据块类型会频繁扩展；新增块时**仅新增表**，不改现有表与既有查询，符合对扩展开放、对修改关闭。
3. **扩展友好**：后续新增块（如 CarrierAudioMetadata、LocationMetadata 等）只需新增 card_metadata_* 表与对应 CRUD，不影响现有块表。

#### 4.2.3 复看表（v1.1：新增）

**card_review 表**：

- 复看不存 card，存独立表；需要时按 card_id 获取。
- 新建卡片时根据 reviewSuggestions（分析产出）生成记录并插入。

```sql
CREATE TABLE card_review (
    card_id TEXT PRIMARY KEY NOT NULL,
    kind TEXT NOT NULL,                    -- 'after_days' | 'interval' | 'custom'
    value INTEGER,                         -- e.g. 7 for 7 days later
    label TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE
);
```

#### 4.2.4 分析产出落库策略（必做）

卡片分析的「四层产出」必须在存储层有清晰归属，便于全栈一致实现：

| 分析产出 | 落库位置 | 说明 |
|----------|----------|------|
| 主类型（type） | `card.type` | 仅三种：review/do/material |
| 来源形态（source） | `card.source` | 仅三种：link/extract/own |
| 载体（carriers） | `card.carriers` | JSON 有序数组，首项为主载体 |
| 结构化元数据（metadata） | `card_metadata.metadata` | JSON：CardMetadata（可选块组合） |
| 标签（tags） | `card_tag` | 关系表为唯一事实来源 |
| 建议卡集 | **不落库** | 仅创建/再分析 API 响应返回 |
| 复看建议 | `card_review` | 由 reviewSuggestions 生成记录插入 |

**说明**：当业务层返回 `AnalysisResult` 时，应在创建流程中完成上述映射；再分析仅更新 card/card_metadata/card_review/card_tag，不产生「建议卡集」持久化。

#### 4.2.5 派生与统计表

**user_statistics 表**：与 v1.0 一致。

**user_card_type_statistics 表**：与 v1.0 一致；card_type 取值与新区间一致（review/do/material）。

#### 4.2.6 索引设计（v1.1）

```sql
-- card
CREATE INDEX idx_card_type ON card(type);
CREATE INDEX idx_card_source ON card(source);
CREATE INDEX idx_card_user_id ON card(user_id);
CREATE INDEX idx_card_created_at ON card(created_at);
CREATE INDEX idx_card_updated_at ON card(updated_at);
CREATE INDEX idx_card_user_id_type ON card(user_id, type);
CREATE INDEX idx_card_user_id_updated_at ON card(user_id, updated_at);

-- card_metadata
CREATE INDEX idx_card_metadata_card_id ON card_metadata(card_id);

-- card_review
CREATE INDEX idx_card_review_card_id ON card_review(card_id);

-- user_card
CREATE INDEX idx_user_card_user_id ON user_card(user_id);
CREATE INDEX idx_user_card_card_id ON user_card(card_id);
CREATE INDEX idx_user_card_updated_at ON user_card(user_id, updated_at);
CREATE INDEX idx_user_card_user_favorite ON user_card(user_id, is_favorite);
CREATE INDEX idx_user_card_review_status ON user_card(user_id, last_reviewed_at);

-- collection
CREATE INDEX idx_collection_user_id ON collection(user_id);

-- tag
CREATE INDEX idx_tag_user_id ON tag(user_id);
CREATE INDEX idx_tag_user_name ON tag(user_id, name);

-- card_tag
CREATE INDEX idx_card_tag_card_id ON card_tag(card_id);
CREATE INDEX idx_card_tag_tag_id ON card_tag(tag_id);

-- collection_card
CREATE INDEX idx_collection_card_collection_id ON collection_card(collection_id);
CREATE INDEX idx_collection_card_card_id ON collection_card(card_id);
```

### 4.3 版本管理

- **v1.0**：基线版本（多表 metadata、card 含 title/content）。
- **v1.1**：对齐产品模型文档；card 壳化、单表 card_metadata（metadata JSON）、新增 card_review；迁移通过 .sqm 或重建实现，本设计不规定迁移路径。

### 4.4 查询接口（v1.1 要点）

- **card**：selectCardById、insertCard、updateCard、deleteCard、selectCardsByUserId、selectCardsByUserIdAndType 等；插入/更新列与新区一致（无 title/content）。
- **card_metadata**：selectCardMetadataByCardId、insertOrReplaceCardMetadata、deleteCardMetadata；读写 metadata 列（JSON）。
- **card_review**：selectCardReviewByCardId、insertOrReplaceCardReview、updateCardReview、deleteCardReview；新建卡片后由业务层根据 reviewSuggestions 插入。
- **collection_card**：不变，表示已加入的卡集。
- **card_tag**：按 card_id 查询标签、按 tag_id 反查卡片；作为 tagIds 的唯一事实来源。
- **user_card**：不变。

---

## 5. 实现细节

### 5.1 SQLDelight 配置

与 v1.0 一致；版本号升级为 2（或按项目约定），以支持 v1.1 Schema 迁移。

### 5.2 领域原则落地（v1.1）

- **Card 是壳**：card 表无 content/title/review；展示用内容与标题来自 card_metadata.metadata（content/title/summary 等）。
- **复看独立**：复看配置与进度分离——配置在 card_review，上次复看时间在 user_card.last_reviewed_at。
- **建议卡集不落库**：仅 collection_card 表示已加入；建议加入的卡集仅在创建/再分析 API 响应中返回。
- **标签关系单一事实源**：`card_tag` 为唯一事实来源；`Card.tagIds` 在仓储层聚合得到，避免冗余与一致性问题。
- **metadata 可扩展**：新增 CardMetadata 子块时，仅应用层序列化/反序列化扩展，card_metadata 表结构不变。

### 5.3 JSON 列约定

- **card.carriers**：JSON 数组，有序，如 `["text","image"]`，首项为主载体。
- **标签关系**：通过 `card_tag` 维护；`Card.tagIds` 在仓储层聚合得到。
- **card_metadata.metadata**：JSON 对象，形如 `{ "content": {...}, "link": {...}, "carrierVideo": {...} }`，与领域 CardMetadata 结构一致；应用层按需序列化/反序列化可选子块。

### 5.4 约束与校验（应用层）

- **source=link**：`metadata.link.url` 必填；站点入口场景要求 `metadata.site.siteId` 或 `metadata.site.siteName`。
- **source=extract / own**：至少应有 `metadata.content.content` 或可展示的内容块（如 code/lexicon）。
- **carriers**：非空、有序；首项为主载体。
- **type=do**：`metadata.execution` 必填（至少包含 `status` 或 `steps` 之一）。
- **carrier=video**：`metadata.carrierVideo` 必填；`durationSeconds` 可选。
- **carrier=image**：`metadata.carrierImage` 至少包含 `coverImageUrl` 或 `thumbnailUrl`。
- **carrier=link**：若作为主载体，建议至少有 `metadata.link` 或 `metadata.site` 之一。

---

## 6. 实施计划

### 6.1 实施阶段（v1.1）

- **阶段 1**：Schema 变更——card 表结构调整、card_metadata 单表新增、card_review 新增；旧 metadata 多表及 card_with_metadata 视图废弃或迁移。
- **阶段 2**：索引与 CRUD——为新表与新列补充索引与查询。
- **阶段 3**：迁移与兼容——若需从 v1.0 迁移，编写 .sqm 或提供重建脚本；业务层适配新 Schema（读取 metadata JSON、按 card_id 查 card_review）。

### 6.2 里程碑

| 里程碑               | 目标日期   | 状态   |
|----------------------|------------|--------|
| v1.1 设计评审通过    | 2026-02-04 | 🟡 待评审 |
| v1.1 Schema 落地     | TBD        | 待开始 |
| v1.1 索引与 CRUD 完成| TBD        | 待开始 |

---

## 7. 风险评估

### 7.1 技术风险

- **迁移风险**：v1.0 → v1.1 涉及 card 列变更与 metadata 多表→单表、新增 card_review，需明确迁移路径与回滚策略。
- **JSON 依赖**：carriers、metadata 依赖应用层 JSON 序列化；需统一格式与版本兼容策略。

### 7.2 维护风险

- **语义一致性**：Schema 与领域文档（产品模型设计）需长期对齐；变更时同步更新本文档与模型文档。

---

## 8. 附录

### 8.1 相关文档

- [卡片数据模型设计 — 基于产品视角](../../../docs/产品/myhub-卡片数据模型设计-基于产品视角.md)（本版依据）
- [MyHub 数据库模块方案设计 v1.0](./myhub-datastore-database-infra-v1.0.md)（基线）
- [MyHub 数据建模原则 v1](./myhub_数据建模原则_v1.0.md)
- [SQLDelight 官方文档](https://cashapp.github.io/sqldelight/)

### 8.2 术语表（v1.1 补充）

| 术语           | 说明                                                         |
|----------------|--------------------------------------------------------------|
| Card 壳化      | card 表仅存身份、类型、来源、载体、用户与时间，不存 content/title/review |
| CardMetadata   | 元数据组合对象（content/attribution/link/site/carrierImage/carrierVideo/code/lexicon/execution 等可选块） |
| card_review    | 复看配置独立表，按 card_id 查询；新建时由 reviewSuggestions 插入       |
| 建议卡集       | 仅创建/再分析 API 返回，不落库；已加入的卡集用 collection_card 表示    |

### 8.3 v1.0 → v1.1 对照摘要

| 对象           | v1.0                           | v1.1                                                                 |
|----------------|--------------------------------|----------------------------------------------------------------------|
| card           | id, type, title, content, created_at, updated_at | id, type, source, carriers, user_id, created_at, updated_at；无 title/content |
| card_metadata  | 多表（article/code/idea/quote/todo/word）        | 单表 card_metadata，一列 metadata TEXT（CardMetadata JSON）          |
| 复看           | 无独立表                       | card_review 表（card_id, kind, value, label, created_at, updated_at）|
| 建议卡集       | 未单独说明                     | 不落库；已加入的卡集仅 collection_card                                |
