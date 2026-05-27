package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandAttachPickedMediaValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.AttachPickedMedia
        return if (command.maxItems <= 0) {
            ToolCommandValidation(ok = false, message = "max_items_invalid")
        } else {
            ToolCommandValidation(ok = true)
        }
    }
}
