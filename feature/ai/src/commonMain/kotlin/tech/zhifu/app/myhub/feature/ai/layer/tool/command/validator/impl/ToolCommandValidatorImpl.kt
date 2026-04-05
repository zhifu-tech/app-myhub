package tech.zhifu.app.myhub.feature.ai.layer.tool.command.validator.impl

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ValidationResult

class ToolCommandValidatorImpl : ToolCommandValidator {

    override fun validate(
        command: ToolCommand
    ): ValidationResult {
        return when (command) {
            is ToolCommand.UpdateTitle -> {
                if (command.title.trim().isBlank()) {
                    ValidationResult(
                        ok = false,
                        message = "title_blank"
                    )
                } else {
                    ValidationResult(
                        ok = true
                    )
                }
            }

            is ToolCommand.AddTag -> {
                if (command.tag.trim().isBlank()) {
                    ValidationResult(
                        ok = false,
                        message = "tag_blank"
                    )
                } else {
                    ValidationResult(
                        ok = true
                    )
                }
            }

            is ToolCommand.PublishCard -> {
                ValidationResult(
                    ok = true
                )
            }

            is ToolCommand.AttachPickedMedia -> {
                if (command.maxItems <= 0) {
                    ValidationResult(
                        ok = false,
                        message = "max_items_invalid"
                    )
                } else {
                    ValidationResult(
                        ok = true
                    )
                }
            }
        }
    }
}
