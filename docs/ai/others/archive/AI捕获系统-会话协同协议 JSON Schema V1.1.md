# AI捕获系统-会话协同协议 JSON Schema V1.1

> 废弃声明（2026-04-17）：V1 系列不再实现。请使用 `docs/ai/2_10-AI 捕获系统-会话协同协议JSON-SchemaV2.0.md`。

## 1. 目标

- 提供客户端可直接落地的请求/响应 JSON Schema（Draft 2020-12）。
- 修复 V1.0 在一致性与扩展性上的关键缺口：Patch 操作语义、Intent 粒度、状态迁移约束、风险决策、审计元数据。

## 2. 关键变更（相对 V1.0）

- `draft_patch` 从“值覆盖”升级为“操作型补丁（operation-based patch）”。
- `intent` 收敛为高层语义：`welcome | clarify | update_draft | publish_ready | manual_edit`。
- `missing_fields` 改为可扩展字符串列表，并增加“必须属于 `field_specs`”的业务校验规则。
- `field_specs` 增强为可执行约束：`required/priority/ai_hint/examples/allowed_values`。
- 新增多因子决策：`decision(confidence,risk_level,evidence)`，不再单点依赖 `confidence`。
- 新增字段锁、字段血缘、补丁追踪：`locked_fields`、`draft_meta.field_sources`、`patch_meta.trace_id`。
- 新增协议协商：`protocol.schema_version`。

