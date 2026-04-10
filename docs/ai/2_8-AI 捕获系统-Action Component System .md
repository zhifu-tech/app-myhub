# AI 捕获系统 Action Component System PRD V1.1

---

## 1. 文档定位

* 版本：V1.1
* 类型：交互系统设计（UI + AI 协同协议）
* 所属层级：Client Layer + Conversation Layer
* 作用：

```text
将“对话”升级为“可操作交互系统”
```

---

## 2. 术语注释

| 术语                  | 定义               |
|---------------------|------------------|
| Action Component    | 嵌入在对话中的可交互 UI 模块 |
| Action Event        | 用户对组件的操作行为       |
| UI Schema           | 描述组件结构的 JSON 协议  |
| Slot Filling        | 对缺失字段的补全过程       |
| Component Lifecycle | 组件从展示到完成的状态过程    |

---

# 一、系统目标

---

## 1.1 核心目标

```text
降低输入成本
提升结构化效率
控制对话流程
```

---

## 1.2 设计本质

从：

```text
Chat
```

升级为：

```text
Chat + Action + Structured Data
```

---

## 1.3 核心模式

```text
AI Message
+ Components
+ Card State
```

---

# 二、系统架构（与整体系统对齐）

---

## 2.1 组件在系统中的位置

```text
User
 ↓
Chat UI
 ↓
Render Components
 ↓
User Action
 ↓
Event Dispatcher
 ↓
API Gateway
 ↓
Conversation Engine
 ↓
Tool Layer
 ↓
Card Engine
```

---

## 2.2 数据流（关键）

```text
LLM Response
↓
UI Schema(JSON)
↓
Frontend Render
↓
User Interaction
↓
Action Event
↓
update_card / tool_call
↓
UI 更新
```

---

# 三、组件体系结构

---

## 3.1 三层组件模型

```text
Action System
     │
     ├── Micro Actions（轻操作）
     ├── Form Components（输入）
     └── Card Components（结构）
```

---

## 3.2 设计原则

| 原则    | 说明           |
|-------|--------------|
| 优先点击  | 降低输入成本       |
| 单任务   | 每次只完成一个字段    |
| 上下文感知 | 与当前 card 强绑定 |
| AI推荐  | 必须提供默认选项     |

---

# 四、组件分类设计

---

# 4.1 Micro Actions（轻操作层）

---

## 4.1.1 Quick Reply

### 用途

* Yes / No
* Skip
* 简单选择

### UI

```text
需要添加图片吗？

[上传]
[跳过]
```

---

### Schema

```json
{
  "type": "quick_reply",
  "options": [
    {
      "label": "上传",
      "value": "upload_image"
    },
    {
      "label": "跳过",
      "value": "skip"
    }
  ]
}
```

---

## 4.1.2 Tag Selector

---

### 用途

标签推荐 + 点击填充

---

### Schema

```json
{
  "type": "tag_selector",
  "field": "tags",
  "options": [
    "美食",
    "餐厅",
    "拉面"
  ],
  "multi": true
}
```

---

## 4.1.3 Option Grid

---

### 用途

类型选择 / 分类

---

```json
{
  "type": "option_grid",
  "field": "type",
  "options": [
    "place",
    "idea",
    "article",
    "person"
  ]
}
```

---

# 4.2 Form Components（输入层）

---

## 4.2.1 Inline Input

```json
{
  "type": "input",
  "field": "title",
  "placeholder": "输入名称",
  "required": true
}
```

---

## 4.2.2 Upload

```json
{
  "type": "upload",
  "field": "media",
  "accept": [
    "image"
  ]
}
```

---

## 4.2.3 Location Picker

```json
{
  "type": "location_picker",
  "field": "location"
}
```

---

# 4.3 Card Components（结构层）

---

## 4.3.1 Card Preview

```json
{
  "type": "card_preview",
  "card_id": "card_123"
}
```

---

## 4.3.2 Field Editor

```json
{
  "type": "field_editor",
  "field": "title",
  "editable": true
}
```

---

## 4.3.3 Card Actions

```json
{
  "type": "card_actions",
  "actions": [
    {
      "type": "publish"
    },
    {
      "type": "save_draft"
    },
    {
      "type": "delete"
    }
  ]
}
```

---

# 五、AI Response 协议（核心）

---

## 5.1 标准结构

```json
{
  "message": "需要添加标签吗？",
  "components": [],
  "card_id": "string",
  "state": "INFO_COLLECT"
}
```

---

## 5.2 设计约束

```text
AI 不直接输出 UI
AI 输出 JSON Schema
前端负责渲染
```

---

