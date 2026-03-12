下面给你一套 **AI 捕获系统的 Prompt + Tool 调度架构（LLM Agent Architecture）**。
这是 **Production 级 AI 产品最核心的一层**：让 LLM 不只是聊天，而是 **可控地调用系统能力（Tools）来完成卡片生成与编辑**。

目标：

* 让 AI **稳定执行捕获任务**
* 避免 **幻觉修改数据**
* 让 AI **调用真实系统 API**

核心思想：

**LLM 负责决策，Tools 负责执行。**

---

# 一、整体架构（LLM Agent Architecture）

AI 捕获系统建议使用 **Tool-based Agent Architecture**。

整体结构：

```text
User Input
    ↓
Prompt System
    ↓
LLM Agent
    ↓
Tool Planner
    ↓
Tool Execution
    ↓
Card Engine
    ↓
Response Builder
    ↓
UI Response
```

完整架构：

```text
User
 │
 ▼
Conversation Engine
 │
 ▼
LLM Agent
 │
 ├── Intent Reasoning
 │
 ├── Tool Selection
 │
 └── Response Generation
 │
 ▼
Tool Layer
 │
 ├── create_card
 ├── update_card
 ├── suggest_tags
 ├── analyze_content
 └── publish_card
 │
 ▼
Card Engine
 │
 ▼
Database
```

---

# 二、Prompt 系统结构

AI Agent 的 Prompt 不能只有一个，而应该 **分层设计**。

推荐 **三层 Prompt**：

```text
System Prompt
Task Prompt
Tool Prompt
```

---

# 三、System Prompt（系统规则）

System Prompt 定义 **AI 的角色和行为规则**。

示例：

```text
You are XiaoZhi, an AI assistant that helps users capture
information and organize it into structured cards.

Your job is to:
1. Understand user input
2. Extract structured information
3. Create or update cards
4. Ask for missing information
5. Suggest tags or fields

Never invent data.
Always use tools when modifying cards.
```

关键规则：

```text
AI 不直接修改数据
AI 必须调用 tool
```

---

# 四、Task Prompt（任务上下文）

Task Prompt 包含当前对话信息。

例如：

```json
{
  "conversation_state": "INFO_COLLECT",
  "current_card": {
    "title": "一兰拉面",
    "location": "上海",
    "tags": [
      "拉面"
    ]
  },
  "missing_fields": [
    "image",
    "note"
  ]
}
```

LLM 根据这些信息决定：

```text
ask image
ask note
suggest tags
```

---

# 五、Tool Prompt（工具说明）

Tool Prompt 描述 **AI可以调用的能力**。

例如：

```json
{
  "tools": [
    {
      "name": "create_card",
      "description": "Create a new card",
      "parameters": {
        "title": "string",
        "type": "string",
        "content": "string"
      }
    },
    {
      "name": "update_card",
      "description": "Update card fields",
      "parameters": {
        "card_id": "string",
        "field": "string",
        "value": "string"
      }
    }
  ]
}
```

LLM 可以返回：

```json
{
  "tool_call": "update_card",
  "arguments": {
    "card_id": "123",
    "field": "tag",
    "value": "美食"
  }
}
```

---

# 六、Tool Layer（工具层）

Tool Layer 是 **AI可以调用的系统能力**。

AI 捕获系统至少需要 8 个工具。

---

## 1 create_card

创建卡片。

```json
{
  "name": "create_card",
  "params": {
    "title": "string",
    "type": "string"
  }
}
```

---

## 2 update_card

更新卡片字段。

```json
{
  "name": "update_card",
  "params": {
    "card_id": "string",
    "field": "string",
    "value": "any"
  }
}
```

---

## 3 suggest_tags

AI 推荐标签。

```json
{
  "name": "suggest_tags",
  "params": {
    "text": "string"
  }
}
```

---

## 4 analyze_image

分析图片。

```json
{
  "name": "analyze_image",
  "params": {
    "image_url": "string"
  }
}
```

