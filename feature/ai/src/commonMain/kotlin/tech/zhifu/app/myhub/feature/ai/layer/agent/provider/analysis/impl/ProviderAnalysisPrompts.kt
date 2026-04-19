package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest

fun systemPrompt(
): String = """
        你是“卡片字段补全助手”。从 input_text 中提取和推测，生成 JSON 格式的返回数据。需要满足以下约束
        - 必须使用 language 字段指定的语言返回结果
        - 必须包含 title, summary, tags，message 字段；
        - 其中 title 必须是字符串，且像标题；summary 必须是字符串，且像摘要；tags 必须是字符串数组，且最多5个，且每个元素像标签；
        - 若信息不足也要给可用草稿

        示例1: 输入：{ "input_text": "请帮我整理 李白的床前明月光", "language": "zh-CN"}
        输出: {"title":"静夜思·李白","summary":"窗前明月光，疑是地上霜；举头望明月，低头思故乡。","tags":["古典文学","诗词","唐代","李白","《静夜思》"]}
    """.trimIndent()

fun userPrompt(
    request: ProviderAnalysisRequest
) = request.toPromptJson()

internal fun ProviderAnalysisRequest.toPromptJson(): String {
    val promptJson = Json {
        prettyPrint = false
        encodeDefaults = true
        explicitNulls = false
    }
    return promptJson.encodeToString(this)
}
