package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisData
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderJsonPatchOp

internal fun parseProviderOutput(
    jsonText: String,
    reasoning: String?
): ProviderAnalysisData? {
    val root = runCatching {
        Json.parseToJsonElement(
            string = unwrapJsonCodeFence(jsonText)
        ).jsonObject
    }.getOrNull() ?: return null

    val patches = buildList {
        root.string("title")?.trim()
            ?.let {
                ProviderJsonPatchOp(
                    op = "replace",
                    path = "/title",
                    value = JsonPrimitive(it),
                )
            }
            ?.let { add(it) }

        root.string("summary")?.trim()
            ?.let {
                ProviderJsonPatchOp(
                    op = "replace",
                    path = "/summary",
                    value = JsonPrimitive(it),
                )
            }
            ?.let { add(it) }

        root["tags"].jsonStringList()
            .map {
                ProviderJsonPatchOp(
                    op = "add",
                    path = "/tags/-",
                    value = JsonPrimitive(it),
                )
            }
            .forEach { add(it) }
    }

    if (patches.isEmpty()) return null

    return ProviderAnalysisData(
        patches = patches,
        reasoning = reasoning,
    )
}

private fun unwrapJsonCodeFence(text: String): String {
    val trimmed = text.trim()
    if (!trimmed.startsWith("```")) return trimmed

    val lines = trimmed.lines()
    if (lines.size >= 3 && lines.last().trim() == "```") {
        return lines
            .drop(1)
            .dropLast(1)
            .joinToString("\n")
            .trim()
    }

    return trimmed
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
}

fun JsonObject.string(key: String): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull
}

private fun JsonElement?.jsonStringList(
): List<String> = when (this) {
    is JsonPrimitive -> contentOrNull?.trim()?.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
    is JsonArray -> mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim()?.ifBlank { null } }
    else -> emptyList()
}
