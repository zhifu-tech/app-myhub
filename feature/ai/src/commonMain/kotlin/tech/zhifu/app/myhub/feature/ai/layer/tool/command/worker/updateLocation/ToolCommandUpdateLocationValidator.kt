package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateLocation

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandUpdateLocationValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.UpdateLocation
        return if (command.location.trim().isBlank()) {
            ToolCommandValidation(ok = false, message = "location_blank")
        } else {
            ToolCommandValidation(ok = true)
        }
    }
}
