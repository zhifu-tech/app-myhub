# 一、文档信息

## 1.1 基本信息

* 版本：V1.0
* 类型：系统架构设计文档（Architecture PRD）
* 适用对象：产品经理 / 架构师 / 后端 / AI工程 / 前端

---

## 1.2 阅读指引

推荐阅读顺序：

1. 术语注释（统一理解系统概念）
2. 端到端架构（整体认知）
3. 流程说明（理解数据流）
4. 模块功能（职责划分）
5. 设计亮点（核心差异）
6. 落地建议（工程实现）

---

# 二、术语定义（统一语义层）

| 术语               | 定义                       |
|------------------|--------------------------|
| Capture          | 将用户输入转化为结构化信息的过程         |
| Card             | 系统中的标准信息单元（结构化数据载体）      |
| Draft            | 卡片的草稿状态（未发布、可编辑）         |
| Tool             | 模型触发的系统能力接口（具备副作用，如写数据库） |
| State Machine    | 控制对话流程的状态机制              |
| Slot             | 卡片中的字段占位（如 title、tag）    |
| Action Component | 对话中的可交互 UI 元素（按钮、选择器等）   |
| LLM Agent        | 基于大模型的决策与工具调用执行体         |

---

# 三、系统目标（架构视角）

## 3.1 核心目标

构建一个：

```text
对话驱动 + AI结构化 + Tool可控执行 + 知识可组织
```

的端到端捕获系统。

---

## 3.2 架构核心原则

* **AI负责理解，不直接写数据**
* **所有副作用必须通过 Tool 执行**
* **结构统一（Schema First）**
* **状态可控（State Machine）**
* **数据可组织（Knowledge Layer）**

---

# 四、端到端架构蓝图

## 4.1 系统架构图（标准版）

```text
[User Layer 用户端]
    │
    ▼
[Chat + Action UI]
（对话 + 实时卡片 + 交互组件）
    │
    ▼
[API Gateway]
（鉴权 / 限流 / 日志）
    │
    ▼
[Conversation Engine]
（状态机 / 上下文 / Slot管理 / 行动规划）
    │
    ▼
[LLM Agent Layer]
（Prompt系统 / 推理决策 / Tool选择）
    │
    ▼
[Tool Layer]
（系统能力接口执行层）
    │
    ▼
[Card Engine]
（业务逻辑 / Schema校验 / 数据规范）
    │
    ▼
[Storage Layer]
（数据库 / 向量库 / 文件存储）
    │
    ▼
[Knowledge Organization]
（知识组织 / 图谱 / 多维视图）
```

---

## 4.2 分层说明（强化理解）

### 1️⃣ UI Layer（交互层）

负责：

* 用户输入（文本 / 图片 / 语音）
* 对话展示
* 实时卡片预览（Live Preview）
* Action Components（按钮/选择器）

---

### 2️⃣ API Gateway（接入层）

负责：

* 鉴权（Auth）
* 限流（Rate Limit）
* 日志记录（Logging）

---

### 3️⃣ Conversation Engine（对话控制层）

核心调度层，包含：

| 子模块             | 作用      |
|-----------------|---------|
| State Machine   | 控制流程阶段  |
| Context Manager | 管理上下文   |
| Slot Manager    | 管理字段缺失  |
| Action Planner  | 决策下一步动作 |

---

### 4️⃣ LLM Agent Layer（AI决策层）

负责：

* 理解用户意图
* 推理当前状态
* 选择 Tool
* 输出结构化决策

组成：

```text
Prompt System
- System Prompt（全局规则）
- Task Prompt（任务定义）
- Tool Prompt（工具描述）

Tool Planner（工具选择）
Reasoning（推理过程）
```

---

### 5️⃣ Tool Layer（能力执行层）

所有“写操作”必须通过 Tool：

```text
create_card
update_card
suggest_tags
analyze_content
analyze_image
summarize_content
extract_entities
publish_card
search_cards
```

---

### 6️⃣ Card Engine（业务核心层）

职责：

* Card CRUD
* Schema校验（Schema Enforcement）
* 字段格式化（Formatter）
* 数据一致性保证

---

### 7️⃣ Storage Layer（存储层）

结构：

```text
Primary DB（结构化数据）
Vector DB（Embedding/语义搜索）
File Storage（图片/附件）
```

---

### 8️⃣ Knowledge Organization（知识组织层）

核心能力：

| 模块                      | 说明     |
|-------------------------|--------|
| Universal Card Schema   | 统一数据结构 |
| Card Type System        | 类型驱动UI |
| Knowledge Graph         | 卡片关系网络 |
| Smart Collections       | 动态集合   |
| Multi-dimensional Views | 多维展示   |

