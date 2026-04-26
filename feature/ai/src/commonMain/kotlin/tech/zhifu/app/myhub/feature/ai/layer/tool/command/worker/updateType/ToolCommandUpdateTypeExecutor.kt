package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateType

import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult

class ToolCommandUpdateTypeExecutor(
    private val cardEngine: CardEngine,
) : ToolCommandExecutor {
    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val command = command as ToolCommand.UpdateType
        return ToolCommandResult.DraftUpdated(
            draft = cardEngine.updateDraftType(
                draft = command.draft,
                type = command.type,
            )
        )
    }
}
