package tech.zhifu.app.myhub.feature.ai.layer.tool.command

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisMediaInput
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset

fun interface ToolCommandExecutor {
    suspend fun execute(command: ToolCommand): ToolCommandResult
}

sealed interface ToolCommandResult {
    data class DraftUpdated(
        val draft: CaptureDraft
    ) : ToolCommandResult

    data class Published(
        val cardId: String,
        val title: String,
    ) : ToolCommandResult

    data class MediaAttached(
        val draft: CaptureDraft,
        val attachedAssets: List<CaptureMediaAsset>,
        val analysisInputs: List<ProviderAnalysisMediaInput> = emptyList(),
    ) : ToolCommandResult

    data class Failed(
        val code: ToolErrorCode,
        val message: String,
    ) : ToolCommandResult
}

enum class ToolErrorCode {
    INVALID_ARGUMENT,
    EXECUTION_FAILED,
}
