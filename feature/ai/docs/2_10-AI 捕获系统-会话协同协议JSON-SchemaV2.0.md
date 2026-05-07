# AI捕获系统-会话协同协议 JSON Schema V2.0

## 1. 定位

- 本文档是客户端唯一实现标准。
- V2 采用“AI 提议、客户端裁决”的协同模式，不兼容 V1 流程控制协议。

## 2. 设计原则

- AI 仅输出候选提议：`patches + clarify + signals + message + suggestions`。
- 客户端是唯一状态机权威：AI 不输出最终 `intent/state`。
- Patch 使用 RFC 6902 JSON Patch，支持嵌套路径与标准化回放。
- 任何 AI 输出都必须先过本地校验、冲突处理与发布门禁。
- 建议采用 `schema-first + short prompt + runtime guard`：结构尽量由 JSON Schema 约束，提示词只保留任务目标与优先级。

## 3. 请求 Schema（AiCaptureRequestV2）

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "tech.zhifu.ai.capture.AiCaptureRequest.v2_0",
  "title": "AiCaptureRequestV2",
  "type": "object",
  "additionalProperties": false,
  "required": ["protocol", "session", "draft", "constraints", "input", "context"],
  "properties": {
    "protocol": {
      "type": "object",
      "additionalProperties": false,
      "required": ["version"],
      "properties": {
        "version": { "type": "string", "enum": ["2.0"] }
      }
    },
    "session": {
      "type": "object",
      "additionalProperties": false,
      "required": ["id", "turn_id"],
      "properties": {
        "id": { "type": "string", "minLength": 1 },
        "turn_id": { "type": "integer", "minimum": 1 }
      }
    },
    "draft": {
      "type": "object",
      "additionalProperties": false,
      "required": ["version", "data"],
      "properties": {
        "version": { "type": "integer", "minimum": 0 },
        "data": { "$ref": "#/$defs/DraftData" }
      }
    },
    "constraints": {
      "type": "object",
      "additionalProperties": false,
      "required": ["required_fields", "locked_fields", "field_rules", "conflict_policy"],
      "properties": {
        "required_fields": {
          "type": "array",
          "items": { "type": "string", "minLength": 1 },
          "uniqueItems": true
        },
        "locked_fields": {
          "type": "array",
          "items": { "type": "string", "minLength": 1 },
          "uniqueItems": true
        },
        "field_rules": {
          "type": "object",
          "propertyNames": { "pattern": "^[a-z][a-z0-9_]*$" },
          "additionalProperties": { "$ref": "#/$defs/FieldRule" }
        },
        "conflict_policy": {
          "type": "object",
          "propertyNames": { "pattern": "^[a-z][a-z0-9_]*$" },
          "additionalProperties": {
            "type": "string",
            "enum": ["user_wins", "merge", "append", "ai_wins"]
          }
        }
      }
    },
    "input": {
      "type": "object",
      "additionalProperties": false,
      "required": ["text", "attachments"],
      "properties": {
        "text": { "type": "string" },
        "attachments": {
          "type": "array",
          "items": { "$ref": "#/$defs/Attachment" }
        }
      }
    },
    "context": {
      "type": "object",
      "additionalProperties": false,
      "required": ["short_history", "active_fields", "language"],
      "properties": {
        "short_history": { "type": "string", "maxLength": 1200 },
        "key_facts": {
          "type": "object",
          "additionalProperties": true
        },
        "active_fields": {
          "type": "array",
          "items": { "type": "string", "minLength": 1 },
          "uniqueItems": true
        },
        "language": {
          "type": "string",
          "enum": ["zh-CN", "en-US", "ja-JP"]
        }
      }
    }
  },
  "$defs": {
    "DraftData": {
      "type": "object",
      "additionalProperties": false,
      "required": ["title", "summary", "tags", "media", "extra"],
      "properties": {
        "title": { "type": "string", "maxLength": 80 },
        "summary": { "type": "string", "maxLength": 500 },
        "tags": {
          "type": "array",
          "maxItems": 10,
          "uniqueItems": true,
          "items": { "type": "string", "minLength": 1, "maxLength": 20 }
        },
        "media": {
          "type": "array",
          "items": { "$ref": "#/$defs/Media" }
        },
        "extra": {
          "type": "object",
          "propertyNames": {
            "pattern": "^[a-z][a-z0-9_]*\\.[a-z][a-z0-9_\\.]*$"
          },
          "additionalProperties": true
        }
      }
    },
    "FieldRule": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "max_len": { "type": "integer", "minimum": 1 },
        "max_count": { "type": "integer", "minimum": 1 }
      }
    },
    "Media": {
      "type": "object",
      "additionalProperties": false,
      "required": ["id", "kind", "uri"],
      "properties": {
        "id": { "type": "string", "minLength": 1 },
        "kind": { "type": "string", "enum": ["image", "video", "audio"] },
        "uri": { "type": "string", "minLength": 1 }
      }
    },
    "Attachment": {
      "type": "object",
      "additionalProperties": false,
      "required": ["id", "kind", "uri"],
      "properties": {
        "id": { "type": "string", "minLength": 1 },
        "kind": { "type": "string", "enum": ["image", "video", "audio", "file"] },
        "uri": { "type": "string", "minLength": 1 }
      }
    }
  }
}
```

## 4. 响应 Schema（AiCaptureResponseV2）

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "tech.zhifu.ai.capture.AiCaptureResponse.v2_0",
  "title": "AiCaptureResponseV2",
  "type": "object",
  "additionalProperties": false,
  "required": ["proposals", "signals", "message", "suggestions"],
  "properties": {
    "proposals": {
      "type": "object",
      "additionalProperties": false,
      "required": ["patches", "clarify"],
      "properties": {
        "patches": {
          "type": "array",
          "maxItems": 30,
          "items": { "$ref": "#/$defs/JsonPatchOp" }
        },
        "clarify": {
          "type": "array",
          "maxItems": 3,
          "items": { "$ref": "#/$defs/ClarifyItem" }
        }
      }
    },
    "signals": {
      "type": "object",
      "additionalProperties": false,
      "required": ["publish_ready", "needs_clarification"],
      "properties": {
        "publish_ready": { "type": "boolean" },
        "needs_clarification": { "type": "boolean" }
      }
    },
    "message": {
      "type": "string",
      "minLength": 1,
      "maxLength": 500
    },
    "suggestions": {
      "type": "array",
      "maxItems": 5,
      "items": { "$ref": "#/$defs/SuggestionItem" }
    }
  },
  "$defs": {
    "PatchPath": {
      "oneOf": [
        { "enum": ["/title", "/summary", "/tags", "/tags/-"] },
        { "type": "string", "pattern": "^/extra/.+" }
      ]
    },
    "JsonPatchOp": {
      "type": "object",
      "additionalProperties": false,
      "required": ["op", "path"],
      "properties": {
        "op": {
          "type": "string",
          "enum": ["add", "remove", "replace", "move", "copy", "test"]
        },
        "path": { "$ref": "#/$defs/PatchPath" },
        "from": { "$ref": "#/$defs/PatchPath" },
        "value": {}
      },
      "allOf": [
        {
          "if": {
            "properties": {
              "op": { "enum": ["move", "copy"] }
            },
            "required": ["op"]
          },
          "then": {
            "required": ["from"]
          }
        },
        {
          "if": {
            "properties": {
              "path": { "enum": ["/title", "/summary"] },
              "op": { "enum": ["add", "replace", "test"] }
            },
            "required": ["path", "op"]
          },
          "then": {
            "required": ["value"],
            "properties": {
              "value": { "$ref": "#/$defs/StringValue" }
            }
          }
        },
        {
          "if": {
            "properties": {
              "path": { "enum": ["/tags", "/tags/-"] },
              "op": { "enum": ["add", "replace", "test"] }
            },
            "required": ["path", "op"]
          },
          "then": {
            "required": ["value"],
            "properties": {
              "value": { "$ref": "#/$defs/TagValue" }
            }
          }
        }
      ]
    },
    "ClarifyItem": {
      "type": "object",
      "additionalProperties": false,
      "required": ["question", "field"],
      "properties": {
        "question": { "type": "string", "minLength": 1, "maxLength": 120 },
        "field": { "type": "string", "minLength": 1 }
      }
    },
    "SuggestionItem": {
      "type": "object",
      "additionalProperties": false,
      "required": ["type", "priority"],
      "properties": {
        "type": {
          "type": "string",
          "enum": [
            "add_media",
            "replace_media",
            "remove_media",
            "refine_title",
            "add_location",
            "set_location",
            "clear_location",
            "improve_summary",
            "suggest_tags",
            "set_capture_type",
            "review_draft",
            "publish_when_ready"
          ]
        },
        "priority": { "type": "string", "enum": ["high", "medium", "low"] },
        "field": {
          "type": "string",
          "enum": ["title", "summary", "tags", "media", "location", "capture_type"]
        },
        "reason": { "type": "string", "minLength": 1, "maxLength": 120 },
        "payload": {}
      },
      "allOf": [
        {
          "if": {
            "properties": {
              "type": { "enum": ["add_media", "replace_media", "remove_media"] }
            },
            "required": ["type"]
          },
          "then": {
            "properties": {
              "field": { "const": "media" }
            },
            "not": {
              "required": ["payload"]
            }
          }
        },
        {
          "if": {
            "properties": {
              "type": { "const": "refine_title" }
            },
            "required": ["type"]
          },
          "then": {
            "properties": {
              "field": { "const": "title" }
            },
            "not": {
              "required": ["payload"]
            }
          }
        },
        {
          "if": {
            "properties": {
              "type": { "enum": ["add_location", "clear_location"] }
            },
            "required": ["type"]
          },
          "then": {
            "properties": {
              "field": { "const": "location" }
            },
            "not": {
              "required": ["payload"]
            }
          }
        },
        {
          "if": {
            "properties": {
              "type": { "const": "improve_summary" }
            },
            "required": ["type"]
          },
          "then": {
            "properties": {
              "field": { "const": "summary" }
            },
            "not": {
              "required": ["payload"]
            }
          }
        },
        {
          "if": {
            "properties": {
              "type": { "const": "suggest_tags" }
            },
            "required": ["type"]
          },
          "then": {
            "required": ["field", "payload"],
            "properties": {
              "field": { "const": "tags" },
              "payload": { "$ref": "#/$defs/SuggestTagsPayload" }
            }
          }
        },
        {
          "if": {
            "properties": {
              "type": { "const": "set_location" }
            },
            "required": ["type"]
          },
          "then": {
            "required": ["field", "payload"],
            "properties": {
              "field": { "const": "location" },
              "payload": { "$ref": "#/$defs/SetLocationPayload" }
            }
          }
        },
        {
          "if": {
            "properties": {
              "type": { "const": "set_capture_type" }
            },
            "required": ["type"]
          },
          "then": {
            "required": ["field", "payload"],
            "properties": {
              "field": { "const": "capture_type" },
              "payload": { "$ref": "#/$defs/SetCaptureTypePayload" }
            }
          }
        },
        {
          "if": {
            "properties": {
              "type": { "enum": ["review_draft", "publish_when_ready"] }
            },
            "required": ["type"]
          },
          "then": {
            "not": {
              "required": ["payload"]
            }
          }
        }
      ]
    },
    "StringValue": {
      "type": "string",
      "minLength": 1
    },
    "TagValue": {
      "oneOf": [
        { "type": "string", "minLength": 1 },
        {
          "type": "array",
          "minItems": 1,
          "items": { "type": "string", "minLength": 1 }
        }
      ]
    },
    "SuggestTagsPayload": {
      "type": "object",
      "additionalProperties": false,
      "required": ["tags"],
      "properties": {
        "tags": {
          "type": "array",
          "minItems": 1,
          "maxItems": 5,
          "items": { "type": "string", "minLength": 1 }
        }
      }
    },
    "SetLocationPayload": {
      "type": "object",
      "additionalProperties": false,
      "required": ["location"],
      "properties": {
        "location": { "type": "string", "minLength": 1 }
      }
    },
    "SetCaptureTypePayload": {
      "type": "object",
      "additionalProperties": false,
      "required": ["capture_type"],
      "properties": {
        "capture_type": {
          "type": "string",
          "enum": ["place", "idea", "article", "person"]
        }
      }
    }
  }
}
```

