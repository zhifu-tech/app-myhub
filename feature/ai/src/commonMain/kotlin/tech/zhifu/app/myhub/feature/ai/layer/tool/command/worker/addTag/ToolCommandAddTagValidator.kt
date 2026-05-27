package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.addTag

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandAddTagValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.AddTag
        return if (command.tag.trim().isBlank()) {
            ToolCommandValidation(ok = false, message = "tag_blank")
        } else {
            ToolCommandValidation(ok = true)
        }
    }
}
