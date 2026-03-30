# 一、概述

## 1.1 背景

传统内容记录流程：

```
表单输入 → AI处理 → Review → 发布
```

存在问题：

* 输入成本高（需要用户主动填写结构化信息）
* 信息结构质量不稳定
* 用户操作步骤多

---

## 1.2 目标

将传统流程升级为：

```
对话 → 自动结构化 → 局部确认 → 发布
```

---

## 1.3 核心价值

```
降低用户输入成本 + 提高信息结构化质量
```

---

## 1.4 目标用户体验

```
用户只需要说话
AI负责整理成卡片
```

补充说明：

* “说话”包含：文本 / 语音 / 图片 / 链接等输入形式
* 用户不需要理解结构化字段

---

# 二、系统整体架构

## 2.1 五层架构模型

```
User Layer（用户层）
↓
Conversation Layer（对话层）
↓
AI Understanding Layer（AI理解层）
↓
Card Generation Layer（卡片生成层）
↓
Storage Layer（存储层）
```

---

## 2.2 数据流流程

```
用户输入
↓
对话系统
↓
意图识别（Intent Detection）
↓
结构化抽取（Entity Extraction）
↓
卡片草稿生成（Draft Generation）
↓
卡片补全（Completion）
↓
用户确认（Review）
↓
发布存储（Storage）
```

---

## 2.3 核心概念说明

* **Intent Detection（意图识别）**：判断用户当前操作目的（创建/修改/查询等）
* **Entity Extraction（实体抽取）**：从自然语言中提取结构化字段
* **Draft Card（草稿卡片）**：AI自动生成的初始结构数据
* **Completion（补全）**：通过对话补充缺失字段

---

# 三、核心数据结构（Card Schema）

> 详细的定义见[信息结构模型]()

## 3.1 卡片结构定义

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

## 3.2 字段说明（补充）

| 字段      | 说明                |
|---------|-------------------|
| type    | 卡片类型              |
| summary | AI自动生成摘要          |
| content | 原始内容或扩展内容         |
| source  | 数据来源（用户输入/网页/图片等） |
| status  | 草稿 / 已发布 / 归档     |

---

## 3.3 卡片类型

```
idea（想法）
place（地点）
article（文章）
person（人物）
note（笔记）
event（事件）
task（任务）
```

---

## 3.4 示例

```
title: 一兰拉面
type: place
location: 上海
tags: 美食
image: 1张
summary: 用户推荐的拉面店
```

---

# 四、捕获流程设计

## 4.1 捕获入口

支持多入口触发：

```
+按钮
语音输入
分享
剪藏
对话
```

示例：

```
用户：记录一下
用户：昨天吃了一家很好吃的拉面
```

---

## 4.2 意图识别（Intent Detection）

### 类型定义

```
Create Card
Edit Card
Add Info
Delete Info
Search Card
```

---

### 示例

输入：

```
昨天在上海吃了一兰拉面
```

识别结果：

```json
{
  "intent": "create",
  "type": "place"
}
```

---

# 五、卡片生成流程

## 5.1 Draft Card（草稿生成）

示例：

```
title: 一兰拉面
type: place
location: 上海
tags: 拉面
```

---

## 5.2 流程说明

1. AI生成初始卡片
2. 标记缺失字段
3. 进入补全流程

---

# 六、对话补全流程

## 6.1 补全策略

基于“缺失字段”触发提问：

```
image
tag
note
```

---

## 6.2 示例

AI：

```
需要添加一张图片吗？

[上传图片]
[跳过]
```

```
要不要加个标签？

[美食]
[餐厅]
[日本料理]
```

---

## 6.3 设计原则（补充）

* 单轮只问一个问题
* 提供可选项（降低输入成本）
* 支持跳过

---

# 七、状态机设计（核心）

## 7.1 状态定义

```
IDLE
↓
INTENT_DETECT
↓
DRAFT_CREATED
↓
COLLECT_INFO
↓
CARD_REVIEW
↓
PUBLISH
```

---

## 7.2 子状态

```
COLLECT_TITLE
COLLECT_IMAGE
COLLECT_TAG
COLLECT_LOCATION
```

---

## 7.3 状态逻辑

```
if title missing → ask title
if image missing → suggest image
if tag missing → suggest tag
```

---

## 7.4 概念说明

**State Machine（状态机）**

用于控制对话流程，使系统具备：

* 可控流程
* 可回溯
* 可中断恢复

---

# 八、AI 对话策略

## 8.1 核心原则

必须为：

```
引导式对话（Guided Interaction）
```

---

## 8.2 示例

