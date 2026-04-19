//package tech.zhifu.app.myhub.feature.ai.layer.agent.impl
//
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonObject
//import kotlinx.serialization.json.JsonPrimitive
//import kotlinx.serialization.json.buildJsonObject
//import kotlinx.serialization.json.jsonObject
//import kotlinx.serialization.json.put
//import kotlinx.serialization.json.putJsonArray
//import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
//
//class PromptTemplateProvider {
//
//    fun systemPromptV2(): String = """
//        你是“卡片字段补全助手”。只返回 JSON，不要解释。
//
//        任务：
//        - 根据 input_text 和当前 draft，优先补 active_fields。
//        - 能直接提炼的 title、tags、summary，用 proposals.patches 返回。
//        - 只有缺少关键事实时，才用 proposals.clarify 提问。
//
//        约束：
//        - 默认只返回 proposals；只有确实需要时才返回 message、signals、suggestions。
//        - 仅修改 /title、/summary、/tags、/tags/-。
//        - /title、/summary 的 value 必须是字符串。
//        - /tags、/tags/- 的 value 必须是字符串或字符串数组。
//        - title 要简洁，像标题，不要写成长句描述。
//        - 不要询问系统规则或字段限制。
//        - 如果没有可补内容，也不要编造字段。
//
//        最小返回格式：
//        {"proposals":{"patches":[],"clarify":[]}}
//    """.trimIndent()
//
//    fun userPromptV2(request: ProviderAnalysisRequest): String = request.toLiteJson()
//
//    fun responseSchemaV2(): JsonObject = responseSchemaV2
//}
//
//private val responseSchemaV2 = Json.parseToJsonElement(
//    """
//    {
//      "type": "object",
//      "additionalProperties": false,
//      "required": ["proposals", "signals", "message", "suggestions"],
//      "properties": {
//        "proposals": {
//          "type": "object",
//          "additionalProperties": false,
//          "required": ["patches", "clarify"],
//          "properties": {
//            "patches": {
//              "type": "array",
//              "maxItems": 30,
//              "items": { "${'$'}ref": "#/${'$'}defs/JsonPatchOp" }
//            },
//            "clarify": {
//              "type": "array",
//              "maxItems": 3,
//              "items": {
//                "type": "object",
//                "additionalProperties": false,
//                "required": ["question", "field"],
//                "properties": {
//                  "question": { "type": "string", "minLength": 1, "maxLength": 120 },
//                  "field": { "type": "string", "minLength": 1 }
//                }
//              }
//            }
//          }
//        },
//        "signals": {
//          "type": "object",
//          "additionalProperties": false,
//          "required": ["publish_ready", "needs_clarification"],
//          "properties": {
//            "publish_ready": { "type": "boolean" },
//            "needs_clarification": { "type": "boolean" }
//          }
//        },
//        "message": { "type": "string", "minLength": 1, "maxLength": 500 },
//        "suggestions": {
//          "type": "array",
//          "maxItems": 5,
//          "items": {
//            "${'$'}ref": "#/${'$'}defs/SuggestionItem"
//          }
//        }
//      },
//      "${'$'}defs": {
//        "StringValue": {
//          "type": "string",
//          "minLength": 1
//        },
//        "TagValue": {
//          "oneOf": [
//            { "type": "string", "minLength": 1 },
//            {
//              "type": "array",
//              "minItems": 1,
//              "items": { "type": "string", "minLength": 1 }
//            }
//          ]
//        },
//        "PatchPath": {
//          "oneOf": [
//            { "enum": ["/title", "/summary", "/tags", "/tags/-"] },
//            { "type": "string", "pattern": "^/extra/.+" }
//          ]
//        },
//        "JsonPatchOp": {
//          "type": "object",
//          "additionalProperties": false,
//          "required": ["op", "path"],
//          "properties": {
//            "op": {
//              "type": "string",
//              "enum": ["add", "remove", "replace", "move", "copy", "test"]
//            },
//            "path": { "${'$'}ref": "#/${'$'}defs/PatchPath" },
//            "from": { "${'$'}ref": "#/${'$'}defs/PatchPath" },
//            "value": {}
//          },
//          "allOf": [
//            {
//              "if": {
//                "properties": {
//                  "op": { "enum": ["move", "copy"] }
//                },
//                "required": ["op"]
//              },
//              "then": {
//                "required": ["from"]
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "path": { "enum": ["/title", "/summary"] },
//                  "op": { "enum": ["add", "replace", "test"] }
//                },
//                "required": ["path", "op"]
//              },
//              "then": {
//                "required": ["value"],
//                "properties": {
//                  "value": { "${'$'}ref": "#/${'$'}defs/StringValue" }
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "path": { "enum": ["/tags", "/tags/-"] },
//                  "op": { "enum": ["add", "replace", "test"] }
//                },
//                "required": ["path", "op"]
//              },
//              "then": {
//                "required": ["value"],
//                "properties": {
//                  "value": { "${'$'}ref": "#/${'$'}defs/TagValue" }
//                }
//              }
//            }
//          ]
//        },
//        "SuggestionItem": {
//          "type": "object",
//          "additionalProperties": false,
//          "required": ["type", "priority"],
//          "properties": {
//            "type": {
//              "type": "string",
//              "enum": [
//                "add_media",
//                "replace_media",
//                "remove_media",
//                "refine_title",
//                "add_location",
//                "set_location",
//                "clear_location",
//                "improve_summary",
//                "suggest_tags",
//                "set_capture_type",
//                "review_draft",
//                "publish_when_ready"
//              ]
//            },
//            "priority": { "type": "string", "enum": ["high", "medium", "low"] },
//            "field": {
//              "type": "string",
//              "enum": ["title", "summary", "tags", "media", "location", "capture_type"]
//            },
//            "reason": { "type": "string", "minLength": 1, "maxLength": 120 },
//            "payload": {}
//          },
//          "allOf": [
//            {
//              "if": {
//                "properties": {
//                  "type": { "enum": ["add_media", "replace_media", "remove_media"] }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "properties": {
//                  "field": { "const": "media" }
//                },
//                "not": {
//                  "required": ["payload"]
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "type": { "const": "refine_title" }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "properties": {
//                  "field": { "const": "title" }
//                },
//                "not": {
//                  "required": ["payload"]
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "type": { "enum": ["add_location", "clear_location"] }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "properties": {
//                  "field": { "const": "location" }
//                },
//                "not": {
//                  "required": ["payload"]
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "type": { "const": "improve_summary" }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "properties": {
//                  "field": { "const": "summary" }
//                },
//                "not": {
//                  "required": ["payload"]
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "type": { "const": "suggest_tags" }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "required": ["field", "payload"],
//                "properties": {
//                  "field": { "const": "tags" },
//                  "payload": { "${'$'}ref": "#/${'$'}defs/SuggestTagsPayload" }
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "type": { "const": "set_location" }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "required": ["field", "payload"],
//                "properties": {
//                  "field": { "const": "location" },
//                  "payload": { "${'$'}ref": "#/${'$'}defs/SetLocationPayload" }
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "type": { "const": "set_capture_type" }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "required": ["field", "payload"],
//                "properties": {
//                  "field": { "const": "capture_type" },
//                  "payload": { "${'$'}ref": "#/${'$'}defs/SetCaptureTypePayload" }
//                }
//              }
//            },
//            {
//              "if": {
//                "properties": {
//                  "type": { "enum": ["review_draft", "publish_when_ready"] }
//                },
//                "required": ["type"]
//              },
//              "then": {
//                "not": {
//                  "required": ["payload"]
//                }
//              }
//            }
//          ]
//        },
//        "SuggestTagsPayload": {
//          "type": "object",
//          "additionalProperties": false,
//          "required": ["tags"],
//          "properties": {
//            "tags": {
//              "type": "array",
//              "minItems": 1,
//              "maxItems": 5,
//              "items": { "type": "string", "minLength": 1 }
//            }
//          }
//        },
//        "SetLocationPayload": {
//          "type": "object",
//          "additionalProperties": false,
//          "required": ["location"],
//          "properties": {
//            "location": { "type": "string", "minLength": 1 }
//          }
//        },
//        "SetCaptureTypePayload": {
//          "type": "object",
//          "additionalProperties": false,
//          "required": ["capture_type"],
//          "properties": {
//            "capture_type": {
//              "type": "string",
//              "enum": ["place", "idea", "article", "person"]
//            }
//          }
//        }
//      }
//    }
//    """.trimIndent()
//).jsonObject
//
//private val promptJson = Json {
//    prettyPrint = false
//    encodeDefaults = true
//    explicitNulls = false
//}
//
//private fun ProviderAnalysisRequest.toLiteJson(): String {
//    val draftData = draft.data
//    val activeFields = context.active_fields.toSet()
//    val draftPayload = buildJsonObject {
//        if (draftData.title.isNotBlank() || "title" in activeFields) {
//            put("title", draftData.title)
//        }
//        if (draftData.summary.isNotBlank() || "summary" in activeFields) {
//            put("summary", draftData.summary)
//        }
//        if (draftData.tags.isNotEmpty() || "tags" in activeFields) {
//            putJsonArray("tags") {
//                draftData.tags.take(8).forEach { add(JsonPrimitive(it)) }
//            }
//        }
//    }
//
//    val payload = buildJsonObject {
//        put("input_text", input.text)
//        put("draft", draftPayload)
//        putJsonArray("active_fields") {
//            context.active_fields.forEach { add(JsonPrimitive(it)) }
//        }
//        if (context.language.isNotBlank() && context.language != "zh-CN") {
//            put("language", context.language)
//        }
//    }
//    return promptJson.encodeToString(payload)
//}
