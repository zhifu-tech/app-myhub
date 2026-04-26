package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.addTag

import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult

class ToolCommandAddTagExecutor(
    private val cardEngine: CardEngine,
) : ToolCommandExecutor {

    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        command as ToolCommand.AddTag
        return ToolCommandResult.DraftUpdated(
            draft = cardEngine.appendTag(
                draft = command.draft,
                tag = command.tag,
            )
        )
    }
}
