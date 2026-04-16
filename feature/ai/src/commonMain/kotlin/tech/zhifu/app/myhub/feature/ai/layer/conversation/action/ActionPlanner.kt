package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureType
import tech.zhifu.app.myhub.feature.ai.model.Field

class ActionPlanner {
    fun actionsFor(
        state: ConversationState,
        draft: CaptureDraft? = null,
        missingFields: List<Field> = emptyList(),
        focusField: Field? = null,
    ): List<ActionComponentSchema> = when (state) {
        ConversationState.INFO_COLLECT -> infoCollectActions(
            draft = draft,
            missingFields = missingFields,
            focusField = focusField,
        )

        ConversationState.CARD_REVIEW -> reviewActions(
            draft = draft,
        )

        ConversationState.MANUAL_EDIT -> manualEditActions(
            draft = draft,
            focusField = focusField,
        )

        ConversationState.COMPLETE -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.QUICK_REPLY,
                options = listOf(ActionOptionSchema.of(ActionOptionType.NEW_CAPTURE)),
            )
        )

        else -> emptyList()
    }

}


private fun infoCollectActions(
    draft: CaptureDraft?,
    missingFields: List<Field>,
    focusField: Field?,
): List<ActionComponentSchema> {
    val field = focusField ?: missingFields.firstOrNull()
    return when (field) {
        Field.MEDIA -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.UPLOAD,
                field = Field.MEDIA,
                status = ActionComponentStatus.ACTIVE,
                options = listOf(ActionOptionSchema.of(ActionOptionType.UPLOAD_MEDIA)),
            ),
            ActionComponentSchema(
                type = ActionComponentType.QUICK_REPLY,
                field = Field.MEDIA,
                status = ActionComponentStatus.ACTIVE,
                options = listOf(ActionOptionSchema.of(ActionOptionType.SKIP_MEDIA)),
            )
        )

        Field.TAGS -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.TAG_SELECTOR,
                field = Field.TAGS,
                status = ActionComponentStatus.ACTIVE,
                options = buildTagOptions(draft = draft),
            ),
            ActionComponentSchema(
                type = ActionComponentType.QUICK_REPLY,
                field = Field.TAGS,
                status = ActionComponentStatus.ACTIVE,
                options = listOf(ActionOptionSchema.of(ActionOptionType.SKIP_TAGS)),
            )
        )

        Field.TITLE -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.INPUT,
                field = Field.TITLE,
                status = ActionComponentStatus.ACTIVE,
                options = listOf(ActionOptionSchema.of(ActionOptionType.INPUT_TITLE_HINT)),
            )
        )

        Field.SUMMARY -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.INPUT,
                field = Field.SUMMARY,
                status = ActionComponentStatus.ACTIVE,
                options = listOf(ActionOptionSchema.of(ActionOptionType.EDIT_SUMMARY)),
            )
        )

        else -> emptyList()
    }
}


private fun reviewActions(
    @Suppress("UNUSED_PARAMETER")
    draft: CaptureDraft?,
): List<ActionComponentSchema> {
    return listOf(
        ActionComponentSchema(
            type = ActionComponentType.CARD_PREVIEW,
            status = ActionComponentStatus.ACTIVE,
        ),
        ActionComponentSchema(
            type = ActionComponentType.FIELD_EDITOR,
            status = ActionComponentStatus.ACTIVE,
            options = listOf(
                ActionOptionSchema.of(ActionOptionType.EDIT_TITLE),
                ActionOptionSchema.of(ActionOptionType.EDIT_MEDIA),
                ActionOptionSchema.of(ActionOptionType.EDIT_TAGS),
                ActionOptionSchema.of(ActionOptionType.EDIT_SUMMARY),
                ActionOptionSchema.of(ActionOptionType.EDIT_LOCATION),
            ),
        ),
        ActionComponentSchema(
            type = ActionComponentType.CARD_ACTIONS,
            status = ActionComponentStatus.ACTIVE,
            options = listOf(
                ActionOptionSchema.of(ActionOptionType.SAVE_DRAFT),
                ActionOptionSchema.of(ActionOptionType.DELETE_CARD),
                ActionOptionSchema.of(ActionOptionType.PUBLISH),
            )
        ),
    )
}


