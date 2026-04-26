package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.captureMediaPhoto

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandCaptureMediaPhotoValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.CaptureMediaPhoto
        return ToolCommandValidation(ok = true)
    }
}
