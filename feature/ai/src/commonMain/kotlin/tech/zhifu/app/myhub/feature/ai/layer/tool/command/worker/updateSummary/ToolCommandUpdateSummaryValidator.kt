package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.updateSummary

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandUpdateSummaryValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.UpdateSummary
        return if (command.summary.trim().isBlank()) {
            ToolCommandValidation(ok = false, message = "summary_blank")
        } else {
            ToolCommandValidation(ok = true)
        }
    }
}
