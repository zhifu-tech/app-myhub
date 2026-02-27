package tech.zhifu.app.myhub.service.media.analysis

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.zhifu.app.myhub.service.media.CaptureAnalysisRequest

private val json = Json { ignoreUnknownKeys = true }

object AnalysisPromptBuilder {

    fun systemPrompt(): String {
        return """
你是一个「Capture 内容分析与卡片草稿生成助手」。
你需要参考 MyHub 的卡片生成提示词设计：先进行内容类型判断与结构化抽取，再映射到 Capture 的 Review 数据结构。
只输出一个合法 JSON 对象，不要输出任何解释、注释、Markdown 代码块。

【推断规则（沿用卡片生成策略）】
1. 输入是 URL：
   - 若明显是视频站或视频内容 => primaryContentType = "Video"
   - 否则 => 以文本摘要为主，primaryContentType = "Text"
2. 输入是图片或包含图片媒体：
   - 抽取可见文字/信息，填 imageOcrSummary / imageOcrInfo
   - 若只有图片信息且无代码文本 => primaryContentType 可为 "Image"
3. 输入是文本：
   - 含代码块或明显编程内容 => primaryContentType = "Code"，并尽量填 code / codeLanguage
   - 名言/短句/普通段落 => primaryContentType = "Text"
4. 有多个媒体时：
   - 视频优先于图片
   - 图片优先于纯文本

【输出结构（必须严格遵循）】
{
  "title": "string, 必填, <= 200 字",
  "text": "string, 必填, <= 10000 字",
  "sourceForm": "extract|link|own",
  "tags": ["string", "..."] ,
  "code": "string|null",
  "codeLanguage": "string|null",
  "imageOcrSummary": "string|null",
  "imageOcrInfo": "string|null",
  "videoMetadataSummary": "string|null",
  "videoMetadataInfo": "string|null",
  "primaryContentType": "Text|Code|Image|Video"
}

【字段约束】
- title/text 不得为空字符串。
- tags 生成 0~5 个，短标签，语义清晰，不重复。
- 不适用字段必须返回 null，不要编造。
- sourceForm 只能是 extract/link/own。
- primaryContentType 只能是 Text/Code/Image/Video。
- 字符串中的换行请使用 \n；不要输出未转义引号。

【质量要求】
- 优先高信息密度摘要，避免空泛描述。
- 内容语言尽量跟随用户输入语言（中文输入优先中文输出）。
        """.trimIndent()
    }

    fun userPrompt(request: CaptureAnalysisRequest): String {
        val media = request.media.joinToString(separator = "\n") {
            "- mediaId=${it.mediaId}, type=${it.type}, mimeType=${it.mimeType}, uri=${it.remoteUri}"
        }
        return """
请基于以下输入生成 Capture Review 结构化结果，并只输出 JSON 对象：

inputText:
${request.inputText.ifBlank { "(empty)" }}

intent: ${request.intent}
sourceForm (用户当前选择): ${request.sourceForm}
media:
${media.ifBlank { "- none" }}
        """.trimIndent()
    }

    fun parseModelOutput(raw: String): AnalysisModelOutput {
        val root = json.parseToJsonElement(raw).jsonObject
        return AnalysisModelOutput(
            title = root.string("title"),
            text = root.string("text"),
            sourceForm = root.string("sourceForm"),
            tags = root.strings("tags"),
            code = root.string("code"),
            codeLanguage = root.string("codeLanguage"),
            imageOcrSummary = root.string("imageOcrSummary"),
            imageOcrInfo = root.string("imageOcrInfo"),
            videoMetadataSummary = root.string("videoMetadataSummary"),
            videoMetadataInfo = root.string("videoMetadataInfo"),
            primaryContentType = root.string("primaryContentType")
        )
    }
}

internal fun buildOllamaRequestBody(
    model: String,
    request: CaptureAnalysisRequest,
    imagesBase64: List<String>
): String {
    val body = buildJsonObject {
        put("model", JsonPrimitive(model))
        put("stream", JsonPrimitive(false))
        put("format", JsonPrimitive("json"))
        put("messages", buildJsonArray {
            add(
                buildJsonObject {
                    put("role", JsonPrimitive("system"))
                    put("content", JsonPrimitive(AnalysisPromptBuilder.systemPrompt()))
                }
            )
            add(
                buildJsonObject {
                    put("role", JsonPrimitive("user"))
                    put("content", JsonPrimitive(AnalysisPromptBuilder.userPrompt(request)))
                    if (imagesBase64.isNotEmpty()) {
                        put("images", buildJsonArray {
                            imagesBase64.forEach { add(JsonPrimitive(it)) }
                        })
                    }
                }
            )
        })
        put("options", buildJsonObject {
            put("temperature", JsonPrimitive(0.2))
        })
    }
    return body.toString()
}

internal fun buildQwenRequestBody(model: String, request: CaptureAnalysisRequest): String {
    val body = buildJsonObject {
        put("model", JsonPrimitive(model))
        put("messages", buildJsonArray {
            add(
                buildJsonObject {
                    put("role", JsonPrimitive("system"))
                    put("content", JsonPrimitive(AnalysisPromptBuilder.systemPrompt()))
                }
            )
            add(
                buildJsonObject {
                    put("role", JsonPrimitive("user"))
                    put("content", JsonPrimitive(AnalysisPromptBuilder.userPrompt(request)))
                }
            )
        })
        put("response_format", buildJsonObject {
            put("type", JsonPrimitive("json_object"))
        })
        put("temperature", JsonPrimitive(0.2))
    }
    return body.toString()
}

internal fun extractOllamaContent(responseBody: String): String {
    val root = json.parseToJsonElement(responseBody).jsonObject
    return root["message"]?.jsonObject?.get("content")?.jsonPrimitive?.content
        ?: throw IllegalStateException("ollama response missing message.content")
}

internal fun extractQwenContent(responseBody: String): String {
    val root = json.parseToJsonElement(responseBody).jsonObject
    return root["choices"]?.jsonArray
        ?.firstOrNull()
        ?.jsonObject
        ?.get("message")
        ?.jsonObject
        ?.get("content")
        ?.jsonPrimitive
        ?.content
        ?: throw IllegalStateException("qwen response missing choices[0].message.content")
}

private fun JsonObject.string(name: String): String? {
    return this[name]?.jsonPrimitive?.contentOrNull
}

private fun JsonObject.strings(name: String): List<String> {
    val v = this[name] as? JsonArray ?: return emptyList()
    return v.mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }
        .filter { it.isNotBlank() }
}