---

# 五、端到端流程（执行路径）

## 5.1 标准流程

```text
用户输入
↓
Conversation Engine（状态判断 + Slot检测）
↓
LLM Agent（理解 + 决策）
↓
Tool Layer（执行操作）
↓
Card Engine（校验 + 更新）
↓
Storage（持久化）
↓
Knowledge Organization（组织信息）
↓
UI实时更新（卡片预览 + 组件）
```

---

## 5.2 分步说明

### Step 1：用户输入

支持：

* 文本
* 图片
* 语音

---

### Step 2：对话引擎处理

* 判断当前 State
* 检测缺失 Slot
* 决策是否提问或调用 Tool

---

### Step 3：LLM Agent 决策

* 识别意图
* 选择 Tool
* 生成参数

---

### Step 4：Tool 执行

执行具体操作：

* 创建卡片
* 更新字段
* 分析内容
* 推荐标签

---

### Step 5：Card Engine 处理

* 校验字段合法性
* 应用 Schema
* 标准化数据

---

### Step 6：存储

* 写入数据库
* 生成 embedding
* 保存媒体文件

---

### Step 7：知识组织

* 自动归类
* 建立关系
* 更新集合

---

### Step 8：UI更新

* 实时卡片预览
* 更新交互组件

---

# 六、模块功能映射

| 模块                     | 功能              |
|------------------------|-----------------|
| Chat + Action UI       | 输入 / 操作 / 预览    |
| API Gateway            | 安全 / 限流 / 日志    |
| Conversation Engine    | 状态控制 / Slot管理   |
| LLM Agent Layer        | AI决策 / Tool选择   |
| Tool Layer             | 执行系统操作          |
| Card Engine            | 数据逻辑 / Schema校验 |
| Storage Layer          | 数据存储            |
| Knowledge Organization | 信息组织            |

---

# 七、关键设计亮点

## 7.1 Action Component System

* 对话中嵌入操作组件
* 替代手动输入
* 降低交互成本

---

## 7.2 Tool 调度机制

```text
AI → 决策
Tool → 执行
```

优势：

* 可控性强
* 可审计
* 易扩展

---

## 7.3 Universal Card Schema

统一结构带来：

* AI可理解
* 系统可校验
* 搜索可用

---

## 7.4 Knowledge Organization Model

实现：

* 智能集合（动态分类）
* 多维视图（标签/时间/位置）
* 知识图谱（关系网络）

---

## 7.5 Card Type System

作用：

* 决定字段结构
* 决定 UI 展示
* 支持扩展

---

# 八、工程落地建议

## 8.1 前端

```text
React / Vue
+ 状态管理（Redux / Zustand）
+ 动态组件系统（Action Components）
```

---

## 8.2 AI层

```text
LLM：GPT 系列
+ Tool Router（工具调度层）
+ Prompt 管理系统
```

---

## 8.3 存储

```text
Postgres（主数据库）
Vector DB（Pinecone / Milvus）
对象存储（S3 / OSS）
```

---

## 8.4 Schema系统

```text
JSON Schema
+ Validator（字段校验）
```

---

## 8.5 知识图谱

```text
Neo4j / TigerGraph / Graph Index
```

---

# 九、系统约束与设计规范

## 9.1 Tool 调用约束

* AI不得直接写数据库
* 必须通过 Tool Layer

---

## 9.2 Schema约束

* 所有 Card 必须符合 Schema
* 不允许非结构化存储

---

## 9.3 状态机约束

* 所有对话必须有 State
* 支持中断恢复

---

## 9.4 UI约束

* 必须有实时预览
* 必须提供快捷操作组件

---

# 十、总结

## 10.1 架构核心能力

```text
对话驱动
AI决策
Tool执行
结构化存储
知识组织
```

---

## 10.2 核心设计理念

```text
AI负责理解与决策
系统负责执行与约束
```

---

# 📌 备注（工程合理性）

* 架构符合当前主流 AI Agent 系统设计（Agent + Tool + Memory）
* Conversation Engine 是系统核心，优先级最高
* Tool Layer 是系统稳定性的关键（需严格设计接口）
* Knowledge Organization 决定长期价值（不是可选项）

---

如果你下一步继续给我「架构设计文档（详细版）」，我可以帮你把这套 PRD继续升级成：

* **可开发级技术设计文档（Tech Spec）**
* **数据库表结构设计**
* **Tool API 定义（直接给后端用）**
* **Agent Prompt 规范（可直接上线）**
