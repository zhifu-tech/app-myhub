package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.feature.ai.model.Field

@Serializable
data class ActionComponentSchema(
    val type: ActionComponentType,
    val field: Field? = null,
    val options: List<ActionOptionSchema> = emptyList(),
)

@Serializable
enum class ActionComponentType {
    @SerialName("quick_reply")
    QUICK_REPLY,

    @SerialName("tag_selector")
    TAG_SELECTOR,

    @SerialName("upload")
    UPLOAD,

    @SerialName("input")
    INPUT,

    @SerialName("card_actions")
    CARD_ACTIONS,
}

@Serializable
data class ActionOptionSchema(
    val label: String,
    val value: String,
)
