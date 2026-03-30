# AI捕获系统-本地主导落地规划 V1.0

## 1. 文档目标

- 建立一套可落地实施方案：以客户端为主，服务端仅提供 AI 能力代理。
- 同时支持用户在客户端直接配置自己的 AI 模型 API，实现完全脱离服务端运行。
- 保持数据主权在本地设备，默认不上传个人资产正文。
- 在本文件中补齐完整架构说明，确保团队不依赖其他文档也可独立实施。

## 2. 设计原则

1. Local-first：卡片、草稿、媒体索引默认本地存储。
2. Client-first：交互、状态机、草稿编排优先在客户端执行。
3. AI pluggable：同一套调用协议可切换 AI 提供方（Server 代理 or 直连用户 API）。
4. Privacy by default：默认最小上传、可配置零留存。
5. Degrade gracefully：任一 AI 通道不可用时，仍可纯本地编辑与发布。
6. Tool-governed mutation：所有数据变更必须经 Tool 层进入 Card Engine，禁止 AI 直接写库。
7. Schema-enforced capture：卡片落库前必须通过统一字段约束与校验器。

## 3. 本地主导总体架构（补全版）

### 3.1 端到端分层

```text
User (App/Web)
      │
      ▼
+-------------------------+
| Chat + Action UI        | 交互层
+-------------------------+
      │
      ▼
+-------------------------+
| Conversation Engine     | 对话状态层
| - State Machine         |
| - Context Manager       |
| - Slot Manager          |
| - Action Planner        |
+-------------------------+
      │
      ▼
+-------------------------+
| LLM Agent Layer         | 推理决策层
| - Prompt Assembler      |
| - Tool Planner          |
| - Response Parser       |
+-------------------------+
      │
      ▼
+-------------------------+
| Tool Layer              | 系统能力层
| - create/update/publish |
| - analyze/summarize/tag |
+-------------------------+
      │
      ▼
+-------------------------+
| Card Engine             | 业务核心层
| - Draft Builder         |
| - Validator             |
| - Publisher             |
+-------------------------+
      │
      ▼
+-------------------------+
| Local Storage Layer     | 本地存储层
| - SQLite(SQLDelight)    |
| - Local File Store      |
| - Local Search Index    |
+-------------------------+
      │
      ├──────────────► Optional Sync/E2EE (默认关闭)
      └──────────────► Optional AI Server Gateway (仅推理代理)
```

### 3.2 系统边界

- 客户端是主系统：对话流程、草稿生成、卡片发布、索引更新均在本地完成。
- 服务端是可选增强：仅承担模型代理、配额、限流、观测；不承担用户资产主存储。
- 发布语义固定：`Publish = Local Commit`，不是上传云端。

### 3.3 部署形态

1. Offline-only：禁网运行，AI 关闭，用户手动完成全部字段。
2. Local + Server Gateway：默认模式，使用平台托管模型代理。
3. Local + Direct API：高级模式，用户自配 endpoint/model/key，绕开平台服务端。

## 4. 模块详细设计（当前文档内完整说明）

## 4.1 Chat + Action UI（交互层）

### 职责

- 承接用户输入（文本/图片/视频/附件/语音转文本）。
- 展示 AI 回复、缺失字段提示、实时卡片预览。
- 通过 Action Components 提供显式动作，降低自由输入成本。

### 核心子模块

1. Chat Timeline：消息流渲染，区分系统消息、AI消息、用户消息。
2. Action Panel：快捷动作（补标题、加标签、发布、继续补充、取消）。
3. Live Card Preview：实时映射草稿字段，展示卡片结构变化。
4. Upload Module：本地媒体导入、压缩、缩略图预览、上传前确认。

### 输入输出

- 输入：用户操作事件、状态机驱动指令、草稿快照。
- 输出：`UiEvent`（send_message/click_action/upload_media/edit_field）。

### 设计要点

- UI 只负责事件与渲染，不执行业务写库。
- 所有修改请求统一下沉到 Tool 层，避免 ViewModel 内出现隐式业务分叉。

### 界面布局：

```
+-----------------------+
| Chat Conversation     |
|                       |
| AI message            |
| User message          |
| [Action Buttons]      |
|                       |
+-----------------------+

+-----------------------+
| Card Preview          |
|                       |
| Title                 |
| Image                 |
| Tags                  |
| Summary               |
+-----------------------+
```

