下面给你一套 **AI 捕获系统的对话状态机（State Machine）设计**。
这是把刚才的 **对话脚本变成系统逻辑** 的部分，也是工程落地时最关键的结构。

目标是让系统：

* 稳定管理对话流程
* 支持多轮补充
* 支持用户随时修改
* 避免对话混乱

---

# 一、为什么必须有 State Machine

如果没有状态机，对话系统会出现这些问题：

* AI反复问同一个问题
* 用户回答后 AI 不知道更新哪个字段
* 对话流程混乱
* 多轮修改无法处理

因此需要一个 **Conversation State Machine**。

---

# 二、整体状态机结构

完整捕获流程建议 7 个核心状态：

```
IDLE
↓
INTENT_DETECT
↓
DRAFT_CREATE
↓
INFO_COLLECT
↓
CARD_REVIEW
↓
PUBLISH_CONFIRM
↓
COMPLETE
```

完整流程：

```
User Input
   ↓
Intent Detect
   ↓
Draft Card
   ↓
Collect Missing Fields
   ↓
Review Card
   ↓
Publish
   ↓
Done
```

---

# 三、状态定义

## 1 IDLE

系统等待用户输入。

触发：

```
用户发送消息
用户上传图片
用户分享链接
```

进入状态：

```
INTENT_DETECT
```

---

## 2 INTENT_DETECT

AI识别用户意图。

识别内容：

```
Create Card
Edit Card
Add Info
Delete Info
Cancel
```

例子：

用户：

```
昨天在上海吃了一兰拉面
```

AI识别：

```
intent = create_card
```

进入状态：

```
DRAFT_CREATE
```

---

## 3 DRAFT_CREATE

AI生成卡片草稿。

AI抽取信息：

```
title
type
tag
location
summary
```

例如：

```
title: 一兰拉面
type: place
location: 上海
tag: 拉面
```

生成：

```
Draft Card
```

然后进入：

```
INFO_COLLECT
```

---

# 四、INFO_COLLECT 状态

这是 **整个系统最重要的状态**。

作用：

收集卡片缺失信息。

例如：

```
missing fields:
image
tag
note
```

系统策略：

```
一次只问一个字段
```

例如：

AI：

```
需要添加图片吗？
```

选项：

```
[上传]
[跳过]
```

---

### INFO_COLLECT 状态循环

```
INFO_COLLECT
   ↓
Ask Field
   ↓
User Response
   ↓
Update Card
   ↓
Check Missing Fields
   ↓
继续 INFO_COLLECT 或进入 REVIEW
```

流程：

```
missing_fields = detect_missing(card)

if missing_fields:
   ask next field
else:
   go REVIEW
```

---

# 五、CARD_REVIEW 状态

展示卡片给用户。

AI：

```
卡片已经准备好了
```

展示：

```
Card Preview
```

选项：

```
[发布]
[修改]
```

---

# 六、PUBLISH_CONFIRM 状态

用户点击发布。

AI确认：

```
是否发布这张卡片？
```

选项：

```
[发布]
[取消]
```

发布后：

进入

```
COMPLETE
```

---

# 七、COMPLETE 状态

AI回复：

```
卡片已保存
```

选项：

```
[查看卡片]
[继续记录]
```

然后回到：

```
IDLE
```

---

# 八、编辑状态（Edit Flow）

用户随时可以修改。

例如：

用户：

```
标题改一下
```

系统：

```
intent = edit_card
field = title
```

进入：

```
EDIT_FIELD
```

流程：

```
EDIT_FIELD
↓
Ask New Value
↓
Update Card
↓
Return REVIEW
```

---

# 九、异常状态

必须设计 **Fallback 状态**。

## 1 用户回答无法理解

例如：

AI：

```
需要添加标签吗？
```

用户：

```
随便
```

系统进入：

```
CLARIFY
```

AI：

```
我没太理解，你可以：

[选择标签]
[跳过]
```

---

## 2 用户打断流程

例如：

AI：

```
需要添加图片吗？
```

用户：

```
标题改一下
```

系统：

```
switch intent → edit_title
```

---

# 十、完整状态机图

完整逻辑：

```
           +------+
           | IDLE |
           +------+
               |
               v
       +----------------+
       | INTENT_DETECT  |
       +----------------+
               |
               v
        +-------------+
        | DRAFT_CREATE|
        +-------------+
               |
               v
        +-------------+
        | INFO_COLLECT|
        +-------------+
          |        |
          |        |
          v        v
   ask field   all fields done
          |        |
          v        v
      update     +-------------+
       card      | CARD_REVIEW |
                 +-------------+
                        |
                        v
                +----------------+
                |PUBLISH_CONFIRM |
                +----------------+
                        |
                        v
                   +---------+
                   |COMPLETE |
                   +---------+
                        |
                        v
                      IDLE
```

---

# 十一、状态数据结构

系统需要存储：

```
ConversationState
```

例如：

```
{
state: INFO_COLLECT,
card_id: 123,
missing_fields: ["image","tag"],
current_question: "image"
}
```

---

# 十二、卡片字段优先级

建议字段优先级：

```
title
type
location
image
tag
note
```

收集顺序：

```
重要 → 次要
```

---

# 十三、避免对话过长

INFO_COLLECT 设计：

```
最多 3 个问题
```

超过：

直接进入：

```
REVIEW
```

剩下字段允许用户后期编辑。

---

# 十四、系统关键函数

工程实现通常需要这些函数：

```
detect_intent()

extract_entities()

generate_draft_card()

detect_missing_fields()

ask_next_field()

update_card()

publish_card()
```

---

# 十五、最终系统结构

AI 捕获系统 =

```
Conversation Manager
+
State Machine
+
AI Understanding
+
Card Engine
```

流程：

```
User Input
↓
State Machine
↓
AI Processing
↓
Card Update
↓
Next State
```

---

# 十六、成熟产品的设计模式

大多数 AI 捕获产品会采用：

```
State Machine
+
Slot Filling
```

Slot：

```
title
image
tag
note
```

填满 slot → 发布。

---

# 十七、最佳实践总结

最稳定的 AI 捕获状态机：

```
Intent
↓
Draft
↓
Slot Filling
↓
Review
↓
Publish
```

关键原则：

- 1️⃣ **一次只问一个字段**
- 2️⃣ **用户随时可以修改**
- 3️⃣ **对话不超过5轮**
- 4️⃣ **AI尽量自动补全**

---

如果你愿意，我可以继续帮你画 **[AI捕获系统的完整产品架构图（Production Level Architecture）](AI%20捕获系统完整架构.md)**，里面会包含：

* Conversation Engine
* Card Engine
* AI Pipeline
* Prompt System
* Memory / Context

这是 **真正可以给工程团队落地的架构设计**。
