package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateLocation

import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult

class ToolCommandUpdateLocationExecutor(
    private val cardEngine: CardEngine,
) : ToolCommandExecutor {
    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val command = command as ToolCommand.UpdateLocation
        return ToolCommandResult.DraftUpdated(
            draft = cardEngine.updateDraftLocation(
                draft = command.draft,
                location = command.location,
            )
        )
    }
}
