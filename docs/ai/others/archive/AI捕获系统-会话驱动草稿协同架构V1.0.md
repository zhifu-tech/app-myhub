# AI捕获系统-会话驱动草稿协同架构 V1.0

> 废弃声明（2026-04-17）：V1 架构不再实现。当前实现基线为 `docs/ai/2_9-AI 捕获系统-会话驱动草稿协同架构V2.0.md`。

## 1. 背景与目标

- 将**“用户输入驱动 + AI持续完善草稿”**的工作模式标准化。
- 统一每轮 AI 交互协议，避免“自由文本难落地、状态漂移、字段冲突”。
- 保持当前工程约束不变：
    - AI 负责语义理解与建议。
    - 本地 Tool/CardEngine 负责实际数据变更与发布。
    - 通过可控协议提升稳定性与可审计性，而不是依赖自由发挥。

## 2. 核心原则（架构决议）

### 2.1. AI 负责“想”，系统负责“做”。

- AI 输出意图、补丁建议、回复文案、下一步动作。
- 本地系统执行校验、应用补丁、持久化、发布。

### 2.2. 单一草稿真相源（Single Source of Truth）。

- 维护统一 `draft`，包含**创建卡片所需全部字段**。
- 任意输入（文本/图片/标题/标签/缩略图）都映射为对 `draft` 的增量更新。

### 2.3. 每轮协同都显式带状态。

- 发送给 AI：`draft + conversation_state + state_policy + user_input + short_history_summary`。
- AI 返回：`intent + assistant_text + operation_patch + next_actions + decision`。

### 2.4. 发布"门禁"固定在本地。

- 仅当本地 `prePublishCheck` 通过才允许发布。
- AI 不直接写库，不绕过 Tool 层。

## 3. 协同数据模型

### 3.1 会话输入上下文（发送给 AI）

```json
{
  "session": {
    "conversation_id": "string",
    "state": "IDLE|INFO_COLLECT|CARD_REVIEW|MANUAL_EDIT|...",
    "missing_fields": [
      "title",
      "tags"
    ],
    "locked_fields": [
      "title"
    ]
  },
  "state_policy": {
    "current_state": "INFO_COLLECT",
    "allowed_intents": [
      "welcome",
      "clarify",
      "update_draft",
      "publish_ready",
      "manual_edit"
    ],
    "allowed_transitions": {
      "INFO_COLLECT": [
        "update_draft",
        "clarify"
      ],
      "CARD_REVIEW": [
        "update_draft",
        "publish_ready",
        "manual_edit"
      ]
    }
  },
  "protocol": {
    "schema_version": "1.1"
  },
  "draft_meta": {
    "field_sources": {
      "title": "user|ai|inferred|system"
    }
  },
  "draft": {
    "title": "",
    "summary": "",
    "tags": [],
    "media": [],
    "source_text": "",
    "extra": {}
  },
  "user_input": {
    "text": "string",
    "attachments": []
  },
  "history_summary": "string"
}
```

### 3.2 字段语义与取值规范（必须随请求提供给 AI）

```json
{
  "field_specs": {
    "title": {
      "type": "string",
      "meaning": "卡片主标题，用户可读，简洁明确",
      "required": true,
      "priority": 1,
      "ai_hint": "简洁、可检索、避免口语赘述",
      "constraints": {
        "min_len": 1,
        "max_len": 80
      },
      "examples": [
        "东京拉面店收藏",
        "周末阅读清单"
      ]
    },
    "summary": {
      "type": "string",
      "meaning": "对内容的摘要说明",
      "constraints": {
        "max_len": 500
      }
    },
    "tags": {
      "type": "string[]",
      "meaning": "主题标签，用于检索和聚合",
      "constraints": {
        "max_count": 10,
        "item_max_len": 20
      },
      "allowed_values_hint": [
        "美食",
        "旅行",
        "工作",
        "学习",
        "待办"
      ]
    },
    "media": {
      "type": "array",
      "meaning": "附件媒体列表（图/视频/音频）",
      "item_schema": {
        "id": "string",
        "kind": "image|video|audio",
        "uri": "string",
        "thumbnail_uri": "string?"
      }
    },
    "source_text": {
      "type": "string",
      "meaning": "用户原始输入文本，保留语义来源"
    },
    "location": {
      "type": "object?",
      "meaning": "地理位置信息（可选）",
      "schema": {
        "name": "string",
        "lat": "number",
        "lng": "number",
        "source": "gps|manual|inferred"
      }
    },
    "occurred_at": {
      "type": "string?",
      "meaning": "事件发生时间（ISO-8601）",
      "example": "2026-04-17T09:30:00+08:00"
    },
    "language": {
      "type": "string",
      "meaning": "输出与理解优先语言",
      "allowed_values_hint": [
        "zh-CN",
        "en-US",
        "ja-JP"
      ]
    },
    "extra": {
      "type": "object",
      "meaning": "扩展字段容器（新增字段先进入 extra）"
    }
  }
}
```

