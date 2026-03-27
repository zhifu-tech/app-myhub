# AI 捕获系统产品设计方案 V1.0

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

## 一、系统目标

将传统流程：

`表单输入 -> AI处理 -> Review -> 发布`

升级为：

`对话 -> 自动结构化 -> 局部确认 -> 发布`

核心价值：

- 降低输入成本
- 提高结构化质量
- 保证可持续组织与检索

---

## 二、整体架构

推荐五层：

`User Layer -> Conversation Layer -> AI Understanding Layer -> Card Generation Layer -> Storage Layer`

核心链路：

`用户输入 -> 意图识别 -> 结构化抽取 -> 草稿生成 -> 信息补全 -> 预览确认 -> 发布`

---

## 三、核心数据结构（Card Schema）

```json
{
  "id": "string",
  "type": "string",
  "title": "string",
  "summary": "string",
  "content": {},
  "media": [],
  "tags": [],
  "location": "string|null",
  "source": {
    "kind": "manual|link|share|import|extract",
    "ref": "string|null"
  },
  "status": "draft|published|archived",
  "entities": [],
  "relations": [],
  "created_at": "ISO-8601",
  "updated_at": "ISO-8601"
}
```

> 各字段的解释见：[AI捕获系统的信息结构模型（Universal Card Schema）V1.0.md](AI%E6%8D%95%E8%8E%B7%E7%B3%BB%E7%BB%9F%E7%9A%84%E4%BF%A1%E6%81%AF%E7%BB%93%E6%9E%84%E6%A8%A1%E5%9E%8B%EF%BC%88Universal%20Card%20Schema%EF%BC%89V1.0.md)
---

## 四、捕获流程设计

### 1. 捕获入口

- 文本输入
- 图片上传
- 链接分享
- 对话触发

例如：

```
用户：记录一下
```

或

```
用户：昨天吃了一家很好吃的拉面
```

### 2. 意图识别

- Create Card
- Edit Card
- Add Info
- Delete Info
- Search Card

例如：

```
昨天在上海吃了一兰拉面
```

识别：

```
intent: create
type: place
```

### 3. 卡片生成流程

核心是： **Draft Card**，AI先生成卡片草稿。

例如：

```
Draft Card

title: 一兰拉面
type: place
location: 上海
tags: 拉面
```

然后进入 **补全流程**。

---

### 4. 对话补全流程

AI根据 **缺失字段**提问。

例如：

缺字段：

```
image
tag
note
```

AI：

```
需要添加一张图片吗？

[上传图片]
[跳过]
```

然后：

```
要不要加个标签？

[美食]
[餐厅]
[日本料理]
```

> - 一次只问一个字段
> - 优先操作组件，减少自由输入

---

## 五、状态机设计

捕获流程是一个 **State Machine**。

推荐状态：

`IDLE -> INTENT_DETECT -> DRAFT_CREATE -> INFO_COLLECT -> CARD_REVIEW -> COMPLETE`

关键点：

- 任意阶段可修改草稿
- 异常可恢复
- 发布前强制 review

状态示例：

```
COLLECT_TITLE
COLLECT_IMAGE
COLLECT_TAG
COLLECT_LOCATION
```

状态逻辑：

```
if title missing → ask title
if image missing → suggest image
if tag missing → suggest tag
```

## 六、AI对话策略

AI对话必须：**引导式**，而不是开放式。

坏例子：

```
你还要补充什么？
```

好例子：

```
需要添加照片吗？

[上传]
[跳过]
```

## 七、卡片实时预览

设计 **Live Card Preview**

界面：

```
聊天区
────────────

AI：帮你生成了一张卡片

用户：...

AI：需要添加图片吗？

[上传]

────────────

卡片预览
────────────

标题：一兰拉面
地点：上海
标签：美食
图片：1张
```

用户看到 **实时变化**。

---

## 八、用户编辑方式

用户可以随时修改。

例如：

```
用户：标题改成 一兰拉面体验
```

AI识别：

```
edit.title
```

更新卡片。

---

## 九、发布流程

完成后：

AI：

```
卡片准备好了 👇

[卡片预览]

是否发布？

[发布]
[继续编辑]
```

## 六、对话与组件策略

对话中必须嵌入 **Action Components**

组件体系：

### 1. Quick Reply

```
[上传图片]
[跳过]
```

---

### 2. Selector

```
选择标签

[美食]
[餐厅]
[日本料理]
```

---

### 3. Inline Input

```
输入标题

[输入框]
```

---

### 4. Upload

```
上传图片
```

---

### 5. Card Preview

实时卡片。

---

设计原则：

1. AI 先完成 80% 自动抽取
2. 用户只做必要确认
3. 对话轮次控制在 2-5 轮

---

## 七、Prompt 设计

三层 Prompt：

- System Prompt：角色和规则
- Task Prompt：当前上下文
- Tool Prompt：可调用能力与参数约束

核心规则：

- 不编造数据
- 修改必须通过工具执行


AI需要三个 Prompt：

### 1 意图识别 Prompt

```
User Input:
昨天在上海吃了一兰拉面

Output JSON:

{
intent: create_card
type: place
}
```

---

### 2 信息抽取 Prompt

```
Extract structured information.

Output:

{
title: 一兰拉面
location: 上海
tag: 拉面
}
```

---

## 八、模块组成

- Conversation Engine
- Card Engine
- Prompt System
- Tool Layer
- Storage Layer
- Knowledge Organization

---

## 九、关键指标

- 捕获完成率
- 平均轮次
- 发布率
- 编辑返工率
- 检索命中率

---

## 十、术语注释

- Draft Card：尚未发布的卡片草稿。
- Slot Filling：按字段缺失逐步补齐。
- Review：发布前统一确认阶段。

## 4. 图示规范

- 图示统一使用 `text` 代码块呈现。
- 流程图默认从上到下，模块图默认从左到右。
- 节点命名使用英文技术词，描述使用中文。
