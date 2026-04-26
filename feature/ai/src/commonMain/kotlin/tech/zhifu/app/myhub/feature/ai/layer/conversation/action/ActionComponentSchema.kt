package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.feature.ai.model.Field

@Serializable
data class ActionComponentSchema(
    val type: ActionComponentType,
    val field: Field? = null,
    val status: ActionComponentStatus = ActionComponentStatus.ACTIVE,
    val options: List<ActionOptionSchema> = emptyList(),
)

@Serializable
data class ActionOptionSchema(
    val type: ActionOptionType,
    val label: String,
    val value: String,
    val selected: Boolean = false,
) {
    companion object {
        fun of(
            type: ActionOptionType,
            label: String = type.value,
            value: String = type.value,
            selected: Boolean = false,
        ): ActionOptionSchema = ActionOptionSchema(
            type = type,
            label = label,
            value = value,
            selected = selected,
        )
    }
}

@Serializable
enum class ActionComponentStatus {
    ACTIVE,
    COMPLETED,
}
