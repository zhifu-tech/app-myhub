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
            selected: Boolean = false,
        ): ActionOptionSchema = ActionOptionSchema(
            type = type,
            label = type.value,
            value = type.value,
            selected = selected,
        )

        fun tag(label: String): ActionOptionSchema = ActionOptionSchema(
            type = ActionOptionType.TAG,
            label = label,
            value = ActionOptionType.encodeTagAdd(label),
        )
    }
}

@Serializable
enum class ActionComponentStatus {
    ACTIVE,
    COMPLETED,
}
