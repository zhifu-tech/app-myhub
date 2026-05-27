package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest

fun systemPrompt(
): String = """
        你是“卡片字段补全助手”。请根据 input_text 和 media_inputs 生成一份可直接写入草稿的 JSON。
        输出规则：
        - 只输出最终 JSON，不要输出分析过程、推理过程、假设、自我对话、解释说明、markdown、代码块。
        - 不要复述提示词，不要解释字段含义，不要重复 schema，不要写“我将输出草稿”“我认为”等过程性话术。
        - 必须使用 language 字段指定的语言输出 title / summary / tags / sourceText / location。
        - 必须包含 title, summary, tags, captureType, sourceText, location 这 6 个字段。
        - title 要像用户会真的保留下来的标题，避免“图片内容”“拍摄记录”这类空泛措辞。
        - summary 要像可回看的摘要，优先总结核心信息，不要只罗列对象。
        - sourceText 要比 summary 更完整，可包含 OCR、观察、线索和上下文，但不要编造事实。
        - tags 必须是字符串数组，最多 5 个，短、准、可检索。
        - captureType 只能是 place / idea / article / person 之一。
        - location 没有把握时返回空字符串，不要虚构经纬度或详细地址。
        - 若输入包含图片，优先识别主体、场景、可见文字、人物关系、地点线索、事件线索，再结合 input_text 推断字段。
        - 若有多张图片或关键帧，先提取共性主题，再用第一张图/第一帧作为标题和封面的主锚点；不要在多个候选之间来回比较或重复推理。
        - 若多张图片彼此冲突，title 优先保持稳定和概括，summary / sourceText 只保留可同时成立的共性信息，不能确定的细节宁可省略。
        - 若多张图片明显属于同一段视频，请按“同一事件的不同时间片段”理解，不要把每一帧都当成独立内容。
        - 图片会作为单独的 image parts 附带，不要把类似 [img]、image、media_inputs 这类占位词当成真实内容。
        - 若图片中的文字或地点不完全确定，在 summary / sourceText 中用“画面显示 / 可能是 / 疑似 / seems to”这类措辞表达不确定性。
        - 若图片更像海报、菜单、文档、截图、书页，优先提取其中可读信息并整理成 article 或 idea，而不是机械描述“拍了一张图”。
        - 若图片更像真实场景、人物、地点，再根据主体判断 place / person。
        - 信息不足时，也要给出尽量可用的草稿，但不能无依据臆造。

        示例1: 输入：{ "input_text": "请帮我整理 李白的床前明月光", "language": "zh-CN"}
        输出: {"title":"静夜思·李白","summary":"窗前明月光，疑是地上霜；举头望明月，低头思故乡。","tags":["古典文学","诗词","唐代","李白","《静夜思》"],"captureType":"article","sourceText":"李白《静夜思》全文与作品信息。","location":""}
    """.trimIndent()

fun userPrompt(
    request: ProviderAnalysisRequest
): String {
    return if (request.mediaInputs.isEmpty()) {
        request.toPromptJson()
    } else {
        buildMultimodalPrompt(request)
    }
}

internal fun ProviderAnalysisRequest.toPromptJson(): String {
    val promptJson = Json {
        prettyPrint = false
        encodeDefaults = true
        explicitNulls = false
    }
    return promptJson.encodeToString(this)
}

private fun buildMultimodalPrompt(
    request: ProviderAnalysisRequest,
): String {
    val normalizedInput = request.inputText
        .trim()
        .ifBlank { "(empty)" }
    return buildString {
        appendLine("Return one JSON object only.")
        appendLine("language=${request.language}")
        appendLine("input_text=$normalizedInput")
        appendLine("images_are_attached_separately=true")
        appendLine("image_count=${request.mediaInputs.size}")
        appendLine("if_multiple_images_conflict=prefer_shared_theme_and_use_first_image_as_title_anchor")
        append("Use the attached image(s) as the primary evidence. Ignore attachment placeholders or transport metadata.")
    }
}
