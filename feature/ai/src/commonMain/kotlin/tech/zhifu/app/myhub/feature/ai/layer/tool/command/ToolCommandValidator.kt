package tech.zhifu.app.myhub.feature.ai.layer.tool.command

fun interface ToolCommandValidator {
    fun validate(
        command: ToolCommand
    ): ValidationResult
}

data class ValidationResult(
    val ok: Boolean,
    val message: String? = null,
)
