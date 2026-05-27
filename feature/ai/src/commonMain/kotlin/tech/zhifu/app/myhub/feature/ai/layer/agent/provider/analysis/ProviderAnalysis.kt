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
    @SerialName("media_inputs")
    val mediaInputs: List<ProviderAnalysisMediaInput> = emptyList(),
)

@Serializable
data class ProviderAnalysisMediaInput(
    @SerialName("kind")
    val kind: Kind = Kind.IMAGE,
    @SerialName("mime_type")
    val mimeType: String,
    @SerialName("data_base64")
    val dataBase64: String,
    @SerialName("source_url")
    val sourceUrl: String? = null,
) {
    @Serializable
    enum class Kind {
        @SerialName("image")
        IMAGE,
    }

    fun toDataUrl(): String = "data:$mimeType;base64,$dataBase64"
}

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
