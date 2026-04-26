package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateTitle

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandUpdateTitleValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.UpdateTitle
        return if (command.title.trim().isBlank()) {
            ToolCommandValidation(ok = false, message = "title_blank")
        } else {
            ToolCommandValidation(ok = true)
        }
    }
}
