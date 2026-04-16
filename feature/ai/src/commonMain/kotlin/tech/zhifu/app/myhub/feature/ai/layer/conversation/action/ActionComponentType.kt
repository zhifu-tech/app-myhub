package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

    @SerialName("option_grid")
    OPTION_GRID,

    @SerialName("location_picker")
    LOCATION_PICKER,

    @SerialName("card_preview")
    CARD_PREVIEW,

    @SerialName("field_editor")
    FIELD_EDITOR,
}
