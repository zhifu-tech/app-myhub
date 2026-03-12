下面给你一套 **AI 捕获系统的 Universal Card Schema（信息结构模型）设计**。
这是 **AI Capture / Knowledge 产品最核心的数据模型**。如果这个模型设计不好，系统会很快变成：

* 信息碎片化
* 搜索困难
* AI 无法理解结构

目标是设计一个 **既通用又结构化的卡片模型**。

---

# 一、为什么需要 Universal Card Schema

AI 捕获系统的本质是：

```text
用户输入 → AI结构化 → 卡片
```

如果结构不统一：

```text
笔记
链接
想法
图片
```

都会变成 **不可搜索的碎片**。

因此需要一个 **统一的卡片结构模型**。

核心原则：

**所有信息 = Card**

---

# 二、Universal Card 的核心结构

推荐一个 **三层结构模型**：

```text
Card
│
├── Metadata
├── Content
└── Relations
```

完整结构：

```json
{
  "id": "",
  "type": "",
  "title": "",
  "summary": "",
  "content": {},
  "media": [],
  "tags": [],
  "entities": [],
  "source": {},
  "relations": [],
  "status": "",
  "created_at": "",
  "updated_at": ""
}
```

---

# 三、Card 顶层字段

核心字段建议包含 **10 个核心字段**。

| 字段        | 作用    |
|-----------|-------|
| id        | 卡片ID  |
| type      | 卡片类型  |
| title     | 标题    |
| summary   | 摘要    |
| content   | 主体内容  |
| media     | 图片/附件 |
| tags      | 标签    |
| entities  | 实体    |
| relations | 关联    |
| source    | 来源    |

---

# 四、Card Type（卡片类型）

Card 必须有 **类型**。

推荐类型：

```text
note
idea
article
place
person
event
task
bookmark
quote
```

示例：

```json
{
  "type": "place"
}
```

类型用于：

* AI理解
* UI展示
* 搜索过滤

---

# 五、Content（内容结构）

Content 用于存储主体内容。

推荐 **Block-based 结构**。

```json
{
  "content": [
    {
      "type": "text",
      "value": "昨天在上海吃了一兰拉面"
    }
  ]
}
```

支持类型：

```text
text
quote
link
code
list
image
```

这样：

* AI 更容易解析
* UI 更容易渲染

---

# 六、Media（媒体）

存储图片和附件。

```json
{
  "media": [
    {
      "type": "image",
      "url": "",
      "caption": ""
    }
  ]
}
```

支持：

```text
image
video
file
audio
```

---

# 七、Tags（标签）

标签用于 **快速分类**。

```json
{
  "tags": [
    "美食",
    "上海",
    "拉面"
  ]
}
```

特点：

* 用户可编辑
* AI推荐

---

# 八、Entities（实体）

Entities 是 **AI抽取的结构化信息**。

例如：

```json
{
  "entities": [
    {
      "type": "restaurant",
      "name": "一兰拉面"
    },
    {
      "type": "location",
      "name": "上海"
    }
  ]
}
```

用途：

* 语义搜索
* 关系图谱

---

# 九、Relations（关系）

卡片之间可以互相关联。

例如：

```json
{
  "relations": [
    {
      "type": "related",
      "card_id": "card_123"
    }
  ]
}
```

关系类型：

```text
related
reference
mention
parent
child
```

这样可以形成：

**知识网络（Knowledge Graph）**。

---

# 十、Source（来源）

来源非常重要。

```json
{
  "source": {
    "type": "link",
    "url": "https://example.com"
  }
}
```

来源类型：

```text
manual
link
image
import
ai_generated
```

---

# 十一、Metadata（元数据）

元数据用于系统管理。

```json
{
  "created_at": "",
  "updated_at": "",
  "created_by": "",
  "status": "draft"
}
```

状态：

```text
draft
published
archived
```

---

# 十二、完整 Card 示例

一个完整卡片：

```json
{
  "id": "card_123",
  "type": "place",
  "title": "一兰拉面",
  "summary": "上海的一家日式拉面店",
  "content": [
    {
      "type": "text",
      "value": "昨天在上海吃了一兰拉面"
    }
  ],
  "media": [
    {
      "type": "image",
      "url": "image_url"
    }
  ],
  "tags": [
    "美食",
    "上海",
    "拉面"
  ],
  "entities": [
    {
      "type": "restaurant",
      "name": "一兰拉面"
    }
  ],
  "relations": [],
  "source": {
    "type": "manual"
  },
  "created_at": "",
  "updated_at": ""
}
```

---

# 十三、AI 在 Schema 中的作用

AI 负责填充：

```text
title
summary
tags
entities
type
```

例如：

用户输入：

```text
昨天在上海吃了一兰拉面
```

AI生成：

```text
title → 一兰拉面
type → place
tags → 美食
entities → restaurant
```

---

# 十四、Schema 扩展机制

不同类型卡片可以 **扩展字段**。

例如：

Place Card：

```json
{
  "location": {
    "lat": "",
    "lng": ""
  }
}
```

Article Card：

```json
{
  "author": "",
  "published_at": ""
}
```

---

# 十五、Vector Embedding

每个卡片建议生成 **Embedding**。

```json
{
  "embedding": []
}
```

用于：

```text
语义搜索
相似推荐
AI问答
```

---

# 十六、搜索结构

搜索通常使用：

```text
Keyword Search
+
Vector Search
```

组合查询：

```text
tags
entities
embedding
title
content
```

---

# 十七、为什么这个 Schema 很重要

好的 Schema 能让系统：

```text
AI理解信息
关系网络
语义搜索
推荐
```

如果没有结构：

系统会变成：

```text
普通笔记
```

AI价值会很低。

---

# 十八、最佳实践总结

一个优秀的 AI Card Schema 应该：

```text
结构化
可扩展
可搜索
AI友好
```

核心模型：

```text
Card
│
├ Metadata
├ Content
├ Entities
├ Tags
└ Relations
```

---

💡 如果你愿意，我可以再给你一个 **非常关键但很多 AI 产品设计错的地方**：

**AI 捕获系统的“信息组织模型（Knowledge Organization Model）”**

也就是：

为什么很多 AI 笔记产品 **一开始很好用，但用 3 个月就乱了**。