---

## 4.2 Conversation Engine（对话状态层）

### 职责

- 管理对话生命周期和状态流转。
- 维护上下文窗口
- 槽位填充（slot filling）。
- 根据当前完整度生成下一步动作计划。

### 详细结构：

```
Conversation Engine
      │
      ├── State Machine
      │
      ├── Context Manager
      │
      ├── Slot Manager
      │
      └── Action Planner
```

---
功能说明：

### State Machine

```
IDLE
INTENT
DRAFT
COLLECT
REVIEW
PUBLISH

```

---

### Context Manager

保存：

```
当前卡片
对话历史
当前状态
```

例如：

```
{
conversation_id
state
card_id
history
missing_fields
}
```

---

### Slot Manager

管理字段：

```
title
image
tag
location
note
```

检测：

```
missing_fields()
```

---

### Action Planner

决定下一步：

```
ask question
suggest tag
show review
publish card
```

---

### 子模块说明

1. State Machine：定义状态迁移规则、守卫条件、超时策略。
2. Context Manager：维护 conversation_id、history、active_draft_id、provider_mode。
3. Slot Manager：管理必填/可选字段，计算 `missing_fields`。
4. Action Planner：根据状态 + 缺失字段 + 用户偏好，生成下一步建议动作。

### 输入输出

- 输入：`UiEvent`、`ToolResult`、`AiResult`。
- 输出：`ConversationDecision`（ask_question/request_tool/show_review/publish_ready）。

### 关键细节

- Context 按“短上下文 + 摘要上下文”双层维护，减少 token 成本。
- 每次状态推进都写 `draft_session` 快照，保证应用异常退出后可恢复。

---

## 4.3 LLM Agent Layer（推理决策层）

### 职责

- 对用户输入进行意图理解、结构抽取、建议生成。
- 生成 Tool 调用计划，但不直接执行数据变更。

### 子模块

1. Prompt Assembler：拼装 system/task/tool/context prompt。
2. Tool Planner：输出候选工具及参数草案。
3. Response Parser：将模型输出解析为规范 JSON。
4. Output Guard：约束输出结构、过滤幻觉字段、做基本置信度判断。

### 输入输出契约

- 输入：`CaptureTaskRequest`（输入内容 + 对话上下文 + 缺失字段）。
- 输出：`CaptureTaskResponse`（字段建议 + tags/entities + suggestions + confidence）。

### 设计细节

- 采用“结构化 JSON 输出 + 本地 schema validator”双保险。
- 当模型输出不合规时，先本地纠错；失败则回退到“澄清提问”而非直接失败。
- Agent 输出必须可追溯到 `ai_job` 记录，便于重放与问题定位。

## 4.4 Tool Layer（系统能力层）

### 职责

- 承接 Agent 决策，执行确定性系统动作。
- 提供统一工具注册、参数校验、权限控制、幂等执行。

### 标准工具集

1. `create_card_draft`
2. `update_card_fields`
3. `suggest_tags`
4. `analyze_content`
5. `analyze_media`
6. `summarize_content`
7. `extract_entities`
8. `publish_card`
9. `search_cards`
10. `rollback_draft_version`

### 工具执行模型

```text
ToolRequest
  -> Validator
  -> Permission Check
  -> Execute (Card Engine / Local Index / AI Provider)
  -> ToolResult
```

### 关键细节

- Tool 是唯一可触发“数据写入”的入口，保证行为可审计。
- 每个 Tool 必须声明输入 schema、副作用范围、失败码、重试策略。
- 发布类 Tool 使用事务边界，确保“草稿->正式卡片”原子切换。

## 4.5 Card Engine（业务核心层）

### 职责

- 承担卡片业务规则和字段治理，不依赖具体 AI Provider。
- 管理草稿与正式态转换、版本增量、软删除、关系更新。

### 子模块

1. Draft Builder：草稿创建、局部字段合并、差异补丁。
2. Card Validator：字段合法性、类型一致性、长度约束、媒体引用完整性。
3. Field Formatter：规范化标签、时间、地点、来源信息。
4. Publisher：发布事务、版本推进、索引更新、事件通知。
5. Relation Updater：实体与卡片关系、知识图谱边更新。

### 数据对象

