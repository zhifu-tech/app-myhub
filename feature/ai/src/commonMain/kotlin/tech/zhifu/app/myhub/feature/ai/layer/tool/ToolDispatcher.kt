package tech.zhifu.app.myhub.feature.ai.layer.tool

import tech.zhifu.app.myhub.feature.ai.CaptureDraft

class ToolDispatcher(
    private val registry: ToolRegistry,
    private val validator: ToolRequestValidator,
) {
    suspend fun execute(command: ToolCommand): ToolResult {
        val validation = validator.validate(command)
        if (!validation.ok) {
            return ToolResult.Failed(
                code = ToolErrorCode.INVALID_ARGUMENT,
                message = validation.message ?: "invalid_tool_argument",
            )
        }
        return runCatching { registry.execute(command) }.getOrElse { error ->
            ToolResult.Failed(
                code = ToolErrorCode.EXECUTION_FAILED,
                message = error.message ?: "tool_execution_failed",
            )
        }
    }
}

sealed interface ToolCommand {
    data class UpdateTitle(
        val draft: CaptureDraft,
        val title: String,
    ) : ToolCommand

    data class AddTag(
        val draft: CaptureDraft,
        val tag: String,
    ) : ToolCommand

    data class PublishCard(
        val draft: CaptureDraft,
    ) : ToolCommand

    data class AttachPickedMedia(
        val draft: CaptureDraft,
        val maxItems: Int = 3,
    ) : ToolCommand
}

sealed interface ToolResult {
    data class DraftUpdated(val draft: CaptureDraft) : ToolResult
    data class Published(
        val cardId: String,
        val title: String,
    ) : ToolResult
    data class MediaAttached(
        val draft: CaptureDraft,
    ) : ToolResult
    data class Failed(
        val code: ToolErrorCode,
        val message: String,
    ) : ToolResult
}

enum class ToolErrorCode {
    INVALID_ARGUMENT,
    PRECONDITION_FAILED,
    EXECUTION_FAILED,
}

class ToolRequestValidator {
    fun validate(command: ToolCommand): ToolValidationResult {
        return when (command) {
            is ToolCommand.UpdateTitle -> {
                if (command.title.trim().isBlank()) {
                    ToolValidationResult(false, "title_blank")
                } else {
                    ToolValidationResult(true)
                }
            }

            is ToolCommand.AddTag -> {
                if (command.tag.trim().isBlank()) {
                    ToolValidationResult(false, "tag_blank")
                } else {
                    ToolValidationResult(true)
                }
            }

            is ToolCommand.PublishCard -> {
                ToolValidationResult(true)
            }

            is ToolCommand.AttachPickedMedia -> {
                if (command.maxItems <= 0) {
                    ToolValidationResult(false, "max_items_invalid")
                } else {
                    ToolValidationResult(true)
                }
            }
        }
    }
}

data class ToolValidationResult(
    val ok: Boolean,
    val message: String? = null,
)
