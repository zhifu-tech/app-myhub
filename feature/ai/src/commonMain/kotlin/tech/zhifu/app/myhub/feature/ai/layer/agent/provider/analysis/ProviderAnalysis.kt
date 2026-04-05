package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis

import kotlinx.serialization.Serializable

@Serializable
data class ProviderAnalysisRequest(
    val task: String = "capture_analysis",
    val input: ProviderAnalysisInput,
    val context: ProviderAnalysisContext,
)

@Serializable
data class ProviderAnalysisInput(
    val text: String,
    val media: List<String> = emptyList(),
)

@Serializable
data class ProviderAnalysisContext(
    val state: String,
    val missing_fields: List<String> = emptyList(),
)

data class ProviderAnalysisOutput(
    val intent: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
)

sealed interface ProviderAnalysisResult {
    data class Success(
        val output: ProviderAnalysisOutput,
        val rawResponseJson: String,
        val reasoning: String? = null,
    ) : ProviderAnalysisResult

    data class Failed(
        val reason: String,
        val category: ProviderErrorCategory,
    ) : ProviderAnalysisResult
}

enum class ProviderErrorCategory {
    CONFIG,
    AUTH,
    TIMEOUT,
    HTTP,
    PARSE,
    UNAVAILABLE,
}
