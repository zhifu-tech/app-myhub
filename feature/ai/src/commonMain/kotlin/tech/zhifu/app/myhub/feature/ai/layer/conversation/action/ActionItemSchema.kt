package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import org.jetbrains.compose.resources.StringResource

data class ActionItemSchema(
    val id: String,
    val command: ActionCommand,
    val label: ActionText,
    val style: ActionStyle,
    val enabled: Boolean = true,
)

enum class ActionStyle {
    Primary,
    Secondary,
    Tonal,
    Destructive,
    Chip,
    SelectedChip,
    InlineSuggestion,
    Overlay,
}

sealed interface ActionText {
    data class Resource(
        val resource: StringResource,
        val formatArgs: List<Any> = emptyList(),
    ) : ActionText

    data class Plain(
        val value: String,
    ) : ActionText
}