## 5. 客户端实现强制规则

- 客户端是状态机唯一权威，AI 响应不参与状态迁移决策。
- `draft.version` 必须与本地一致才能应用 patch，不一致必须 `reject/rebase`。
- patch 白名单路径：`/title /summary /tags /extra/*`，其余全部丢弃；媒体相关动作统一通过 `suggestions` 表达。
- `/title`、`/summary` 的 `value` 仅允许字符串；对象、数组一律视为非法 patch。
- `/tags`、`/tags/-` 的 `value` 仅允许字符串或字符串数组；对象一律视为非法 patch。
- 仅 `suggest_tags`、`set_location`、`set_capture_type` 允许携带 `payload`；其余 suggestion 出现 `payload` 时应在客户端剥离或拒绝。
- `suggest_tags` 必须带 `payload.tags`；`set_location` 必须带 `payload.location`；`set_capture_type` 必须带 `payload.capture_type`。
- `locked_fields` 对应路径不可改写，命中直接丢弃并记录审计日志。
- 应用顺序：用户输入落地 -> AI patch -> 冲突策略 -> 校验 -> `draft.version + 1`。
- 发布门禁仅依赖本地校验：`required_fields`、字段规则、风险检查，不依赖 AI 直接触发。
- 客户端应对 AI `message` 做运行时归一化：message 只允许描述真实 patch / clarify / suggestions 结果，不得信任“空 patches 却声称已补充”的文案。

## 6. 示例

```json
{
  "proposals": {
    "patches": [
      { "op": "replace", "path": "/title", "value": "周末拉面店记录" },
      { "op": "add", "path": "/tags/-", "value": "拉面" }
    ],
    "clarify": [
      { "question": "要补充店名吗？", "field": "title" }
    ]
  },
  "signals": {
    "publish_ready": true,
    "needs_clarification": false
  },
  "message": "我已补充标题和标签。",
  "suggestions": [
    {
      "type": "add_media",
      "priority": "high",
      "field": "media",
      "reason": "visual_first"
    }
  ]
}
```

## 7. 关联文档

- 架构说明：[2_9-AI 捕获系统-会话驱动草稿协同架构V2.0.md](./2_9-AI%20捕获系统-会话驱动草稿协同架构V2.0.md)
- 完整请求/返回示例：[2_11-AI 捕获系统-V2完整请求与返回示例.md](./2_11-AI%20捕获系统-V2完整请求与返回示例.md)