---

## 5 summarize_content

总结内容。

```json
{
  "name": "summarize_content",
  "params": {
    "text": "string"
  }
}
```

---

## 6 extract_entities

抽取实体。

```json
{
  "name": "extract_entities",
  "params": {
    "text": "string"
  }
}
```

---

## 7 publish_card

发布卡片。

```json
{
  "name": "publish_card",
  "params": {
    "card_id": "string"
  }
}
```

---

## 8 search_cards

查找卡片。

```json
{
  "name": "search_cards",
  "params": {
    "query": "string"
  }
}
```

---

# 七、Agent 决策流程

LLM Agent 每次响应会执行：

```text
Reason → Decide → Act
```

流程：

```text
User Input
     ↓
LLM Reasoning
     ↓
Choose Tool
     ↓
Execute Tool
     ↓
Update Context
     ↓
Generate Response
```

示例：

用户：

```text
昨天在上海吃了一兰拉面
```

LLM：

```text
识别 intent: create_card
```

调用：

```json
create_card
```

---

# 八、工具调用示例

LLM输出：

```json
{
  "tool_call": "create_card",
  "arguments": {
    "title": "一兰拉面",
    "type": "restaurant"
  }
}
```

系统执行：

```text
Card Engine
↓
DB
```

返回：

```json
{
  "card_id": "card_123"
}
```

---

# 九、工具链（Tool Chain）

有些任务需要多个工具。

例如：

用户：

```text
收藏这个网页
```

执行链：

```text
analyze_link
↓
extract_entities
↓
summarize_content
↓
create_card
```

---

# 十、上下文管理（Memory）

Agent 需要 **对话记忆**。

推荐保存：

```json
{
  "conversation_id": "",
  "state": "",
  "card_id": "",
  "last_action": "",
  "missing_fields": []
}
```

存储：

```text
Redis
```

---

# 十一、Response Builder

最后系统生成 UI 响应。

例如：

```json
{
  "message": "需要添加标签吗？",
  "components": [
    {
      "type": "tag_selector",
      "options": [
        "美食",
        "餐厅",
        "拉面"
      ]
    }
  ]
}
```

---

# 十二、完整执行流程

完整一次捕获：

```text
User Input
↓
LLM Agent
↓
Tool: extract_entities
↓
Tool: create_card
↓
Slot Manager
↓
Ask Missing Field
↓
User Response
↓
Tool: update_card
↓
Publish
```

---

# 十三、稳定性关键策略

为了避免 AI 失控：

### 1 强制 Tool 调用

```text
修改数据必须调用 tool
```

---

### 2 JSON 输出

限制 AI 输出：

```json
{
  "tool_call": "",
  "arguments": {}
}
```

---

### 3 状态机约束

AI 不能随意跳状态。

---

# 十四、成熟 AI 产品常用架构

成熟系统一般是：

```text
LLM Agent
+
Tool Router
+
State Machine
+
Business Engine
```

在你的系统里：

```text
Agent → 决策
Tool → 执行
State Machine → 控制流程
Card Engine → 业务逻辑
```

---

# 十五、最终 Production 架构

完整结构：

```text
User
 │
 ▼
UI (Chat + Card)
 │
 ▼
Conversation Engine
 │
 ▼
LLM Agent
 │
 ▼
Tool Layer
 │
 ▼
Card Engine
 │
 ▼
Storage
```

---

# 十六、关键设计原则

优秀 AI 捕获系统必须：

```text
LLM 只负责思考
Tools 负责执行
State Machine 负责流程
```

这样系统才会：

* 稳定
* 可扩展
* 可调试

---

如果你愿意，我可以再帮你设计一个 **非常关键、很多团队最后才意识到的问题**：

**AI 捕获系统的“信息结构模型（Universal Card Schema）”**

也就是：

为什么很多 AI 记录系统 **越用越乱、越来越难搜索**。
