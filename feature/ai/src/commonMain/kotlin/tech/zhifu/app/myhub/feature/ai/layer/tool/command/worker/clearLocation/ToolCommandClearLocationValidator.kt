package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.clearLocation

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandClearLocationValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.ClearLocation
        return ToolCommandValidation(ok = true)
    }
}
