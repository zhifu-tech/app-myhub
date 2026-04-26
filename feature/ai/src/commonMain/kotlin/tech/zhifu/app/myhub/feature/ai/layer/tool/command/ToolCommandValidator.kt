package tech.zhifu.app.myhub.feature.ai.layer.tool.command

fun interface ToolCommandValidator {
    fun validate(command: ToolCommand): ToolCommandValidation
}

data class ToolCommandValidation(
    val ok: Boolean,
    val message: String? = null,
)
