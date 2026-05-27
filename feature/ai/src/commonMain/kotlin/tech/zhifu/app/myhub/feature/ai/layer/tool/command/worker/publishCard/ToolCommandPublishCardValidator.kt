package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.publishCard

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandPublishCardValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.PublishCard
        return ToolCommandValidation(ok = true)
    }
}
