# AI捕获系统-会话协同协议 JSON Schema V1.0

> 废弃声明（2026-04-17）：V1 不再实现，仅保留历史记录。当前唯一实现标准为 `V2.0`。

## 1. 文档目标

- 提供可直接接入客户端与服务端的请求/响应 JSON Schema。
- 与《AI捕获系统-会话驱动草稿协同架构 V1.0》保持一致。
- 约束 AI 输出边界，确保 `draft_patch` 可校验、可审计、可执行。

## 2. 使用约定

- Schema 版本：JSON Schema Draft 2020-12。
- 校验策略：
- 请求入站：必须严格校验（`additionalProperties: false`）。
- 响应入站：严格校验；不合法响应降级为 `clarify` 分支。
- 字段命名：统一使用 `snake_case`。

## 3. 请求 Schema（AiCaptureRequest）

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "tech.zhifu.ai.capture.AiCaptureRequest.v1",
  "title": "AiCaptureRequest",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "session",
    "draft",
    "user_input",
    "history_summary",
    "field_specs",
    "runtime_context",
    "system_prompt_profile"
  ],
  "properties": {
    "session": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "conversation_id",
        "state",
        "missing_fields"
      ],
      "properties": {
        "conversation_id": {
          "type": "string",
          "minLength": 1
        },
        "state": {
          "type": "string",
          "enum": [
            "IDLE",
            "INFO_COLLECT",
            "CARD_REVIEW",
            "MANUAL_EDIT",
            "PUBLISH_CONFIRM",
            "COMPLETE",
            "CLARIFY"
          ]
        },
        "missing_fields": {
          "type": "array",
          "items": {
            "type": "string",
            "enum": [
              "title",
              "summary",
              "tags",
              "media",
              "source_text",
              "location",
              "occurred_at",
              "language"
            ]
          },
          "uniqueItems": true
        }
      }
    },
    "draft": {
      "$ref": "#/$defs/Draft"
    },
    "user_input": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "text",
        "attachments"
      ],
      "properties": {
        "text": {
          "type": "string"
        },
        "attachments": {
          "type": "array",
          "items": {
            "$ref": "#/$defs/Attachment"
          }
        }
      }
    },
    "history_summary": {
      "type": "string",
      "maxLength": 4000
    },
    "field_specs": {
      "$ref": "#/$defs/FieldSpecs"
    },
    "runtime_context": {
      "$ref": "#/$defs/RuntimeContext"
    },
    "system_prompt_profile": {
      "$ref": "#/$defs/SystemPromptProfile"
    }
  },
  "$defs": {
    "Draft": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "title",
        "summary",
        "tags",
        "media",
        "source_text",
        "extra"
      ],
      "properties": {
        "title": {
          "type": "string",
          "maxLength": 80
        },
        "summary": {
          "type": "string",
          "maxLength": 500
        },
        "tags": {
          "type": "array",
          "maxItems": 10,
          "uniqueItems": true,
          "items": {
            "type": "string",
            "minLength": 1,
            "maxLength": 20
          }
        },
        "media": {
          "type": "array",
          "items": {
            "$ref": "#/$defs/Media"
          }
        },
        "source_text": {
          "type": "string"
        },
        "location": {
          "$ref": "#/$defs/Location"
        },
        "occurred_at": {
          "type": "string",
          "format": "date-time"
        },
        "language": {
          "type": "string",
          "enum": [
            "zh-CN",
            "en-US",
            "ja-JP"
          ]
        },
        "extra": {
          "type": "object",
          "additionalProperties": true
        }
      }
    },
    "Media": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "id",
        "kind",
        "uri"
      ],
      "properties": {
        "id": {
          "type": "string",
          "minLength": 1
        },
        "kind": {
          "type": "string",
          "enum": [
            "image",
            "video",
            "audio"
          ]
        },
        "uri": {
          "type": "string",
          "minLength": 1
        },
        "thumbnail_uri": {
          "type": "string",
          "minLength": 1
        }
      }
    },
    "Attachment": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "id",
        "kind",
        "uri"
      ],
      "properties": {
        "id": {
          "type": "string",
          "minLength": 1
        },
        "kind": {
          "type": "string",
          "enum": [
            "image",
            "video",
            "audio",
            "file"
          ]
        },
        "uri": {
          "type": "string",
          "minLength": 1
        }
      }
    },
    "Location": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "name",
        "lat",
        "lng",
        "source"
      ],
      "properties": {
        "name": {
          "type": "string",
          "minLength": 1
        },
        "lat": {
          "type": "number",
          "minimum": -90,
          "maximum": 90
        },
        "lng": {
          "type": "number",
          "minimum": -180,
          "maximum": 180
        },
        "source": {
          "type": "string",
          "enum": [
            "gps",
            "manual",
            "inferred"
          ]
        }
      }
    },
    "FieldSpecs": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "title",
        "summary",
        "tags",
        "media",
        "source_text",
        "location",
        "occurred_at",
        "language",
        "extra"
      ],
      "properties": {
        "title": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "summary": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "tags": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "media": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "source_text": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "location": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "occurred_at": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "language": {
          "$ref": "#/$defs/FieldSpecItem"
        },
        "extra": {
          "$ref": "#/$defs/FieldSpecItem"
        }
      }
    },
    "FieldSpecItem": {
      "type": "object",
      "additionalProperties": true,
      "required": [
        "type",
        "meaning"
      ],
      "properties": {
        "type": {
          "type": "string",
          "minLength": 1
        },
        "meaning": {
          "type": "string",
          "minLength": 1
        }
      }
    },
    "RuntimeContext": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "now",
        "timezone",
        "language_preference",
        "device_locale",
        "app_channel"
      ],
      "properties": {
        "now": {
          "type": "string",
          "format": "date-time"
        },
        "timezone": {
          "type": "string",
          "minLength": 1
        },
        "user_location": {
          "$ref": "#/$defs/Location"
        },
        "language_preference": {
          "type": "string",
          "enum": [
            "zh-CN",
            "en-US",
            "ja-JP"
          ]
        },
        "device_locale": {
          "type": "string",
          "minLength": 2
        },
        "app_channel": {
          "type": "string",
          "enum": [
            "mobile",
            "desktop",
            "web"
          ]
        }
      }
    },
    "SystemPromptProfile": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "persona_profile"
      ],
      "properties": {
        "persona_profile": {
          "type": "object",
          "additionalProperties": false,
          "required": [
            "name",
            "role",
            "gender_style",
            "tone",
            "verbosity",
            "emoji_policy",
            "safety_style"
          ],
          "properties": {
            "name": {
              "type": "string",
              "minLength": 1
            },
            "role": {
              "type": "string",
              "minLength": 1
            },
            "gender_style": {
              "type": "string",
              "enum": [
                "neutral",
                "female",
                "male",
                "child"
              ]
            },
            "tone": {
              "type": "string",
              "minLength": 1
            },
            "verbosity": {
              "type": "string",
              "enum": [
                "short",
                "normal",
                "long"
              ]
            },
            "emoji_policy": {
              "type": "string",
              "enum": [
                "none",
                "limited",
                "free"
              ]
            },
            "safety_style": {
              "type": "string",
              "minLength": 1
            }
          }
        }
      }
    }
  }
}
```

## 4. 响应 Schema（AiCaptureResponse）

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "tech.zhifu.ai.capture.AiCaptureResponse.v1",
  "title": "AiCaptureResponse",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "intent",
    "assistant_text",
    "draft_patch",
    "next_actions",
    "confidence"
  ],
  "properties": {
    "intent": {
      "type": "string",
      "enum": [
        "welcome",
        "create_card",
        "update_title",
        "update_summary",
        "update_tags",
        "update_media",
        "clarify",
        "publish_ready",
        "manual_edit"
      ]
    },
    "assistant_text": {
      "type": "string",
      "minLength": 1,
      "maxLength": 1000
    },
    "draft_patch": {
      "$ref": "#/$defs/DraftPatch"
    },
    "next_actions": {
      "type": "array",
      "maxItems": 6,
      "items": {
        "$ref": "#/$defs/NextAction"
      }
    },
    "confidence": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    }
  },
  "$defs": {
    "DraftPatch": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "title": {
          "type": "string",
          "maxLength": 80
        },
        "summary": {
          "type": "string",
          "maxLength": 500
        },
        "tags": {
          "type": "array",
          "maxItems": 10,
          "uniqueItems": true,
          "items": {
            "type": "string",
            "minLength": 1,
            "maxLength": 20
          }
        },
        "media": {
          "type": "array",
          "items": {
            "type": "object",
            "additionalProperties": false,
            "required": [
              "id",
              "kind",
              "uri"
            ],
            "properties": {
              "id": {
                "type": "string",
                "minLength": 1
              },
              "kind": {
                "type": "string",
                "enum": [
                  "image",
                  "video",
                  "audio"
                ]
              },
              "uri": {
                "type": "string",
                "minLength": 1
              },
              "thumbnail_uri": {
                "type": "string",
                "minLength": 1
              }
            }
          }
        },
        "source_text": {
          "type": "string"
        },
        "location": {
          "type": "object",
          "additionalProperties": false,
          "required": [
            "name",
            "lat",
            "lng",
            "source"
          ],
          "properties": {
            "name": {
              "type": "string",
              "minLength": 1
            },
            "lat": {
              "type": "number",
              "minimum": -90,
              "maximum": 90
            },
            "lng": {
              "type": "number",
              "minimum": -180,
              "maximum": 180
            },
            "source": {
              "type": "string",
              "enum": [
                "gps",
                "manual",
                "inferred"
              ]
            }
          }
        },
        "occurred_at": {
          "type": "string",
          "format": "date-time"
        },
        "language": {
          "type": "string",
          "enum": [
            "zh-CN",
            "en-US",
            "ja-JP"
          ]
        },
        "extra": {
          "type": "object",
          "additionalProperties": true
        }
      }
    },
    "NextAction": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "label",
        "value"
      ],
      "properties": {
        "label": {
          "type": "string",
          "minLength": 1,
          "maxLength": 30
        },
        "value": {
          "type": "string",
          "enum": [
            "upload_media",
            "skip_media",
            "edit_title",
            "edit_summary",
            "edit_tags",
            "publish",
            "save_draft",
            "clarify"
          ]
        }
      }
    }
  }
}
```

