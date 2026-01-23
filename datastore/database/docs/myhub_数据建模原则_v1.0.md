# MyHub 数据建模原则 v1

> **文档定位**：
> 本文档用于约束 MyHub 在 v0.1–v1 阶段的数据建模方式，明确哪些是“事实数据”、哪些是“用户主观数据”、哪些是“派生数据”，避免 Schema 随功能增长而失控。

---

## 一、核心设计目标

MyHub 的数据模型设计遵循以下核心目标：

1. **事实与主观彻底分离**：
    - 内容本身（Card）不承载任何用户主观状态
    - 所有“喜欢 / 复看 / 偏好”必须绑定用户

2. **最小公共模型原则**：
    - 公共表只包含对所有类型都成立的字段
    - 类型差异通过 Metadata 扩展，而非在主表堆字段

3. **阶段性最优，而非一次性完美**：
    - v0.1 优先清晰、稳定、可理解
    - 为 v2+ 的协作、智能、Schema-less 留好演进空间

---

## 二、Card：内容事实模型

### 2.1 定位

> **Card 是“内容存在的事实”，而不是“内容的含义集合”。**

它回答的问题只有：

- 这个内容存在吗？
- 它是什么类型？

### 2.2 设计原则

- Card 表中 **不包含任何用户主观状态**
- Card 表中 **不包含类型特有字段**

### 2.3 允许存在的字段

- id
- type
- title（可选）
- content（最小可展示内容）
- created_at / updated_at

### 2.4 明确禁止的字段

- is_favorite / is_read
- author / source / language（若非对所有类型成立）
- 标签、卡集、统计信息

> **一句话原则**：Card 只表达“存在”，不表达“意义”。

---

## 三、Metadata：类型语义扩展

### 3.1 定位

> **Metadata 是 Card 的“语义放大器”。**

它回答的问题是：

- 这个内容在该类型下意味着什么？

### 3.2 设计原则

- 每种 card.type 对应一个 metadata 表
- card_id = metadata 的唯一主键（1:1）
- 不同类型绝不共享语义字段

### 3.3 示例

- card_metadata_article：url / author / cover
- card_metadata_todo：status / due_at
- card_metadata_word：definition / example

### 3.4 演进预期

- v0.1–v1：结构化 metadata 表
- v2+：可演进为 schema-less（JSON payload）

---

## 四、User × Card：主观状态模型

### 4.1 定位

> **所有“我怎么看这个内容”的状态，都必须在 User × Card 中。**

### 4.2 设计原则

- user_card 是主观事实表
- 同一张 Card，不同用户状态可以完全不同

### 4.3 典型字段

- is_favorite
- last_reviewed_at
- progress（未来）

> **铁律**：Card 表中永远不允许出现主观字段。

---

## 五、Collection：结构化内容容器

### 5.1 定位

> **Collection 是“结构决策”，不是内容事实。**

### 5.2 Owner 原则（非常重要）

- 每个 Collection 必须有且仅有一个 Owner
- Owner 是数据归属与生命周期责任人

实现方式：

- collection.user_id = Owner（事实）
- user_collection.role = 权限视图

### 5.3 阶段性说明

- v0.1：Collection 是用户私有容器
- v2+：可演进为公共 Topic / 协作集合

---

## 六、User × Collection：权限模型

### 6.1 定位

> **权限是关系，不是实体属性。**

### 6.2 设计原则

- role 描述“能做什么”，而不是“是谁的”
- role 允许扩展：owner / editor / viewer

### 6.3 不变量（Invariant）

- 每个 collection 只能有一个 owner
- owner.user_id == collection.user_id

---

## 七、Tag：用户语义空间

### 7.1 定位

> **Tag 是用户私有的语义工具，而不是公共分类。**

### 7.2 设计原则

- Tag 必须是 user scope
- 不允许跨用户共享 tag 定义

### 7.3 原因

- 不同用户对同一词语理解不同
- 防止语义污染与全局冲突

---

## 八、事实表 vs 派生表

### 8.1 事实表（Source of Truth）

- user
- card
- collection
- user_card
- user_collection
- metadata

### 8.2 派生 / 缓存表

- user_statistics
- user_card_type_statistics

原则：

- 派生表 **可以删、可以重建**
- 不参与业务判断

---

## 九、模板（Template）原则

> **Template 是生成器，不是内容。**

- Template 不等于 Card
- 使用时生成新的 Card 实例

模板能力属于 v1+，不污染 v0.1 Card 结构。

---

## 十、终极一句话原则（请记住）

> **事实要稳定，主观要隔离，
> 结构要有主，语义要下沉。**

这 4 句话，是 MyHub 数据建模在所有阶段的最高约束。