❌ 不推荐：

```
你还要补充什么？
```

✅ 推荐：

```
需要添加照片吗？

[上传]
[跳过]
```

---

## 8.3 设计补充

* 减少开放式问题
* 优先选择题
* 降低认知负担

---

# 九、卡片实时预览（Live Preview）

## 9.1 功能描述

在对话过程中实时展示卡片变化。

---

## 9.2 界面结构

```
聊天区
────────────
AI对话
────────────

卡片预览区
────────────
标题：一兰拉面
地点：上海
标签：美食
图片：1张
```

---

## 9.3 价值

* 提升用户信心
* 降低编辑成本
* 增强可控感

---

# 十、用户编辑机制

## 10.1 编辑方式

自然语言编辑：

```
用户：标题改成 一兰拉面体验
```

---

## 10.2 解析结果

```
edit.title
```

---

## 10.3 更新机制

* 实时更新卡片
* 同步预览

---

# 十一、发布流程

## 11.1 完成提示

```
卡片准备好了 👇

[卡片预览]

是否发布？

[发布]
[继续编辑]
```

---

## 11.2 状态变化

```
Draft → Published
```

---

# 十二、对话组件设计

## 12.1 组件类型

### 1. Quick Reply

```
[上传图片]
[跳过]
```

---

### 2. Selector

```
[美食]
[餐厅]
[日本料理]
```

---

### 3. Inline Input

```
[输入框]
```

---

### 4. Upload

```
上传图片
```

---

### 5. Card Preview

实时卡片组件

---

## 12.2 设计原则（补充）

* 所有组件必须可点击
* 优先替代手动输入
* 支持移动端操作

---

# 十三、AI Prompt 设计

## 13.1 意图识别 Prompt

```json
{
  intent: "create_card",
  type: "place"
}
```

---

## 13.2 信息抽取 Prompt

```json
{
  title: "一兰拉面",
  location: "上海",
  tag: "拉面"
}
```

---

## 13.3 卡片生成 Prompt

生成字段：

```
title
summary
tag
type
```

---

## 13.4 补充说明

* Prompt需稳定输出JSON结构
* 需容错用户非标准输入

---

# 十四、系统模块设计

## 14.1 模块列表

```
Capture Engine（捕获引擎）
Conversation Manager（对话管理）
Intent Detector（意图识别）
Entity Extractor（实体抽取）
Card Generator（卡片生成）
Card Editor（卡片编辑）
Storage（存储）
```

---

## 14.2 模块流程

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

# 十五、AI能力要求

| 能力                | 作用   |
|-------------------|------|
| Intent Detection  | 识别操作 |
| Entity Extraction | 抽取信息 |
| Summarization     | 生成摘要 |
| Classification    | 分类   |
| Tag Suggestion    | 标签推荐 |

---

# 十六、效率优化策略

## 16.1 AI自动补全

目标：

```
尽量少问问题
```

---

## 16.2 默认值策略

示例：

```
时间 = 当前时间
```

---

## 16.3 推荐机制

示例：

```
标签推荐
```

---

# 十七、完整用户体验流程

```
用户：昨天吃了一兰拉面

AI：生成卡片 + 预览

AI：需要加图片吗？
用户：上传

AI：选择标签
用户：美食

AI：发布
```

---

## 17.1 目标效率

```
平均对话 < 4轮
```

---

# 十八、关键指标（KPI）

```
Capture Success Rate（捕获成功率）
Capture Time（捕获耗时）
Average Turns（平均对话轮数）
Publish Rate（发布率）
```

---

# 十九、进阶能力

## 19.1 自动捕获

```
浏览网页 → 自动生成卡片
```

---

## 19.2 多模态支持

```
图片
语音
文本
链接
```

---

## 19.3 自动分类

```
知识库
收藏
地点
```

---

# 二十、未来演进方向（AI Agent）

## 20.1 Capture Agent

用户：

```
帮我记录这个
```

AI自动完成：

```
识别 → 结构化 → 生成 → 分类
```

---

## 20.2 产品形态变化

* 从“工具” → “代理（Agent）”
* 用户操作趋近于0

---

# 二十一、总结

## 21.1 系统核心能力

```
对话驱动
AI结构化
实时预览
渐进补充
最终发布
```

---

## 21.2 核心原则

```
AI多做，用户少做
```

---

# 📌 备注（产品合理性补充）

* 当前设计符合主流 AI Native 产品 SOP（如 Notion AI / Mem / Capture类产品）
* 状态机设计是关键实现点，需优先工程化
* Prompt 稳定性将直接影响产品体验，需要重点优化

---
