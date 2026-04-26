package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.publishCard

import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.storage.StorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult

class ToolCommandPublishCardExecutor(
    private val cardEngine: CardEngine,
    private val storageGateway: StorageGateway,
) : ToolCommandExecutor {
    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val command = command as ToolCommand.PublishCard
        val card = cardEngine.toPublishedCard(
            draft = command.draft
        )
        storageGateway.savePublishedCard(
            card = card,
            draft = command.draft
        )
        return ToolCommandResult.Published(
            cardId = card.id,
            title = card.title,
        )
    }
}
