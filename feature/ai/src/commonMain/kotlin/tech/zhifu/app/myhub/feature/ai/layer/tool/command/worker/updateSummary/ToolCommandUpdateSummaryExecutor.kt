package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateSummary

import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult

class ToolCommandUpdateSummaryExecutor(
    private val cardEngine: CardEngine,
) : ToolCommandExecutor {
    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val command = command as ToolCommand.UpdateSummary
        return ToolCommandResult.DraftUpdated(
            draft = cardEngine.updateDraftSummary(
                draft = command.draft,
                summary = command.summary,
            )
        )
    }
}
