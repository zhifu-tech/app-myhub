package tech.zhifu.app.myhub.feature.ai.layer.tool.command

import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

class ToolCommandDispatcher(
    private val workers: List<ToolCommandWorker>,
) {
    suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val commandClassName = command::class.qualifiedName
        logger.debug {
            "tool_command_dispatcher_execute:${commandClassName}, ${workers.size}"
        }
        val worker = workers.firstOrNull { it.commandClassName == commandClassName }
            ?: return ToolCommandResult.Failed(
                code = ToolErrorCode.EXECUTION_FAILED,
                message = "tool_command_executor_not_found:${command::class.simpleName.orEmpty()}",
            )
        val validation = worker.validator.validate(command)
        if (!validation.ok) {
            return ToolCommandResult.Failed(
                code = ToolErrorCode.INVALID_ARGUMENT,
                message = validation.message ?: "invalid_tool_argument",
            )
        }
        return runCatching { worker.executor.execute(command) }
            .getOrElse { error ->
                ToolCommandResult.Failed(
                    code = ToolErrorCode.EXECUTION_FAILED,
                    message = error.message ?: "tool_execution_failed",
                )
            }
    }
}