# 六、组件事件模型（核心）

---

## 6.1 标准事件结构

```json
{
  "event_type": "component_action",
  "component_type": "tag_selector",
  "field": "tags",
  "value": [
    "美食"
  ],
  "card_id": "card_123"
}
```

---

## 6.2 流程

```text
User Click
↓
Event
↓
API
↓
Conversation Engine
↓
Tool Call (update_card)
↓
DB Update
↓
返回新状态
```

---

# 七、组件生命周期

---

## 7.1 生命周期状态

```text
active → completed → expired
```

---

## 7.2 示例

```text
需要添加图片？

[上传] [跳过]
```

点击后：

```text
✔ 已跳过图片
```

---

## 7.3 生命周期控制

由：

```text
State Machine + Conversation Engine
```

控制。

---

# 八、组件与状态机联动

---

## 8.1 映射关系

| 状态              | 组件                        |
|-----------------|---------------------------|
| INFO_COLLECT    | input / selector / upload |
| CARD_REVIEW     | preview + actions         |
| PUBLISH_CONFIRM | confirm dialog            |

---

## 8.2 规则

```text
一个状态 → 一组组件
```

---

# 九、组件优先级策略

---

```text
1 Quick Reply
2 Selector
3 Upload
4 Input
```

---

## 原则

```text
点击优先 > 输入
```

---

# 十、组件组合模式

---

## 示例

```json
{
  "message": "要不要补充信息？",
  "components": [
    {
      "type": "upload"
    },
    {
      "type": "tag_selector"
    },
    {
      "type": "card_actions"
    }
  ]
}
```

---

# 十一、与 Tool 系统对齐

---

## 映射关系

| 组件          | Tool                        |
|-------------|-----------------------------|
| TagSelector | update_card(tags)           |
| Upload      | analyze_image + update_card |
| Input       | update_card(field)          |
| Publish     | publish_card                |

---

# 十二、前端渲染规范

---

## 12.1 渲染方式

```text
JSON Schema → Component Renderer（React/Vue）
```

---

## 12.2 建议架构

```text
Component Registry
↓
Dynamic Renderer
↓
Event Dispatcher
```

---

# 十三、异常处理

---

## 13.1 无效输入

```text
进入 CLARIFY 状态
```

---

## 13.2 组件失效

```text
expired → 不可点击
```

---

## 13.3 状态冲突

```text
以 State Machine 为准
```

---

# 十四、完整交互流程

```text
AI Message
↓
Render Components
↓
User Click
↓
Event
↓
Tool Call
↓
Card Update
↓
New UI
```

---

# 十五、系统价值

---

## 无组件

```text
聊天 → 输入 → 低效
```

---

## 有组件

```text
聊天 → 点击 → 完成结构化
```

---

## 效率提升

```text
3–5 倍
```

---

# 十六、最终结论

Action Component System 是：

```text
对话系统的执行层
+
结构化数据入口
+
AI能力的交互桥梁
```

---

## 系统三大支柱（最终统一）

```text
State Machine → 控流程
LLM Agent → 做决策
Action Components → 执行交互
```

---

# 十七、V1.2 组件统一设计（当前实现建议）

## 17.1 统一渲染模型

组件统一采用：

```text
Action Card（容器）
 + Title（当前步骤）
 + Options（按钮/标签）
```

设计规则：

```text
Primary Action: Filled Button
Secondary Action: Outlined Button
Selector Action: Chip
Input Hint: 仅提示，不重复输入框
```

## 17.2 状态到组件映射（收敛版）

```text
INFO_COLLECT:
  if missing=media -> upload + quick_reply(skip_media/review)
  if missing=tags  -> tag_selector + quick_reply(skip_tags/review)
  if missing=title -> input_hint + quick_reply(review)

CARD_REVIEW:
  card_actions(edit_title/publish)

MANUAL_EDIT:
  input_hint(title) + card_actions(edit_title/publish)

COMPLETE:
  quick_reply(new_capture)
```

## 17.3 生命周期

```text
active -> completed -> expired
```

示例：

```text
点击“跳过图片”后，media 组件 completed，不再渲染；
下一缺失字段组件自动接管（如 tags）。
```

## 17.4 事件协议（建议补齐）

```json
{
  "event_type": "component_action",
  "component_type": "upload|tag_selector|quick_reply|card_actions",
  "field": "media|tags|title|null",
  "value": "action_value",
  "session_id": "session_xxx"
}
```

## 17.5 缺失字段策略

```text
missing_fields 必须是有序列表：media -> tags -> title
一次只驱动一个主任务，避免同屏多任务认知负担。
```