## 3. 请求 Schema（AiCaptureRequest v1.1）

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "tech.zhifu.ai.capture.AiCaptureRequest.v1_1",
  "title": "AiCaptureRequestV11",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "protocol",
    "session",
    "state_policy",
    "draft",
    "draft_meta",
    "user_input",
    "history_summary",
    "field_specs",
    "runtime_context",
    "system_prompt_profile"
  ],
  "properties": {
    "protocol": {
      "type": "object",
      "additionalProperties": false,
      "required": ["schema_version"],
      "properties": {
        "schema_version": {
          "type": "string",
          "enum": ["1.1"]
        },
        "min_supported_version": {
          "type": "string"
        },
        "max_supported_version": {
          "type": "string"
        }
      }
    },
    "session": {
      "type": "object",
      "additionalProperties": false,
      "required": ["conversation_id", "state", "missing_fields", "locked_fields"],
      "properties": {
        "conversation_id": {"type": "string", "minLength": 1},
        "state": {
          "type": "string",
          "enum": ["IDLE", "INFO_COLLECT", "CARD_REVIEW", "MANUAL_EDIT", "PUBLISH_CONFIRM", "COMPLETE", "CLARIFY"]
        },
        "missing_fields": {
          "type": "array",
          "items": {"type": "string", "minLength": 1},
          "uniqueItems": true
        },
        "locked_fields": {
          "type": "array",
          "items": {"type": "string", "minLength": 1},
          "uniqueItems": true
        }
      }
    },
    "state_policy": {
      "type": "object",
      "additionalProperties": false,
      "required": ["current_state", "allowed_intents"],
      "properties": {
        "current_state": {
          "type": "string",
          "enum": ["IDLE", "INFO_COLLECT", "CARD_REVIEW", "MANUAL_EDIT", "PUBLISH_CONFIRM", "COMPLETE", "CLARIFY"]
        },
        "allowed_intents": {
          "type": "array",
          "items": {
            "type": "string",
            "enum": ["welcome", "clarify", "update_draft", "publish_ready", "manual_edit"]
          },
          "minItems": 1,
          "uniqueItems": true
        },
        "allowed_transitions": {
          "type": "object",
          "propertyNames": {
            "enum": ["IDLE", "INFO_COLLECT", "CARD_REVIEW", "MANUAL_EDIT", "PUBLISH_CONFIRM", "COMPLETE", "CLARIFY"]
          },
          "additionalProperties": {
            "type": "array",
            "items": {
              "type": "string",
              "enum": ["welcome", "clarify", "update_draft", "publish_ready", "manual_edit"]
            },
            "uniqueItems": true
          }
        }
      }
    },
    "draft": {"$ref": "#/$defs/Draft"},
    "draft_meta": {"$ref": "#/$defs/DraftMeta"},
    "user_input": {
      "type": "object",
      "additionalProperties": false,
      "required": ["text", "attachments"],
      "properties": {
        "text": {"type": "string"},
        "attachments": {
          "type": "array",
          "items": {"$ref": "#/$defs/Attachment"}
        }
      }
    },
    "history_summary": {"type": "string", "maxLength": 4000},
    "field_specs": {
      "type": "object",
      "propertyNames": {"pattern": "^[a-z][a-z0-9_]*$"},
      "additionalProperties": {"$ref": "#/$defs/FieldSpecItem"},
      "minProperties": 1
    },
    "runtime_context": {"$ref": "#/$defs/RuntimeContext"},
    "system_prompt_profile": {"$ref": "#/$defs/SystemPromptProfile"}
  },
  "$defs": {
    "Draft": {
      "type": "object",
      "additionalProperties": false,
      "required": ["title", "summary", "tags", "media", "source_text", "extra"],
      "properties": {
        "title": {"type": "string", "maxLength": 80},
        "summary": {"type": "string", "maxLength": 500},
        "tags": {
          "type": "array",
          "maxItems": 10,
          "uniqueItems": true,
          "items": {"type": "string", "minLength": 1, "maxLength": 20}
        },
        "media": {
          "type": "array",
          "items": {"$ref": "#/$defs/Media"}
        },
        "source_text": {"type": "string"},
        "location": {"$ref": "#/$defs/Location"},
        "occurred_at": {"type": "string", "format": "date-time"},
        "language": {"type": "string", "enum": ["zh-CN", "en-US", "ja-JP"]},
        "extra": {
          "type": "object",
          "propertyNames": {
            "pattern": "^[a-z][a-z0-9_]*\\.[a-z][a-z0-9_\\.]*$"
          },
          "additionalProperties": true
        }
      }
    },
    "DraftMeta": {
      "type": "object",
      "additionalProperties": false,
      "required": ["field_sources"],
      "properties": {
        "field_sources": {
          "type": "object",
          "propertyNames": {"pattern": "^[a-z][a-z0-9_]*$"},
          "additionalProperties": {
            "type": "string",
            "enum": ["user", "ai", "inferred", "system"]
          }
        }
      }
    },
    "Media": {
      "type": "object",
      "additionalProperties": false,
      "required": ["id", "kind", "uri"],
      "properties": {
        "id": {"type": "string", "minLength": 1},
        "kind": {"type": "string", "enum": ["image", "video", "audio"]},
        "uri": {"type": "string", "minLength": 1},
        "thumbnail_uri": {"type": "string", "minLength": 1}
      }
    },
    "Attachment": {
      "type": "object",
      "additionalProperties": false,
      "required": ["id", "kind", "uri"],
      "properties": {
        "id": {"type": "string", "minLength": 1},
        "kind": {"type": "string", "enum": ["image", "video", "audio", "file"]},
        "uri": {"type": "string", "minLength": 1}
      }
    },
    "Location": {
      "type": "object",
      "additionalProperties": false,
      "required": ["name", "lat", "lng", "source"],
      "properties": {
        "name": {"type": "string", "minLength": 1},
        "lat": {"type": "number", "minimum": -90, "maximum": 90},
        "lng": {"type": "number", "minimum": -180, "maximum": 180},
        "source": {"type": "string", "enum": ["gps", "manual", "inferred"]}
      }
    },
    "FieldSpecItem": {
      "type": "object",
      "additionalProperties": false,
      "required": ["type", "meaning", "required", "priority"],
      "properties": {
        "type": {"type": "string", "minLength": 1},
        "meaning": {"type": "string", "minLength": 1},
        "required": {"type": "boolean"},
        "priority": {"type": "integer", "minimum": 1, "maximum": 10},
        "ai_hint": {"type": "string"},
        "examples": {"type": "array", "items": {"type": "string"}, "maxItems": 5},
        "allowed_values": {"type": "array", "items": {"type": "string"}, "maxItems": 100}
      }
    },
    "RuntimeContext": {
      "type": "object",
      "additionalProperties": false,
      "required": ["now", "timezone", "language_preference", "device_locale", "app_channel"],
      "properties": {
        "now": {"type": "string", "format": "date-time"},
        "timezone": {"type": "string", "minLength": 1},
        "user_location": {"$ref": "#/$defs/Location"},
        "language_preference": {"type": "string", "enum": ["zh-CN", "en-US", "ja-JP"]},
        "device_locale": {"type": "string", "minLength": 2},
        "app_channel": {"type": "string", "enum": ["mobile", "desktop", "web"]}
      }
    },
    "SystemPromptProfile": {
      "type": "object",
      "additionalProperties": false,
      "required": ["persona_profile"],
      "properties": {
        "persona_profile": {
          "type": "object",
          "additionalProperties": false,
          "required": ["name", "role", "gender_style", "tone", "verbosity", "emoji_policy", "safety_style"],
          "properties": {
            "name": {"type": "string", "minLength": 1},
            "role": {"type": "string", "minLength": 1},
            "gender_style": {"type": "string", "enum": ["neutral", "female", "male", "child"]},
            "tone": {"type": "string", "minLength": 1},
            "verbosity": {"type": "string", "enum": ["short", "normal", "long"]},
            "emoji_policy": {"type": "string", "enum": ["none", "limited", "free"]},
            "safety_style": {"type": "string", "minLength": 1}
          }
        }
      }
    }
  }
}
```

## 4. 响应 Schema（AiCaptureResponse v1.1）

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "tech.zhifu.ai.capture.AiCaptureResponse.v1_1",
  "title": "AiCaptureResponseV11",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "protocol",
    "intent",
    "assistant_text",
    "operation_patch",
    "explanations",
    "next_actions",
    "decision"
  ],
  "properties": {
    "protocol": {
      "type": "object",
      "additionalProperties": false,
      "required": ["schema_version"],
      "properties": {
        "schema_version": {
          "type": "string",
          "enum": ["1.1"]
        }
      }
    },
    "intent": {
      "type": "string",
      "enum": ["welcome", "clarify", "update_draft", "publish_ready", "manual_edit"]
    },
    "assistant_text": {
      "type": "string",
      "minLength": 1,
      "maxLength": 1000
    },
    "operation_patch": {
      "$ref": "#/$defs/OperationPatch"
    },
    "explanations": {
      "type": "array",
      "items": {"$ref": "#/$defs/FieldExplanation"},
      "maxItems": 20
    },
    "next_actions": {
      "type": "array",
      "maxItems": 6,
      "items": {"$ref": "#/$defs/NextAction"}
    },
    "decision": {
      "$ref": "#/$defs/Decision"
    }
  },
  "$defs": {
    "OperationPatch": {
      "type": "object",
      "additionalProperties": false,
      "required": ["operations", "patch_meta"],
      "properties": {
        "operations": {
          "type": "array",
          "maxItems": 30,
          "items": {"$ref": "#/$defs/PatchOp"}
        },
        "patch_meta": {
          "type": "object",
          "additionalProperties": false,
          "required": ["trace_id", "model", "generated_at"],
          "properties": {
            "trace_id": {"type": "string", "minLength": 1},
            "model": {"type": "string", "minLength": 1},
            "generated_at": {"type": "string", "format": "date-time"}
          }
        }
      }
    },
    "PatchOp": {
      "type": "object",
      "additionalProperties": false,
      "required": ["field", "op"],
      "properties": {
        "field": {"type": "string", "minLength": 1},
        "op": {"type": "string", "enum": ["replace", "add", "remove"]},
        "value": {},
        "source": {
          "type": "string",
          "enum": ["user", "ai", "inferred", "system"]
        }
      }
    },
    "FieldExplanation": {
      "type": "object",
      "additionalProperties": false,
      "required": ["field", "reason"],
      "properties": {
        "field": {"type": "string", "minLength": 1},
        "reason": {"type": "string", "minLength": 1, "maxLength": 200},
        "evidence_from": {
          "type": "string",
          "enum": ["user_input", "history_summary", "runtime_context", "inference"]
        }
      }
    },
    "Decision": {
      "type": "object",
      "additionalProperties": false,
      "required": ["risk_level", "evidence"],
      "properties": {
        "confidence": {"type": "number", "minimum": 0, "maximum": 1},
        "risk_level": {"type": "string", "enum": ["low", "medium", "high"]},
        "evidence": {
          "type": "array",
          "items": {"type": "string", "minLength": 1},
          "minItems": 1,
          "maxItems": 10
        }
      }
    },
    "NextAction": {
      "type": "object",
      "additionalProperties": false,
      "required": ["label", "value"],
      "properties": {
        "label": {"type": "string", "minLength": 1, "maxLength": 30},
        "value": {
          "type": "string",
          "enum": ["upload_media", "skip_media", "edit_title", "edit_summary", "edit_tags", "publish", "save_draft", "clarify"]
        }
      }
    }
  }
}
```