private fun manualEditActions(
    draft: CaptureDraft?,
    focusField: Field?,
): List<ActionComponentSchema> {
    val field = focusField ?: Field.TITLE
    val editor = when (field) {
        Field.MEDIA -> ActionComponentSchema(
            type = ActionComponentType.UPLOAD,
            field = Field.MEDIA,
            status = ActionComponentStatus.ACTIVE,
            options = listOf(ActionOptionSchema.of(ActionOptionType.UPLOAD_MEDIA)),
        )

        Field.TAGS -> ActionComponentSchema(
            type = ActionComponentType.TAG_SELECTOR,
            field = Field.TAGS,
            status = ActionComponentStatus.ACTIVE,
            options = buildTagOptions(draft = draft),
        )

        Field.SUMMARY -> ActionComponentSchema(
            type = ActionComponentType.INPUT,
            field = Field.SUMMARY,
            status = ActionComponentStatus.ACTIVE,
            options = listOf(ActionOptionSchema.of(ActionOptionType.EDIT_SUMMARY)),
        )

        Field.LOCATION -> ActionComponentSchema(
            type = ActionComponentType.INPUT,
            field = Field.LOCATION,
            status = ActionComponentStatus.ACTIVE,
            options = listOf(ActionOptionSchema.of(ActionOptionType.EDIT_LOCATION)),
        )

        else -> ActionComponentSchema(
            type = ActionComponentType.INPUT,
            field = Field.TITLE,
            status = ActionComponentStatus.ACTIVE,
            options = listOf(ActionOptionSchema.of(ActionOptionType.INPUT_TITLE_HINT)),
        )
    }
    return listOf(
        editor,
        ActionComponentSchema(
            type = ActionComponentType.QUICK_REPLY,
            status = ActionComponentStatus.ACTIVE,
            options = listOf(ActionOptionSchema.of(ActionOptionType.REVIEW)),
        ),
//        ActionComponentSchema(
//            type = ActionComponentType.CARD_ACTIONS,
//            status = ActionComponentStatus.ACTIVE,
//            options = listOf(
//                ActionOptionSchema.of(ActionOptionType.SAVE_DRAFT),
//                ActionOptionSchema.of(ActionOptionType.DELETE_CARD),
//                ActionOptionSchema.of(ActionOptionType.PUBLISH),
//            )
//        ),
    )
}


private fun orderedDraftActions(
    currentField: Field?,
): List<ActionComponentSchema> {
    val order = listOf(
        ActionOptionType.EDIT_TITLE to Field.TITLE,
        ActionOptionType.EDIT_MEDIA to Field.MEDIA,
        ActionOptionType.EDIT_TAGS to Field.TAGS,
        ActionOptionType.EDIT_SUMMARY to Field.SUMMARY,
        ActionOptionType.EDIT_LOCATION to Field.LOCATION,
    )
    return listOf(
        ActionComponentSchema(
            type = ActionComponentType.CARD_ACTIONS,
            status = ActionComponentStatus.ACTIVE,
            options = buildList {
                order.forEach { (type, field) ->
                    add(
                        ActionOptionSchema.of(
                            type = type,
                            selected = currentField == field,
                        )
                    )
                }
                add(ActionOptionSchema.of(ActionOptionType.PUBLISH))
            }
        )
    )
}


private fun buildOptionGrid(
    draft: CaptureDraft?,
): List<ActionOptionSchema> {
    val selected = draft?.captureType
    return CaptureType.entries.map { type ->
        ActionOptionSchema(
            type = ActionOptionType.SET_CAPTURE_TYPE,
            label = type.value,
            value = ActionOptionType.encodeSetCaptureType(type.value),
            selected = selected == type,
        )
    }
}

private fun buildLocationOptions(
    draft: CaptureDraft?,
): List<ActionOptionSchema> {
    val selected = draft?.location?.name.orEmpty()
    val presets = listOf("Home", "Office", "Shanghai", "Hangzhou")
    val presetOptions = presets.map { name ->
        ActionOptionSchema(
            type = ActionOptionType.SET_LOCATION,
            label = name,
            value = ActionOptionType.encodeSetLocation(name),
            selected = selected.equals(name, ignoreCase = true),
        )
    }
    val selectedOption = selected
        .takeIf { it.isNotBlank() && presets.none { preset -> preset.equals(it, ignoreCase = true) } }
        ?.let { custom ->
            ActionOptionSchema(
                type = ActionOptionType.SET_LOCATION,
                label = custom,
                value = ActionOptionType.encodeSetLocation(custom),
                selected = true,
            )
        }
    return buildList {
        add(ActionOptionSchema.of(ActionOptionType.EDIT_LOCATION))
        addAll(presetOptions)
        if (selectedOption != null) add(selectedOption)
        if (selected.isNotBlank()) add(ActionOptionSchema.of(ActionOptionType.CLEAR_LOCATION))
    }
}

private fun buildTagOptions(
    draft: CaptureDraft?
): List<ActionOptionSchema> {
    val selected = draft?.tags.orEmpty().toSet()
    val seeds = buildList {
        addAll(listOf("food", "idea", "todo", "work", "life"))
        addAll(draft?.tags.orEmpty())
    }
    return seeds
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(12)
        .map { tag ->
            if (tag in selected) {
                ActionOptionSchema(
                    type = ActionOptionType.REMOVE_TAG,
                    label = tag,
                    value = ActionOptionType.encodeTagRemove(tag),
                )
            } else {
                ActionOptionSchema.tag(tag)
            }
        }
}
