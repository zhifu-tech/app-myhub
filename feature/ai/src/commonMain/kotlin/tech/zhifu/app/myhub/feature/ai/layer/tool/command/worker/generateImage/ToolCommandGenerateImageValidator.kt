package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.generateImage

import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidation
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandValidator

class ToolCommandGenerateImageValidator : ToolCommandValidator {
    override fun validate(command: ToolCommand): ToolCommandValidation {
        command as ToolCommand.GenerateImage
        return if (!CaptureImagePromptBuilder.hasSemanticSeed(command.draft)) {
            ToolCommandValidation(ok = false, message = "image_generation_prompt_insufficient")
        } else {
            ToolCommandValidation(ok = true)
        }
    }
}