- `DraftCard`：可空字段多，允许不完整。
- `PublishedCard`：满足最小发布条件（标题/正文摘要/类型/时间等）。
- `CardVersion`：版本序列与变更摘要，支持回滚。

### 关键细节

- 草稿更新采用 patch 语义，避免全量覆盖造成字段丢失。
- 发布前执行 `prePublishCheck()`，失败时返回可修复建议。
- 发布成功后触发本地搜索索引增量更新与媒体引用固化。

## 4.6 Local Storage Layer（本地存储层）

### 职责

- 提供结构化存储、文件存储、检索索引三层能力。
- 保证离线可用、可恢复、可迁移。

### 三层结构

1. Structured DB：SQLite(SQLDelight) 保存 card/draft/media/relation/job。
2. File Store：保存图片/视频/附件原始文件与缩略图。
3. Search Index：本地全文或倒排索引（可选 FTS5）。

### 目录建议

```text
/app-data
  /cards/{cardId}
    card.json
    /media
      {mediaId}.orig
      {mediaId}.thumb.jpg
```

### 数据一致性策略

1. 先写文件（临时路径）再写 DB 事务。
2. DB 成功后原子重命名临时文件。
3. 任一环节失败回滚，避免孤儿文件。
4. 后台 GC 清理无引用媒体。

### 索引与性能

- 列表查询只取轻字段（title/summary/thumb/status/updated_at）。
- 详情页再加载大字段（content JSON、media details）。
- 大媒体导入采用分片复制和异步缩略图生成。

## 4.7 AI Provider Router（AI路由层）

### 职责

- 根据用户配置与运行时健康状态路由到具体 Provider。
- 对上暴露统一请求/响应，不泄露底层差异。

### Provider 类型

```text
server_gateway
direct_openai
direct_ollama
direct_compatible
disabled
```

### 路由规则

1. 首选用户显式配置模式。
2. Provider 健康检查失败时按降级链回退。
3. `disabled` 或全失败时返回 `AI_UNAVAILABLE`，状态机切换到 `MANUAL_EDIT`。

### 统一内部 DTO

请求：

```json
{
  "task": "capture_analysis",
  "input": {
    "text": "string",
    "media": []
  },
  "context": {
    "state": "INFO_COLLECT",
    "missing_fields": [
      "tags"
    ]
  }
}
```

响应：

```json
{
  "title": "string",
  "summary": "string",
  "tags": [
    "string"
  ],
  "entities": [],
  "suggestions": [],
  "confidence": 0.0
}
```

## 4.8 Optional Server Gateway（可选服务端能力层）

### 职责边界

- 仅做 AI 代理，不做用户正文资产主存储。
- 提供统一模型接口、重试、限流、配额、观测。

### 推荐接口

1. `POST /api/ai/capture-analysis`
2. `POST /api/ai/tool-execution`（可选）
3. `GET /api/ai/providers/health`

### 默认数据策略

- 默认不持久化正文，只保留最小运行指标。
- 指标可由用户或租户策略关闭。

## 4.9 Knowledge Organization（本地信息组织层）

### 职责

- 基于 Universal Card Schema 在本地组织卡片，支持检索与关联发现。

### 本地能力

1. 卡片类型映射：不同类型拥有不同字段模板和渲染策略。
2. 实体索引：从内容中抽取人物/地点/主题并建立倒排。
3. 关系图谱：`card_relation` 存储卡片间关联边。
4. 智能集合：按标签、时间、地点、主题生成动态视图。

### 设计细节

- 知识组织结果先本地生效，跨端同步是后续增强能力。
- 图谱计算采用增量任务，避免阻塞主交互线程。

## 4.10 Privacy & Security（隐私与安全层）

### 隐私模式

1. 仅本地：禁止任何网络推理。
2. 本地 + Server：最小上传。
3. 本地 + Direct API：绕过平台服务端。

### 核心策略

- 最小上传：仅当前任务所需字段。
- 上传前脱敏：手机号/邮箱/证件号/地址规则脱敏。
- 密钥保护：API Key 使用系统安全区 + 本地加密封装。
- 审计可见：记录“上传了什么字段”，供用户复核。

## 5. 关键流程设计（本地主导）

## 5.1 捕获创建流程（文本）

