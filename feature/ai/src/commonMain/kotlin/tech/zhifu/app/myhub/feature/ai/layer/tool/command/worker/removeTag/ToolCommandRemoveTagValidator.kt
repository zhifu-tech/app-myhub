package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.removeTag

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandRemoveTagValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.RemoveTag
        return if (command.tag.trim().isBlank()) {
            ToolCommandValidation(ok = false, message = "tag_blank")
        } else {
            ToolCommandValidation(ok = true)
        }
    }
}
