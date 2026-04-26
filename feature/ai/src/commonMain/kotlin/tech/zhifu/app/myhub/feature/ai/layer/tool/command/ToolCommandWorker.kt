package tech.zhifu.app.myhub.feature.ai.layer.tool.command

import kotlin.reflect.KClass

class ToolCommandWorker(
    val commandClass: KClass<out ToolCommand>,
    val validator: ToolCommandValidator,
    executorProvider: () -> ToolCommandExecutor,
) {
    val commandClassName: String = requireNotNull(commandClass.qualifiedName) {
        "tool_command_class_name_missing"
    }

    val executor by lazy(LazyThreadSafetyMode.NONE) { executorProvider() }
}