```text
User Input
  -> Conversation Engine (intent_detect)
  -> Tool:create_card_draft
  -> AI Provider Router:capture_analysis (可选)
  -> Tool:update_card_fields
  -> Slot Manager:missing_fields
  -> UI提问补全
  -> REVIEW
```

## 5.2 媒体捕获流程（图片/视频）

```text
Media Pick
  -> File Store(temp write)
  -> Metadata Extract(EXIF/duration/size)
  -> Thumb Generate(async)
  -> media_asset upsert
  -> Draft attach media refs
```

## 5.3 发布流程

```text
REVIEW confirm
  -> Tool:publish_card
  -> Card Validator
  -> DB transaction (card + relation + index marker)
  -> draft_session close
  -> UI show published card
```

## 5.4 AI不可用降级流程

```text
AI call failed/timeout
  -> Router returns AI_UNAVAILABLE
  -> State -> MANUAL_EDIT
  -> 用户手动补全字段
  -> 正常发布
```

## 6. 配置与开关设计

客户端新增配置：

- `ai.mode`: `server_gateway | direct_api | disabled`
- `ai.direct.endpoint`
- `ai.direct.model`
- `ai.direct.apiKey`（加密）
- `ai.request.timeoutMs`
- `ai.request.maxRetries`
- `privacy.upload.minimal`
- `privacy.masking.enabled`
- `privacy.audit.log.enabled`
- `capture.autoThumb.enabled`
- `search.fts.enabled`

## 7. 分期实施计划（更新版）

## Phase 1（2-3 周）：最小闭环可用

- 完成本地状态机、草稿仓库、发布事务。
- 打通 Tool 层基础工具（create/update/publish）。
- 接入 Server Gateway Provider。
- 实现 AI 失败降级到手动编辑。

交付标准：

- 离线可完整执行输入-草稿-发布。
- 在线可获得 AI 建议并安全合并到草稿。

## Phase 2（2-3 周）：本地能力补强

- 落地媒体本地链路（入库、缩略图、索引、删除回收）。
- 增加 Draft 恢复与会话重入。
- 增加本地搜索索引（可选 FTS）。

交付标准：

- 应用重启后草稿可恢复。
- 图片/视频导入、预览、删除稳定可用。

## Phase 3（2-3 周）：直连模型与隐私增强

- 增加 Direct API Provider（OpenAI 兼容 + Ollama）。
- 完成 AI 配置中心和连通性测试。
- 完成脱敏、上传审计、密钥加密保护。

交付标准：

- 关闭服务端后可用自配模型完成捕获。
- 隐私模式切换和上传裁剪可验证。

## Phase 4（可选）：多端同步（E2EE）

- 默认关闭，用户显式启用。
- 服务端仅存密文，支持设备间恢复。

## 8. 风险与对策（细化）

1. 直连 API 协议差异导致结果不稳定。

- 对策：实现 provider capability 探测 + 统一兼容层 + JSON guard。

2. 客户端密钥泄露风险。

- 对策：系统安全存储 + 本地密文包装 + 生物识别解锁。

3. 媒体处理耗时导致主线程卡顿。

- 对策：后台任务队列 + 可取消 + 进度回传 + 失败重试。

4. 状态机复杂后出现死循环或不可达状态。

- 对策：状态迁移白名单 + 单元测试 + 可视化状态图回归检查。

5. 文本/媒体上传超过用户预期。

- 对策：上传预览面板 + 每次调用显示上传字段摘要。

## 9. 验收标准（Definition of Done）

- 用户可在离线状态完成“输入-草稿-发布”全流程。
- AI 模式可切换（服务端代理/直连/禁用）并即时生效。
- 服务端不可用时，直连模式可独立工作；直连也失败时可手动完成。
- 本地资产（卡片正文、媒体、草稿）不依赖服务端保存。
- 隐私设置可验证（最小上传/脱敏/禁网模式/上传审计）。
- 关键流程可恢复（应用重启恢复草稿、事务失败不产生孤儿文件）。

## 10. 推荐实施顺序

1. 先完成 Conversation Engine + Tool Layer + Card Engine 本地闭环。
2. 再接 Server Gateway，验证“AI增强但不依赖”。
3. 落地媒体本地链路和搜索索引。
4. 增加 Direct API 模式与隐私安全能力。
5. 最后评估可选 E2EE 同步。
