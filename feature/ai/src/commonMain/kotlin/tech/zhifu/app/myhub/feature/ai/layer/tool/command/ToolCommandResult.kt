package tech.zhifu.app.myhub.feature.ai.layer.tool.command

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft

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
    ) : ToolCommandResult

    data class Failed(
        val code: ToolErrorCode,
        val message: String,
    ) : ToolCommandResult
}

enum class ToolErrorCode {
    INVALID_ARGUMENT,
    PRECONDITION_FAILED,
    EXECUTION_FAILED,
}
