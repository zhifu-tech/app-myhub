package tech.zhifu.app.myhub.feature.ai.layer.tool.command

class ToolCommandDispatcher(
    private val registry: ToolCommandRegistry,
    private val validator: ToolCommandValidator,
) {
    suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val validation = validator.validate(command)
        if (!validation.ok) {
            return ToolCommandResult.Failed(
                code = ToolErrorCode.INVALID_ARGUMENT,
                message = validation.message ?: "invalid_tool_argument",
            )
        }
        return runCatching { registry.execute(command) }.getOrElse { error ->
            ToolCommandResult.Failed(
                code = ToolErrorCode.EXECUTION_FAILED,
                message = error.message ?: "tool_execution_failed",
            )
        }
    }
}

