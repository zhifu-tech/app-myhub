package tech.zhifu.app.myhub.feature.ai.layer.conversation.context

import org.jetbrains.compose.resources.StringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponent
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.model.Message

fun ContextManager.appendMessages(
    builderAction: MutableList<Message>.() -> Unit
): List<Message> = buildList {
    addAll(context.messages)
    builderAction(this)
}

fun ContextManager.ofMessage(
    role: Message.Role,
    text: String = "",
    textRes: StringResource? = null,
    textArgs: List<Any> = emptyList(),
    mediaAssets: List<CaptureMediaAsset> = emptyList(),
    editingField: Field? = null,
) = Message(
    id = nextMessageId(),
    role = role,
    text = text,
    textRes = textRes,
    textArgs = textArgs,
    mediaAssets = mediaAssets,
    editingField = editingField,
)

fun List<Message>.replaceMessage(
    messageId: String,
    transform: (Message) -> Message,
): List<Message> = map { message ->
    if (message.id == messageId) {
        transform(message)
    } else {
        message
    }
}

fun List<Message>.bindActionComponents(
    components: List<ActionComponent>,
    ownerMessageId: String?,
): List<Message> = map { message ->
    val nextComponents = if (message.id == ownerMessageId && message.canOwnActionComponents()) {
        components
    } else {
        emptyList()
    }
    if (message.actionComponents == nextComponents) {
        message
    } else {
        message.copy(actionComponents = nextComponents)
    }
}

fun Message.canOwnActionComponents(): Boolean = when (role) {
    Message.Role.AI,
    Message.Role.SYSTEM -> true

    Message.Role.USER,
    Message.Role.THINKING -> false
}