要求：

- AI 必须基于 `field_specs` 生成 `operation_patch`，不允许写入未知顶层字段。
- `missing_fields` 中的字段必须在 `field_specs` 中有定义。
- 若用户请求超出已定义字段，先写入 `extra.<namespace>.<key>` 并返回澄清建议。

### 3.3 AI 结构化响应（返回客户端）

```json
{
  "intent": "welcome|clarify|update_draft|publish_ready|manual_edit",
  "assistant_text": "给用户展示的反馈文案",
  "operation_patch": {
    "operations": [
      {
        "field": "tags",
        "op": "add|replace|remove",
        "value": [
          "拉面"
        ],
        "source": "ai|user|inferred|system"
      }
    ],
    "patch_meta": {
      "trace_id": "string",
      "model": "string",
      "generated_at": "ISO-8601"
    }
  },
  "explanations": [
    {
      "field": "tags",
      "reason": "依据用户输入中的实体词生成",
      "evidence_from": "user_input|history_summary|runtime_context|inference"
    }
  ],
  "decision": {
    "confidence": 0.0,
    "risk_level": "low|medium|high",
    "evidence": [
      "来自用户输入"
    ]
  },
  "next_actions": [
    {
      "label": "发布",
      "value": "publish"
    },
    {
      "label": "编辑标题",
      "value": "edit_title"
    }
  ]
}
```

## 4. 每轮处理流程（客户端）

### 1. 收集输入：

- 用户文本输入/动作/上传媒体。

### 2. 组装请求：

- 读取当前 `draft`、`conversation_state`、历史摘要。
- 附带 `field_specs + state_policy + runtime_context + system_prompt_profile`。

### 3. 调用 AI：

- 获得结构化响应（上面的统一 DTO）。

### 4. 本地校验并应用补丁：

- 对 `operation_patch` 做 schema 校验、字段规范化、冲突处理。
- 通过后写回 `draft` 与 `draft_session`。

### 5. 更新 UI：

- 展示 `assistant_text`。
- 按 `next_actions` 渲染动作组件。

### 6. 发布判定：

- 若意图/状态指向可发布，进入本地 `prePublishCheck`。
- 通过则 `ToolDispatcher -> CardEngine -> StorageGateway` 发布。

## 5. 三个典型用例（与你当前讨论一致）

### use case-1 启动场景（空输入）

- 输入为空，AI 返回：
- `intent=welcome`
- `assistant_text=欢迎与引导话术`
- `operation_patch.operations=[]`（不改草稿）
- `next_actions` 给出“开始输入/上传媒体”。

### use case-2 用户输入一段话

- AI 基于当前 `draft + state` 做意图识别：
- 新建内容
- 补充标题
- 修改标签
- 返回更新后的 `operation_patch` + 用户反馈文案。
- 客户端校验后合并补丁，刷新预览。

### use case-3 可发布阶段继续输入

- AI 判定 `publish_ready` 时提示用户可发布。
- 用户仍可继续输入，AI 返回修订补丁（如改标题/标签/摘要）。
- 直到本地校验通过再发布。

## 6. 边界与风控

