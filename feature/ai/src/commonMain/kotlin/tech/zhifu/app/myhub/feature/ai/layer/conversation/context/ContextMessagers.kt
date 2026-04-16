package tech.zhifu.app.myhub.feature.ai.layer.conversation.context

import org.jetbrains.compose.resources.StringResource
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
) = Message(
    id = nextMessageId(),
    role = role,
    text = text,
    textRes = textRes,
    textArgs = textArgs,
)
