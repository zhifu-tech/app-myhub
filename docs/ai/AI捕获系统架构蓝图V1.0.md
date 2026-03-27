# AI捕获系统架构蓝图V1.0

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

我会分层说明，标出各个模块及其功能，确保团队落地开发可用。

---

# 一、整体端到端架构（端到端蓝图）

```text id="0kq9xs"
User (App/Web)
      │
      ▼
+------------------------+
|  Chat + Action UI       |  ← 用户界面
|  - Chat Conversation    |
|  - Live Card Preview    |
|  - Action Components    |
+------------------------+
      │
      ▼
+------------------------+
|   API Gateway           |  ← 请求入口
|  - Auth                |
|  - Rate Limit          |
|  - Logging             |
+------------------------+
      │
      ▼
+------------------------+
| Conversation Engine     |  ← 对话状态管理
|  - State Machine       |
|  - Context Manager     |
|  - Slot Manager        |
|  - Action Planner      |
+------------------------+
      │
      ▼
+------------------------+
|   LLM Agent Layer       |  ← AI 推理和决策
|  - Prompt System       |
|      • System Prompt   |
|      • Task Prompt     |
|      • Tool Prompt     |
|  - Tool Planner        |
|  - Reasoning + Decision|
+------------------------+
      │
      ▼
+------------------------+
| Tool Layer              |  ← 系统能力调用
|  - create_card         |
|  - update_card         |
|  - suggest_tags        |
|  - analyze_content     |
|  - analyze_image       |
|  - summarize_content   |
|  - extract_entities    |
|  - publish_card        |
|  - search_cards        |
+------------------------+
      │
      ▼
+------------------------+
|    Card Engine          |  ← 业务逻辑核心
|  - Card CRUD           |
|  - Card Validator      |
|  - Field Formatter     |
|  - Schema Enforcement  |  ← Universal Card Schema
+------------------------+
      │
      ▼
+------------------------+
|   Storage Layer         |  ← 数据存储与检索
|  - Primary DB (cards, conversations) |
|  - Vector DB (semantic search, embeddings) |
|  - File Storage (images, attachments) |
+------------------------+
      │
      ▼
+------------------------+
| Knowledge Organization  |  ← 信息组织层
|  - Universal Card Schema|
|  - Card Type System     |
|  - Knowledge Graph      |
|  - Smart Collections    |
|  - Multi-dimensional Views |
+------------------------+
```

---

# 二、流程说明（端到端）

1️⃣ **用户输入**

* 文字、图片、语音
* UI 提供快速操作按钮（Action Components）

2️⃣ **Conversation Engine**

* 状态机控制流程
* Slot Manager 检测缺失字段
* Action Planner 决定下一步 AI 提问或操作

3️⃣ **LLM Agent**

* 根据 Prompt 系统理解用户意图
* 选择合适 Tool
* 决策下一步操作

4️⃣ **Tool Layer**

* 执行具体操作（创建/更新卡片、分析内容、推荐标签等）

5️⃣ **Card Engine**

* 处理业务逻辑
* 强制执行 Universal Card Schema
* 更新字段、校验卡片完整性

6️⃣ **Storage Layer**

* 保存卡片、向量化 Embedding、媒体文件
* 支持语义搜索和快速查询

7️⃣ **Knowledge Organization**

* 智能组织卡片到动态集合（Collection）
* 生成知识图谱（Card 之间关系）
* 支持多维度视图（标签、时间、类型、地理位置）

8️⃣ **UI 实时更新**

* 卡片实时渲染
* 动态更新 Action Component
* 支持直接编辑和发布

---

# 三、模块功能映射总结

| 模块                     | 功能                                     |
|------------------------|----------------------------------------|
| Chat + Action UI       | 用户输入、快速操作、实时卡片预览                       |
| API Gateway            | 安全入口、流量控制、日志                           |
| Conversation Engine    | 对话状态机、Slot管理、上下文                       |
| LLM Agent Layer        | 理解意图、工具调度、AI决策                         |
| Tool Layer             | 执行操作（CRUD、分析、推荐、发布）                    |
| Card Engine            | 业务逻辑、字段验证、Schema 执行                    |
| Storage Layer          | 数据库、向量存储、文件存储                          |
| Knowledge Organization | 卡片类型体系、Universal Card Schema、智能集合、知识图谱 |

---

# 四、关键设计亮点

1. **Action Component System**：对话中动态插入操作组件，减少用户输入，提高效率
2. **LLM Agent + Tool 调度**：AI 不直接修改数据，所有变更通过 Tool 完成，提高系统可控性
3. **Universal Card Schema**：统一字段结构，AI可填充、系统可校验、搜索可用
4. **Knowledge Organization Model**：智能集合 + 多维度视图 + 关系图谱，让信息不会乱
5. **Card Type System**：字段和 UI 映射，不同类型卡片有专属模板和展示方式

---

# 五、落地建议

* UI 层：React/Vue + 状态管理 + 动态组件渲染
* LLM Agent：可以用 GPT 系列 + 自定义 Tool Router
* Storage：Postgres + Vector DB (Pinecone/Milvus) + S3/OSS
* 卡片 Schema + Type 系统：JSON Schema + Schema Validator
* Knowledge Graph：Neo4j / TigerGraph / 关系索引

---

我可以帮你画一张 **可直接交给设计与开发团队的视觉化架构图**，把 UI、AI、Tool、Card、Knowledge Graph 都可视化出来，这样更直观。


## 4. 图示规范
- 图示统一使用 `text` 代码块呈现。
- 流程图默认从上到下，模块图默认从左到右。
- 节点命名使用英文技术词，描述使用中文。
