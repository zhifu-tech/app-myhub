package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

sealed interface ActionEvent {
    val raw: String

    data class Option(
        val type: ActionOptionType,
        override val raw: String,
    ) : ActionEvent

    data class AddTag(
        val tag: String,
        override val raw: String,
    ) : ActionEvent

    data class RemoveTag(
        val tag: String,
        override val raw: String,
    ) : ActionEvent

    data class RemoveMediaAt(
        val index: Int,
        override val raw: String,
    ) : ActionEvent

    data class SetCaptureType(
        val type: String,
        override val raw: String,
    ) : ActionEvent

    data class SetLocation(
        val location: String,
        override val raw: String,
    ) : ActionEvent

    data class Unknown(
        override val raw: String,
    ) : ActionEvent
}

fun parseActionEvent(raw: String): ActionEvent {
    ActionOptionType.entries.firstOrNull { it.value == raw }?.let { type ->
        return ActionEvent.Option(type = type, raw = raw)
    }
    ActionOptionType.decodeTagAdd(raw)?.let { tag ->
        return ActionEvent.AddTag(tag = tag, raw = raw)
    }
    ActionOptionType.decodeTagRemove(raw)?.let { tag ->
        return ActionEvent.RemoveTag(tag = tag, raw = raw)
    }
    ActionOptionType.decodeRemoveMediaAt(raw)?.let { index ->
        return ActionEvent.RemoveMediaAt(index = index, raw = raw)
    }
    ActionOptionType.decodeSetCaptureType(raw)?.let { type ->
        return ActionEvent.SetCaptureType(type = type, raw = raw)
    }
    ActionOptionType.decodeSetLocation(raw)?.let { location ->
        return ActionEvent.SetLocation(location = location, raw = raw)
    }
    return ActionEvent.Unknown(raw = raw)
}
