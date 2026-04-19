package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProviderAnalysisRequest(
    @SerialName("input_text")
    val inputText: String,
    @SerialName("language")
    val language: String,
)

sealed interface ProviderAnalysisResult {
    @Serializable
    data class Success(
        @SerialName("data")
        val data: ProviderAnalysisData,
    ) : ProviderAnalysisResult

    @Serializable
    data class Failed(
        val reason: String,
        val category: ProviderAnalysisError,
    ) : ProviderAnalysisResult
}

@Serializable
data class ProviderAnalysisData(
    val patches: List<ProviderJsonPatchOp>,
    val reasoning: String? = null,
)

@Serializable
data class ProviderJsonPatchOp(
    val op: String,
    val path: String,
    val from: String? = null,
    val value: JsonElement? = null,
)

@Serializable
enum class ProviderAnalysisError {
    CONFIG,
    AUTH,
    TIMEOUT,
    HTTP,
    PARSE,
    UNAVAILABLE,
}
