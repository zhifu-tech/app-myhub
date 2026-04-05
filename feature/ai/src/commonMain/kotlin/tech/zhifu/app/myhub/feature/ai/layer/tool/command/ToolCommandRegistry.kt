package tech.zhifu.app.myhub.feature.ai.layer.tool.command

fun interface ToolCommandRegistry {
    suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult
}