- 不使用单一 `confidence` 做门禁；改为 `decision.risk_level + evidence + 冲突检测` 组合判定。
- 标签/标题等字段始终走本地 formatter + validator。
- `locked_fields` 字段禁止 AI 补丁改写。
- `state + intent` 必须通过本地状态转移校验，失败则强制降级 `clarify`。
- 发布失败需保留会话与草稿，允许重试与手动修订。
- Provider 不可用时降级 `MANUAL_EDIT`，流程不中断。

## 7. 系统提示词与运行时上下文规范（新增）

### 7.1 运行时上下文（每轮都传）

```json
{
  "runtime_context": {
    "now": "2026-04-17T10:00:00+08:00",
    "timezone": "Asia/Shanghai",
    "user_location": {
      "name": "Shanghai",
      "lat": 31.2304,
      "lng": 121.4737,
      "source": "gps"
    },
    "language_preference": "zh-CN",
    "device_locale": "zh-CN",
    "app_channel": "mobile"
  }
}
```

### 7.2 系统提示词模板（System Prompt Baseline）

```text
你是“捕获助手”，目标是帮助用户高效整理并发布卡片。
工作原则：
1) 只输出结构化响应：intent/assistant_text/operation_patch/decision/next_actions。
2) 严格遵守 field_specs；未知字段写入 extra，不得发散。
3) 用户显式修改优先级最高，不覆盖用户刚输入的明确值。
4) 当信息不足或高风险时，提出单一澄清问题，不猜测关键字段。
5) 使用 runtime_context 的时间、地点、语言生成自然且准确的话术。
6) 语气与人设遵循 persona_profile。
```

### 7.3 人设与语气配置（persona_profile）

```json
{
  "persona_profile": {
    "name": "Milo",
    "role": "结构化捕获助理",
    "gender_style": "neutral|female|male|child",
    "tone": "专业、简洁、友好",
    "verbosity": "short",
    "emoji_policy": "none",
    "safety_style": "不做医疗/法律/金融结论，仅做信息整理"
  }
}
```

约束：

- 默认推荐 `gender_style=neutral`，避免不必要拟人偏置。
- 产品若要求“男/女/小孩”人设，必须由配置中心下发，不在 prompt 中硬编码。

### 7.4 建议补充的条件（用于减少歧义）

- `privacy_mode`：是否允许使用位置/相册 EXIF/历史会话。
- `sensitivity_level`：普通/敏感内容处理级别，影响措辞与追问策略。
- `publish_policy`：发布前必填字段、阈值、禁止词规则。
- `tool_capability`：当前可用工具清单，避免 AI 生成不可执行动作。
- `output_language_fallback`：用户语言不明确时的回退语言策略。
- `date_interpretation_rule`：相对时间词（今天/明天）映射到绝对时间。

## 8. 细化实施计划（下一步）

### Phase A：协议定型

- 固化 `ProviderAnalysisRequest/Result` 为统一结构响应协议。
- 增加 `operation_patch` 合并器与冲突策略。

### Phase B：会话策略增强

- 引入 `history_summary` 生成器，控制 token 成本。
- 增加低置信度澄清分支与动作模板。

### Phase C：发布闭环增强

- 发布前检查项可配置化（字段规则/阈值）。
- 增强失败回滚、重放调试与审计追踪。

### Phase D：评估与监控

- 统计“补丁采纳率、澄清率、发布成功率、平均轮次”。
- 建立按 provider/mode 的质量对比报表。

## 9. 与当前代码映射（简要）

- 会话编排：`feature/ai/CaptureOrchestrator.kt`
- 对话状态：`feature/ai/layer/conversation/*`
- AI 路由与执行：`feature/ai/layer/agent/*`
- 工具与发布：`feature/ai/layer/tool/*` + `feature/ai/layer/cardengine/*`
- 本地持久化：`feature/ai/layer/storage/*`

该文档作为“协同协议基线”，后续迭代以此为准扩展，不再回退到“AI直接写库”路径。

配套协议 Schema 文档：

- `docs/ai/AI捕获系统-会话协同协议JSON-SchemaV1.0.md`