## 5. 客户端强制规则（实现必须）

- `session.missing_fields` 中每个字段必须存在于 `field_specs`；否则请求无效。
- `intent` 必须属于 `state_policy.allowed_intents`；否则响应无效并降级 `clarify`。
- `operation_patch.operations[*].field` 必须存在于 `field_specs` 或匹配 `extra.*` 命名空间。
- `locked_fields` 中的字段禁止被 `replace/add/remove`。
- 冲突检测：
- 若同轮用户显式修改字段且 AI 同时修改同字段，保留用户输入并记录冲突日志。
- `decision.risk_level=high` 时禁止自动发布，仅允许澄清或手动编辑。
- 相对时间词必须在客户端按 `runtime_context.now + timezone` 归一为绝对时间。

## 6. 职责边界（稳定性约束）

| 模块 | 责任 |
|---|---|
| AI | 结构抽取、补丁建议、解释说明 |
| Client | 状态机与转移校验、冲突处理、UI 驱动 |
| Server/Tool | 最终校验、合并、持久化、发布门禁 |

## 7. 示例

### 7.1 响应示例（操作型 patch）

```json
{
  "protocol": {
    "schema_version": "1.1"
  },
  "intent": "update_draft",
  "assistant_text": "我先补齐标题并追加标签，你可以再确认是否发布。",
  "operation_patch": {
    "operations": [
      {
        "field": "title",
        "op": "replace",
        "value": "周末拉面店体验记录",
        "source": "ai"
      },
      {
        "field": "tags",
        "op": "add",
        "value": ["美食", "拉面"],
        "source": "ai"
      }
    ],
    "patch_meta": {
      "trace_id": "trace_20260417_0001",
      "model": "gpt-5.3",
      "generated_at": "2026-04-17T10:00:00+08:00"
    }
  },
  "explanations": [
    {
      "field": "title",
      "reason": "根据用户输入中的核心对象“拉面店”生成主标题",
      "evidence_from": "user_input"
    }
  ],
  "next_actions": [
    {
      "label": "补充摘要",
      "value": "edit_summary"
    },
    {
      "label": "直接发布",
      "value": "publish"
    }
  ],
  "decision": {
    "confidence": 0.84,
    "risk_level": "low",
    "evidence": ["来自用户当前输入", "与历史摘要一致"]
  }
}
```

## 8. 兼容与迁移

- 旧版 `v1.0` 的 `draft_patch` 值覆盖模式仅用于兼容读，不再作为默认写协议。
- 新接入客户端必须使用 `schema_version=1.1`。
- 版本升级原则：
- 仅新增可选字段可维持次版本兼容。
- 变更字段语义、删除字段、修改枚举都必须升级主版本。
