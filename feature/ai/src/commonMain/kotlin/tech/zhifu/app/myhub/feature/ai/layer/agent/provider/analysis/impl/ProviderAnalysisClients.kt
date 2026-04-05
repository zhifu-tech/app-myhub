package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisOutput

internal fun parseProviderOutput(
    jsonText: String
): ProviderAnalysisOutput? {
    val root = runCatching {
        Json.parseToJsonElement(string = jsonText).jsonObject
    }.getOrNull() ?: return null

    val data = root["output"] as? JsonObject ?: root
    val title = data.string("title").orEmpty().trim()
    val summary = data.string("summary").orEmpty().trim()
    val tags = data.stringList("tags")
    val intent = data.string("intent").orEmpty().ifBlank { "create_card" }
    if (title.isBlank() && summary.isBlank()) return null
    return ProviderAnalysisOutput(
        intent = intent,
        title = title,
        summary = summary,
        tags = tags,
    )
}

private fun JsonObject.string(key: String): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull
}

private fun JsonObject.stringList(key: String): List<String> {
    val arr = this[key] as? JsonArray ?: return emptyList()
    return arr.mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }.filter { it.isNotBlank() }
}
