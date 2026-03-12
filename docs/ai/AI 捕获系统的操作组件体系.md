--
下面给你一套 **AI 捕获系统的操作组件体系（Action Component System）完整设计**。
这是对话式 AI 产品里 **最容易被忽视，但体验差异最大的部分**。

很多团队只做 **Chat + AI 回复**，但真正好用的系统是：

**Chat + 动态操作组件（Action Components）**

这样用户 **几乎不需要打字**。

---

# 一、什么是 Action Component System

Action Component 是 **嵌入在对话中的可操作 UI 模块**。

不是普通聊天，而是：

```text
AI Message
+ Action UI
+ Structured Result
```

例如：

```text
小志：
需要添加标签吗？

[美食] [餐厅] [日本料理] [+添加]
```

用户只需要点击。

---

# 二、组件体系整体结构

建议设计 **三层组件系统**：

```text
Action System
     │
     ├── Micro Actions
     │
     ├── Form Components
     │
     └── Card Components
```

含义：

| 层级              | 用途   |
|-----------------|------|
| Micro Actions   | 快速选择 |
| Form Components | 输入数据 |
| Card Components | 结构操作 |

---

# 三、Micro Actions（最常用）

Micro Actions 是 **最轻量操作组件**。

特点：

* 单击完成
* 不需要输入
* 提高效率

---

## 1 Quick Reply

最常见组件。

例：

```text
需要添加图片吗？

[上传]
[跳过]
```

结构：

```json
{
  "type": "quick_reply",
  "options": [
    "上传图片",
    "跳过"
  ]
}
```

使用场景：

* Yes / No
* Skip
* Simple choice

---

## 2 Tag Selector

用于标签选择。

UI：

```text
推荐标签

[美食]
[餐厅]
[拉面]
[+添加]
```

结构：

```json
{
  "type": "tag_selector",
  "tags": [
    "美食",
    "餐厅",
    "拉面"
  ]
}
```

特点：

* AI 推荐
* 用户点击

---

## 3 Option Grid

多选组件。

UI：

```text
这张卡片类型是？

[地点]
[想法]
[文章]
[人物]
```

结构：

```json
{
  "type": "option_grid",
  "options": [
    "地点",
    "想法",
    "文章",
    "人物"
  ]
}
```

---

# 四、Form Components（输入组件）

用于 **补充结构化字段**。

---

## 1 Inline Input

用户输入字段。

UI：

```text
这家店叫什么？

[输入框]
```

结构：

```json
{
  "type": "input",
  "field": "title",
  "placeholder": "输入名称"
}
```

---

## 2 Image Upload

上传图片。

UI：

```text
添加一张照片

[上传图片]
```

结构：

```json
{
  "type": "upload",
  "accept": "image"
}
```

---

## 3 Location Picker

位置选择。

UI：

```text
这家店在哪里？

[选择位置]
```

结构：

```json
{
  "type": "location_picker"
}
```

---

# 五、Card Components（结构组件）

这些组件 **直接操作卡片内容**。

---

## 1 Card Preview

实时卡片。

UI：

```text
┌─────────────┐
一兰拉面
上海
标签：美食
└─────────────┘
```

结构：

```json
{
  "type": "card_preview",
  "card_id": "123"
}
```

---

## 2 Field Editor

编辑字段。

UI：

```text
标题
一兰拉面

[编辑]
```

---

## 3 Card Actions

卡片操作。

```text
[发布]
[保存草稿]
[删除]
```

结构：

```json
{
  "type": "card_actions",
  "actions": [
    "publish",
    "save",
    "delete"
  ]
}
```

---

# 六、组件插入机制（关键）

AI 在回复中 **动态插入组件**。

AI message schema：

```json
{
  "message": "需要添加标签吗？",
  "components": [
    {
      "type": "tag_selector",
      "tags": [
        "美食",
        "餐厅",
        "拉面"
      ]
    }
  ]
}
```

前端渲染：

```text
AI message
↓
render component
```

---

# 七、组件触发流程

用户点击组件：

```text
User click
↓
Event
↓
API
↓
Conversation Engine
↓
Card Update
```

例如：

```text
点击标签
↓
update_card(tag)
```

---

# 八、组件生命周期

组件存在生命周期。

状态：

```text
active
completed
expired
```

例：

```text
需要添加图片？

[上传]
[跳过]
```

用户点击：

```text
组件状态 → completed
```

变成：

```text
✔ 已跳过图片
```

---

# 九、组件渲染模型

推荐 **JSON UI Schema**。

例：

```json
{
  "message": "需要添加标签吗？",
  "components": [
    {
      "type": "tag_selector",
      "options": [
        "美食",
        "餐厅",
        "日本料理"
      ]
    },
    {
      "type": "button",
      "label": "跳过"
    }
  ]
}
```

前端：

```text
JSON → React / Vue Component
```

---

# 十、组件优先级策略

AI选择组件时有优先级。

推荐：

```text
1 Quick Reply
2 Selector
3 Upload
4 Input
```

原则：

**优先点击，不优先输入。**

---

# 十一、组件组合模式

一个 AI message 可以包含多个组件。

例：

```text
要不要补充信息？

[上传图片]

推荐标签
[美食] [餐厅] [拉面]

[直接发布]
```

---

# 十二、组件设计原则

## 1 降低输入

多点击，少输入。

---

## 2 单任务

一次只做一个操作。

---

## 3 上下文感知

组件必须和当前卡片相关。

---

## 4 自动推荐

AI必须提供默认选项。

---

# 十三、完整组件体系

完整组件库建议包含：

```text
QuickReply
OptionGrid
TagSelector
UploadImage
InputField
LocationPicker
CardPreview
CardActions
FieldEditor
ConfirmDialog
```

---

# 十四、最终系统结构

AI Capture UI =

```text
Chat
+
Action Components
+
Card Preview
```

系统运行方式：

```text
User Message
↓
AI Response
↓
Render Components
↓
User Click
↓
Card Update
```

---

# 十五、为什么这个系统很重要

如果没有 Action Component：

用户体验是：

```text
聊天
↓
不停打字
```

有 Action Component：

用户体验是：

```text
聊天
↓
点击
↓
完成记录
```

效率会 **提升 3–5 倍**。

---

💡 如果你愿意，我可以再给你一个 **非常关键的设计**：

**AI 捕获系统的 Prompt + Tool 调度架构（LLM Agent Architecture）**

这一步其实是：

为什么很多 AI 捕获产品 **很聪明但很不稳定**。
