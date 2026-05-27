package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateTitle

import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult

class ToolCommandUpdateTitleExecutor(
    private val cardEngine: CardEngine,
) : ToolCommandExecutor {

    override suspend fun execute(command: ToolCommand): ToolCommandResult {
        val command = command as ToolCommand.UpdateTitle
        return ToolCommandResult.DraftUpdated(
            draft = cardEngine.updateDraftTitle(
                draft = command.draft,
                title = command.title,
            )
        )
    }
}
