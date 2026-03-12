下面给你一套 **完整的 AI 捕获卡片系统产品设计方案（AI Conversational Capture System）**。
这套方案是目前很多 AI 产品（知识管理、笔记、信息捕获、收藏类产品）正在采用的 **对话式结构化生产模型**。

我会从 **产品架构 → 交互设计 → AI系统 → 数据结构 → 状态机 → Prompt → UI组件** 全部讲清楚。

---

# 一、系统目标

把传统：

```
表单输入 → AI处理 → Review → 发布
```

变成：

```
对话 → 自动结构化 → 局部确认 → 发布
```

核心价值：

`**降低用户输入成本 + 提高信息结构化质量**`

目标体验：

```
用户只需要说话
AI负责整理成卡片
```

---

# 二、整体系统架构

AI捕获系统可以拆成 **5层架构**

```
User Layer
↓
Conversation Layer
↓
AI Understanding Layer
↓
Card Generation Layer
↓
Storage Layer
```

详细结构：

```
用户输入
   ↓
对话系统
   ↓
意图识别
   ↓
结构化抽取
   ↓
卡片草稿生成
   ↓
卡片补全
   ↓
用户确认
   ↓
发布存储
```

---

# 三、核心数据结构（Card Schema）

卡片必须 **结构化设计**。

例如：

```
Card
{
 id
 type
 title
 summary
 content
 images[]
 tags[]
 location
 created_time
 source
 status
}
```

卡片类型：

```
idea
place
article
person
note
event
task
```

例如：

```
Card

title: 一兰拉面
type: place
location: 上海
tags: 美食
image: 1张
summary: 用户推荐的拉面店
```

---

# 四、捕获流程设计

## 1 捕获入口

入口可以很多：

```
+按钮
语音输入
分享
剪藏
对话
```

例如：

```
用户：记录一下
```

或

```
用户：昨天吃了一家很好吃的拉面
```

---

## 2 AI意图识别

AI首先识别：

```
Intent
```

类型：

```
Create Card
Edit Card
Add Info
Delete Info
Search Card
```

例如：

```
昨天在上海吃了一兰拉面
```

识别：

```
intent: create
type: place
```

---

# 五、卡片生成流程

核心是：

**Draft Card**

AI先生成卡片草稿。

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

# 六、对话补全流程

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

---

# 七、状态机设计（核心）

捕获流程是一个 **State Machine**

状态：

```
START
↓
INTENT_DETECT
↓
DRAFT_CREATED
↓
COLLECT_INFO
↓
CARD_REVIEW
↓
PUBLISH[AI 捕获对话脚本.md](AI%20%E6%8D%95%E8%8E%B7%E5%AF%B9%E8%AF%9D%E8%84%9A%E6%9C%AC.md)
```

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

---

# 八、AI对话策略

AI对话必须：

**引导式**

而不是开放式。

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

---

# 九、卡片实时预览

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

# 十、用户编辑方式

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

# 十一、发布流程

完成后：

AI：

```
卡片准备好了 👇

[卡片预览]

是否发布？

[发布]
[继续编辑]
```

---

# 十二、对话组件设计

对话中必须嵌入 **Action Components**

组件类型：

### 1 Quick Reply

```
[上传图片]
[跳过]
```

---

### 2 Selector

```
选择标签

[美食]
[餐厅]
[日本料理]
```

---

### 3 Inline Input

```
输入标题

[输入框]
```

---

### 4 Upload

```
上传图片
```

---

### 5 Card Preview

实时卡片。

---

# 十三、AI Prompt 设计

AI需要三个 Prompt：

---

## 1 意图识别 Prompt

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

## 2 信息抽取 Prompt

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

## 3 卡片生成 Prompt

```
Generate a card draft.

Fields:

title
summary
tag
type
```

---

# 十四、系统模块

完整系统模块：

```
Capture Engine
Conversation Manager
Intent Detector
Entity Extractor
Card Generator
Card Editor
Storage
```

架构：

```
User Input
↓
Intent Detector
↓
Entity Extractor
↓
Card Draft Generator
↓
Conversation Manager
↓
Card Editor
↓
Storage
```

---

# 十五、AI能力

需要的AI能力：

| 能力                | 作用   |
|-------------------|------|
| Intent Detection  | 识别操作 |
| Entity Extraction | 抽取信息 |
| Summarization     | 生成摘要 |
| Classification    | 卡片分类 |
| Tag Suggestion    | 推荐标签 |

---

# 十六、效率优化

三个关键：

### 1 AI自动补全

尽量 **少问问题**

---

### 2 默认值

例如：

```
时间 = 当前
```

---

### 3 推荐选项

例如：

```
标签推荐
```

---

# 十七、完整用户体验

理想流程：

```
用户：
昨天吃了一兰拉面

AI：
我帮你记录了一张卡片 👇

[卡片预览]

需要加一张图片吗？

[上传]
[跳过]

用户：上传图片

AI：
要不要添加标签？

[美食]
[餐厅]

用户：美食

AI：
卡片已经准备好了

[发布]
```

整个流程：

**3轮对话**

---

# 十八、关键指标

产品上线后重点关注：

```
Capture Success Rate
Capture Time
Average Turns
Publish Rate
```

理想：

```
平均对话 < 4轮
```

---

# 十九、进阶能力

高级系统可以加入：

### 自动捕获

例如：

```
浏览网页
AI自动生成卡片
```

---

### 多模态

支持：

```
图片
语音
文本
链接
```

---

### 自动分类

AI自动归档：

```
知识库
收藏
地点
```

---

# 二十、未来进化（AI Agent）

最终系统会进化成：

**Capture Agent**

用户：

```
帮我记录这个
```

AI自动：

```
识别
整理
生成卡片
分类
```

用户几乎不用操作。

---

# 总结

完整 AI 捕获卡片系统：

```
对话驱动
AI结构化
实时预览
渐进补充
最终发布
```

核心原则：

**AI多做，用户少做。**

---

如果你愿意，我可以继续帮你设计 **三件非常关键的东西**：

+ 1️⃣ [AI捕获对话脚本（完整 conversation design）](AI%20捕获对话脚本.md)
+ 2️⃣ [对话状态机（State Machine 图）](AI%20捕获系统的对话状态机.md)
+ 3️⃣ **完整 UI 线框图（Chat + Card Layout）**

这三部分是 **真正决定产品好不好用的核心设计**。
