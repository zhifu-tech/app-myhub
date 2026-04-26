package tech.zhifu.app.myhub.feature.ai.orchestrator.patch

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderJsonPatchOp
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureLocation
import tech.zhifu.app.myhub.feature.ai.model.CaptureType

data class PatchApplyResult(
    val draft: CaptureDraft,
    val droppedOps: Int,
)

class PatchApplier {
    fun apply(
        draft: CaptureDraft,
        ops: List<ProviderJsonPatchOp>,
    ): PatchApplyResult {
        var current = draft
        var dropped = 0

        ops.forEach { op ->
            val next = applySingle(current = current, op = op)
            if (next == null) {
                dropped += 1
            } else {
                current = next
            }
        }

        return PatchApplyResult(
            draft = current,
            droppedOps = dropped,
        )
    }

    private fun applySingle(
        current: CaptureDraft,
        op: ProviderJsonPatchOp,
    ): CaptureDraft? {
        return when (op.path) {
            "/title" -> applyTitle(current, op)
            "/summary" -> applySummary(current, op)
            "/sourceText" -> applySourceText(current, op)
            "/captureType" -> applyCaptureType(current, op)
            "/location", "/location/name" -> applyLocation(current, op)
            "/tags/-", "/tags" -> applyTags(current, op)
            else -> {
                if (op.path.startsWith("/extra/")) {
                    // CaptureDraft 当前模型没有 extra 容器，本轮忽略 extra 写入，避免污染结构。
                    current
                } else {
                    null
                }
            }
        }
    }

    private fun applyTitle(
        current: CaptureDraft,
        op: ProviderJsonPatchOp,
    ): CaptureDraft? {
        val value = jsonText(op.value)?.trim() ?: return null
        return when (op.op) {
            "replace", "add" -> current.copy(title = value)
            "remove" -> current.copy(title = "")
            else -> null
        }
    }

    private fun applySummary(
        current: CaptureDraft,
        op: ProviderJsonPatchOp,
    ): CaptureDraft? {
        val value = jsonText(op.value)?.trim()
        return when (op.op) {
            "replace", "add" -> current.copy(summary = value.orEmpty())
            "remove" -> current.copy(summary = "")
            else -> null
        }
    }

    private fun applyTags(
        current: CaptureDraft,
        op: ProviderJsonPatchOp,
    ): CaptureDraft? {
        val tags = current.tags.toMutableList()
        return when (op.op) {
            "replace" -> {
                val incoming = jsonStringList(op.value)
                current.copy(tags = incoming.distinct())
            }

            "add" -> {
                val incoming = jsonStringList(op.value)
                incoming.forEach { tag ->
                    if (tag.isNotBlank() && tag !in tags) {
                        tags += tag
                    }
                }
                current.copy(tags = tags)
            }

            "remove" -> {
                val incoming = jsonStringList(op.value)
                if (incoming.isEmpty()) {
                    current.copy(tags = emptyList())
                } else {
                    current.copy(tags = tags.filterNot { it in incoming })
                }
            }

            else -> null
        }
    }

    private fun applySourceText(
        current: CaptureDraft,
        op: ProviderJsonPatchOp,
    ): CaptureDraft? {
        val value = jsonText(op.value)?.trim()
        return when (op.op) {
            "replace", "add" -> current.copy(sourceText = value.orEmpty())
            "remove" -> current.copy(sourceText = "")
            else -> null
        }
    }

    private fun applyCaptureType(
        current: CaptureDraft,
        op: ProviderJsonPatchOp,
    ): CaptureDraft? {
        val value = jsonText(op.value)?.trim()
        return when (op.op) {
            "replace", "add" -> current.copy(captureType = value?.let(CaptureType::fromValue))
            "remove" -> current.copy(captureType = null)
            else -> null
        }
    }

    private fun applyLocation(
        current: CaptureDraft,
        op: ProviderJsonPatchOp,
    ): CaptureDraft? {
        val value = jsonText(op.value)?.trim()
        return when (op.op) {
            "replace", "add" -> current.copy(
                location = value
                    ?.takeIf { it.isNotBlank() }
                    ?.let { CaptureLocation(name = it) }
            )

            "remove" -> current.copy(location = null)
            else -> null
        }
    }

    private fun isSupported(path: String): Boolean {
        return path == "/title" ||
            path == "/summary" ||
            path == "/tags/-" ||
            path == "/tags" ||
            path.startsWith("/extra/")
    }

    private fun isLocked(path: String, locked: Set<String>): Boolean {
        val field = when {
            path.startsWith("/title") -> "title"
            path.startsWith("/summary") -> "summary"
            path.startsWith("/tags") -> "tags"
            path.startsWith("/media") -> "media"
            path.startsWith("/extra") -> "extra"
            else -> return false
        }
        return field in locked
    }
}

private fun jsonText(value: JsonElement?): String? {
    return (value as? JsonPrimitive)?.contentOrNull
}

private fun jsonStringList(value: JsonElement?): List<String> {
    return when (value) {
        is JsonPrimitive -> value.contentOrNull?.let { listOf(it) }.orEmpty()
        is JsonArray -> value.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        is JsonObject -> emptyList()
        null -> emptyList()
    }.map { it.trim() }.filter { it.isNotBlank() }
}
