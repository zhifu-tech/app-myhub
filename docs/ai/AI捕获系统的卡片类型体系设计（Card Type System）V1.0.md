# AI捕获系统的卡片类型体系设计（Card Type System）V1.0

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

这是整个 AI 捕获产品最核心的模型之一，因为 **卡片类型决定 UI 展示、字段结构、AI 填充逻辑和搜索组织方式**。
如果类型设计不好，系统会出现：

* 卡片字段不统一
* 搜索与推荐失效
* 用户混乱，不知道信息放哪

---

# 一、设计原则

1️⃣ **最小可复用单元**：所有信息都归为“卡片”，每个卡片类型可以共享部分字段。
2️⃣ **字段可扩展**：支持基础字段 + 类型特定字段。
3️⃣ **兼容 AI 自动填充**：每种类型对应 AI 提示模板和必填字段。
4️⃣ **支持多维度组织**：不同类型卡片可以组成集合和关系网络。

---

# 二、卡片类型层级

卡片类型可以分 **三层结构**：

```text
Root Type (通用) → Subtype (细分) → Field Schema (字段结构)
```

---

### 1. Root Type（顶层类型）

建议 5–6 个顶层类型：

| 类型      | 作用      | 示例             |
|---------|---------|----------------|
| Note    | 通用笔记、记录 | “会议纪要”、“想法”    |
| Idea    | 创意或灵感   | “新产品创意”、“旅行计划” |
| Entity  | 实体类信息   | 人物、地点、机构       |
| Content | 内容类信息   | 文章、网页、书籍、视频    |
| Event   | 事件      | 会议、约会、旅行活动     |
| Task    | 任务      | 待办事项、提醒        |

---

### 2. Subtype（细分类）

每个顶层类型可进一步细分：

| Root Type | Subtype      | 说明     |
|-----------|--------------|--------|
| Note      | General      | 通用文本笔记 |
| Note      | Diary        | 日志类    |
| Idea      | Product      | 产品创意   |
| Idea      | Travel       | 旅行灵感   |
| Entity    | Person       | 人物     |
| Entity    | Place        | 地点     |
| Entity    | Organization | 公司/机构  |
| Content   | Article      | 文章     |
| Content   | Book         | 书籍     |
| Content   | Video        | 视频     |
| Event     | Meeting      | 会议     |
| Event     | Trip         | 旅行     |
| Task      | Todo         | 待办     |
| Task      | Deadline     | 时间敏感任务 |

---

### 3. Field Schema（字段结构）

每个卡片类型对应一个 **字段集合**：

| 字段类别   | 示例                                                                              |
|--------|---------------------------------------------------------------------------------|
| 必填基础字段 | title, type, created_at, created_by                                             |
| 可选基础字段 | summary, tags, entities, media                                                  |
| 类型特定字段 | Entity: birthday, Organization: location, Event: start_time, end_time, Location |
| AI填充字段 | AI_summary, AI_tags, AI_entities                                                |

---

# 三、卡片类型示例

### 示例 1：地点卡片（Place）

```json
{
  "id": "card_001",
  "type": "Entity",
  "subtype": "Place",
  "title": "一兰拉面",
  "location": "上海",
  "tags": [
    "美食",
    "拉面"
  ],
  "entities": [
    "Restaurant"
  ],
  "media": [
    {
      "type": "image",
      "url": "..."
    }
  ],
  "created_at": "2026-03-12T10:00:00Z",
  "source": "manual"
}
```

---

### 示例 2：文章卡片（Article）

```json
{
  "id": "card_002",
  "type": "Content",
  "subtype": "Article",
  "title": "AI 对话系统设计最佳实践",
  "summary": "本文介绍了对话式 AI 捕获系统的设计方法",
  "tags": [
    "AI",
    "Product",
    "Knowledge Management"
  ],
  "entities": [
    "AI Agent",
    "State Machine"
  ],
  "media": [
    {
      "type": "link",
      "url": "https://example.com"
    }
  ],
  "created_at": "2026-03-12T11:00:00Z",
  "source": "link"
}
```

---

### 示例 3：任务卡片（Task）

```json
{
  "id": "card_003",
  "type": "Task",
  "subtype": "Todo",
  "title": "提交产品原型",
  "due_date": "2026-03-15",
  "tags": [
    "Product",
    "Deadline"
  ],
  "status": "in_progress",
  "created_at": "2026-03-12T12:00:00Z",
  "source": "manual"
}
```

---

# 四、AI 捕获与类型映射

AI 可以根据用户输入自动判断卡片类型：

| 用户输入            | AI 类型判断           | 字段自动填充                          |
|-----------------|-------------------|---------------------------------|
| “昨天在上海吃了一兰拉面”   | Entity → Place    | title, location, tags, entities |
| “发现新产品创意：智能手表”  | Idea → Product    | title, summary, tags            |
| “阅读文章《AI 捕获系统》” | Content → Article | title, summary, tags, source    |

---

# 五、UI 与类型映射

不同卡片类型在 UI 上可做定制：

| 卡片类型    | UI特点                |
|---------|---------------------|
| Note    | 通用文本显示，带可编辑 summary |
| Idea    | 显示灵感标签和图片预览         |
| Entity  | 显示核心实体信息和属性         |
| Content | 显示文章/书籍链接预览         |
| Event   | 显示时间、地点和参与人         |
| Task    | 显示状态、截止日期、提醒按钮      |

---

# 六、类型体系扩展机制

1️⃣ **可新增 subtype**：系统允许管理员或 AI 根据使用场景新增 subtype
2️⃣ **可拓展字段**：每种类型的字段可通过 schema 更新增加新字段
3️⃣ **可配置 AI 提示模板**：不同类型有不同 AI 捕获 prompt 模板

---

# 七、推荐类型层级结构图

```text
Root Type
├─ Note
│  ├─ General
│  └─ Diary
├─ Idea
│  ├─ Product
│  └─ Travel
├─ Entity
│  ├─ Person
│  ├─ Place
│  └─ Organization
├─ Content
│  ├─ Article
│  ├─ Book
│  └─ Video
├─ Event
│  ├─ Meeting
│  └─ Trip
└─ Task
   ├─ Todo
   └─ Deadline
```

---

# 八、核心优势

* **统一字段**：便于 AI 自动填充
* **可扩展**：支持新类型与字段
* **UI 定制**：不同类型展示不同组件
* **搜索优化**：按类型、标签、字段维度搜索
* **AI 友好**：类型映射 + prompt 模板驱动

---


* Conversation Engine
* Action Component System
* LLM Agent + Tool Layer
* Universal Card Schema
* Knowledge Organization Model
* Card Type System

这样可以给团队做落地开发参考。


## 4. 图示规范
- 图示统一使用 `text` 代码块呈现。
- 流程图默认从上到下，模块图默认从左到右。
- 节点命名使用英文技术词，描述使用中文。
