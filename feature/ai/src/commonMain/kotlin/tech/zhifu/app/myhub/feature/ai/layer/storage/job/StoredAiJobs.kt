package tech.zhifu.app.myhub.feature.ai.layer.storage.job

import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredAiJob

fun StoredAiJob.failed(
    reason: String,
    category: String = "Unknown",
) = copy(
    responseJson = """{"error":"${reason.escapeJson()}","category":"$category"}""",
    status = "failed",
)


private fun String.escapeJson(): String = this
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
