# AI 捕获系统的信息结构模型（Universal Card Schema） V1.0

## 1. 文档定位

- 版本：V1.0
- 类型：AI 捕获系统设计文档
- 阅读方式：先看术语注释，再阅读方案正文。

## 2. 术语注释

- Capture：将输入内容转化为结构化信息。
- Card：系统中的标准信息单元。
- Draft：发布前可编辑的草稿状态。
- Tool：由模型触发、用于执行真实系统动作的能力接口。
- State Machine：用于约束对话流程的状态控制机制。

## 3. 方案正文

## 一、设计目标

建立统一、可扩展、可检索的卡片结构，避免信息碎片化与结构不一致。

### 1. 为什么需要 Universal Card Schema

AI 捕获系统的本质是： `用户输入 → AI结构化 → 卡片`，如果结构不统一, 那么`笔记、链接、想法、图片` 都会变成 **不可搜索的碎片**。 因此需要一个 **统一的卡片结构模型**。

### 2. 核心原则：

**所有信息 = Card**

---

## 二、统一 Schema（标准口径）

```json
{
  "id": "string",
  "type": "string",
  "title": "string",
  "summary": "string",
  "content": [
    {
      "type": "text|quote|link|code|list|image",
      "value": "any"
    }
  ],
  "ui": {
    "cover": {
      "icon_key": "string|null",
      "bg_color": "string|null",
      "tint_color": "string|null",
      "image_ref": "string|null"
    }
  },
  "media": [
    {
      "type": "image|video|file|audio",
      "url": "string",
      "caption": "string"
    }
  ],
  "tags": [
    "string"
  ],
  "location": {
    "name": "string|null",
    "latitude": "number|null",
    "longitude": "number|null"
  },
  "entities": [
    {
      "type": "string",
      "name": "string"
    }
  ],
  "source": {
    "kind": "manual|link|share|import|extract",
    "ref": "string|null"
  },
  "relations": [
    {
      "type": "related|reference|mention|parent",
      "card_id": "string"
    }
  ],
  "status": "draft|published|archived",
  "created_at": "ISO-8601",
  "updated_at": "ISO-8601"
}
```

---

## 三、Card 顶层字段说明

> 回答两个问题：1. 字段是什么？2. 有什么作用？

| 字段           | 含义     | 作用                  |
|--------------|--------|---------------------|
| `id`         | 卡片唯一标识 | 用于检索、更新、关联          |
| `type`       | 卡片类型   | 决定 AI 补全策略与 UI 展示方式 |
| `title`      | 卡片标题   | 提高可读性与列表识别效率        |
| `summary`    | 卡片摘要   | 用于预览和快速理解           |
| `content`    | 主体内容   | 承载核心信息文本或结构块        |
| `ui`         | 展示信息   | 承载封面 icon/color/image 等 UI 展示配置（JSON 结构） |
| `media`      | 媒体资源   | 承载图片、视频、文件、音频       |
| `tags`       | 标签集合   | 支持快速分类、筛选与推荐        |
| `location`   | 地点对象   | 包含名称与经纬度，支持地图定位与地理维度筛选 |
| `entities`   | 实体信息   | 支持语义搜索与关系抽取         |
| `source`     | 来源信息   | 用于追溯信息出处与可信度判断      |
| `relations`  | 关联信息   | 连接相关卡片形成知识网络        |
| `status`     | 生命周期状态 | 管理草稿、发布与归档流程        |
| `created_at` | 创建时间   | 时间排序与时间视图展示         |
| `updated_at` | 更新时间   | 变更跟踪与最近编辑排序         |

---

## 四、关键字段解释与示例

### 1. `type`（卡片类型）

- 作用：告诉系统“这张卡片是什么”，用于搜索过滤和 UI 差异化展示。
- 示例：

```json
{
  "type": "place"
}
```

- 可能的类型

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

### 2. `content`（内容结构）

- 作用：存储主体信息，推荐 block-based 结构，便于 AI 解析和 UI 渲染。
- 支持类型：`text`、`quote`、`link`、`code`、`list`、`image`。
- 示例：

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

### 3. `ui`（展示结构）

- 作用：承载卡片展示信息（如封面 icon、背景色、封面图引用）。
- 存储方式：JSON 对象结构。
- 示例：

```json
{
  "ui": {
    "cover": {
      "icon_key": "psychology",
      "bg_color": "#FFFBEB",
      "tint_color": "#D97706",
      "image_ref": "media:cover_001"
    }
  }
}
```

### 4. `media`（媒体）

- 作用：承载图片和附件等多媒体信息。
- 支持类型：`image`、`video`、`file`、`audio`。
- 示例：

```json
{
  "media": [
    {
      "type": "image",
      "url": "https://example.com/a.jpg",
      "caption": "门店招牌"
    }
  ]
}
```

### 5. `tags`（标签）

- 作用：用于快速分类、筛选和推荐。
- 特点：可手动编辑，也可 AI 推荐。
- 示例：

```json
{
  "tags": [
    "美食",
    "上海",
    "拉面"
  ]
}
```

### 6. `entities`（实体）

- 作用：将正文中的关键对象结构化，支持语义搜索和关系图谱。
- 示例：

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

### 7. `relations`（关系）

- 作用：建立卡片之间的连接关系，形成知识网络。
- 常见关系类型：`related`、`reference`、`mention`、`parent`。
- 示例：

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

### 8. `source`（来源）

- 作用：追踪内容来源，判断可靠性，并支持来源维度筛选。
- 示例：

```json
{
  "source": {
    "kind": "link",
    "ref": "https://example.com"
  }
}
```

### 9. `status`（状态）

- 作用：管理卡片生命周期。
- 可选值：`draft`、`published`、`archived`。
- 示例：

```json
{
  "status": "draft"
}
```

---

## 五、扩展机制

- 在不破坏核心字段的前提下扩展类型字段。
- 新增字段需保持向后兼容。
- 统一通过 schema 校验器验证。

---

## 六、术语注释（补充）

- Schema：结构约束规则。
- Block-based Content：按内容块组织正文结构。
- Entity：从内容中抽取出的结构化对象。
- Relation：卡片之间的关系边。

## 4. 图示规范

- 图示统一使用 `text` 代码块呈现。
- 流程图默认从上到下，模块图默认从左到右。
- 节点命名使用英文技术词，描述使用中文。