## 5. 客户端合并与校验规则（实现约束）

- `draft_patch` 仅允许部分更新，不应回传完整 `draft`。
- 用户本轮显式输入字段优先级最高，AI 不得覆盖。
- `confidence < 0.60` 时：
- 禁止更新 `title/tags` 等关键字段。
- 强制将 `intent` 转入 `clarify` 或触发本地澄清问题。
- 相对时间词（今天/明天）必须在客户端根据 `runtime_context.now + timezone` 转绝对时间后再入库。
- AI 未知字段必须进入 `draft_patch.extra`，否则视为无效响应。

## 6. 示例

### 6.1 请求示例

```json
{
  "session": {
    "conversation_id": "conv_001",
    "state": "INFO_COLLECT",
    "missing_fields": [
      "title",
      "tags"
    ]
  },
  "draft": {
    "title": "",
    "summary": "",
    "tags": [],
    "media": [],
    "source_text": "周末去了一家很好吃的拉面店",
    "language": "zh-CN",
    "extra": {}
  },
  "user_input": {
    "text": "帮我整理成一张卡片，标题强调拉面",
    "attachments": []
  },
  "history_summary": "用户正在整理美食体验，尚未设置标题和标签",
  "field_specs": {
    "title": {
      "type": "string",
      "meaning": "卡片标题"
    },
    "summary": {
      "type": "string",
      "meaning": "摘要"
    },
    "tags": {
      "type": "string[]",
      "meaning": "标签"
    },
    "media": {
      "type": "array",
      "meaning": "媒体"
    },
    "source_text": {
      "type": "string",
      "meaning": "原始输入"
    },
    "location": {
      "type": "object?",
      "meaning": "位置"
    },
    "occurred_at": {
      "type": "string?",
      "meaning": "发生时间"
    },
    "language": {
      "type": "string",
      "meaning": "语言"
    },
    "extra": {
      "type": "object",
      "meaning": "扩展字段"
    }
  },
  "runtime_context": {
    "now": "2026-04-17T10:00:00+08:00",
    "timezone": "Asia/Shanghai",
    "language_preference": "zh-CN",
    "device_locale": "zh-CN",
    "app_channel": "mobile"
  },
  "system_prompt_profile": {
    "persona_profile": {
      "name": "Milo",
      "role": "结构化捕获助理",
      "gender_style": "neutral",
      "tone": "专业、简洁、友好",
      "verbosity": "short",
      "emoji_policy": "none",
      "safety_style": "仅做信息整理，不做高风险结论"
    }
  }
}
```

### 6.2 响应示例

```json
{
  "intent": "update_title",
  "assistant_text": "我先帮你拟一个标题，并推荐几个标签。",
  "draft_patch": {
    "title": "周末拉面店体验记录",
    "tags": [
      "美食",
      "拉面",
      "餐厅"
    ]
  },
  "next_actions": [
    {
      "label": "继续补充",
      "value": "edit_summary"
    },
    {
      "label": "直接发布",
      "value": "publish"
    }
  ],
  "confidence": 0.84
}
```

## 7. 版本策略

- 当前版本：`v1`。
- 兼容策略：
- 新增字段仅允许向后兼容扩展。
- 删除/重命名字段必须升级主版本（`v2`）。
