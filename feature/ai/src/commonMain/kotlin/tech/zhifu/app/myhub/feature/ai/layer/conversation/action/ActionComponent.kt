package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import tech.zhifu.app.myhub.feature.ai.model.Field

data class ActionComponent(
    val kind: ActionComponentKind,
    val payload: ActionPayload = ActionPayload.None,
    val actions: ActionSlots = ActionSlots(),
)

enum class ActionComponentKind {
    REVIEW,
    PUBLISH,
    MEDIA,
    TAGS,
    INPUT,
    LOCATION,
    QUICK_REPLY,
}

data class ActionSlots(
    val primary: List<ActionItemSchema> = emptyList(),
    val secondary: List<ActionItemSchema> = emptyList(),
    val inline: List<ActionItemSchema> = emptyList(),
    val overlay: List<ActionItemSchema> = emptyList(),
)

sealed interface ActionPayload {
    data object None : ActionPayload

    data class Review(
        val fields: List<ReviewFieldItem>,
    ) : ActionPayload

    data class Media(
        val hasMedia: Boolean,
        val mediaCount: Int,
        val missingMediaCount: Int,
        val isGenerating: Boolean,
    ) : ActionPayload

    data class Tags(
        val selectedCount: Int,
    ) : ActionPayload

    data class Input(
        val field: Field,
        val currentValue: String,
    ) : ActionPayload

    data class Location(
        val currentLocation: String,
    ) : ActionPayload
}

data class ReviewFieldItem(
    val id: String,
    val action: ActionItemSchema,
    val value: ActionText,
)
