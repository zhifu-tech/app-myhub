package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field

class ActionPlanner {
    fun actionsFor(
        state: ConversationState,
        draft: CaptureDraft? = null,
        missingFields: List<Field> = emptyList(),
    ): List<ActionComponentSchema> = when (state) {
        ConversationState.INFO_COLLECT -> infoCollectActions(
            draft = draft,
            missingFields = missingFields,
        )

        ConversationState.CARD_REVIEW -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.CARD_ACTIONS,
                options = listOf(
                    ActionOptionSchema(
                        label = "edit_title",
                        value = "edit_title"
                    ),
                    ActionOptionSchema(
                        label = "publish",
                        value = "publish"
                    ),
                )
            )
        )

        ConversationState.MANUAL_EDIT -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.INPUT,
                field = Field.TITLE,
                options = listOf(
                    ActionOptionSchema(
                        label = "input_title_hint",
                        value = "input_title_hint"
                    ),
                )
            ),
            ActionComponentSchema(
                type = ActionComponentType.CARD_ACTIONS,
                options = listOf(
                    ActionOptionSchema(
                        label = "edit_title",
                        value = "edit_title"
                    ),
                    ActionOptionSchema(
                        label = "publish",
                        value = "publish"
                    ),
                )
            )
        )

        ConversationState.COMPLETE -> listOf(
            ActionComponentSchema(
                type = ActionComponentType.QUICK_REPLY,
                options = listOf(
                    ActionOptionSchema(
                        label = "new_capture",
                        value = "new_capture"
                    ),
                )
            )
        )

        else -> emptyList()
    }

    private fun infoCollectActions(
        draft: CaptureDraft?,
        missingFields: List<Field>,
    ): List<ActionComponentSchema> {
        return when (missingFields.firstOrNull()) {
            Field.MEDIA -> listOf(
                ActionComponentSchema(
                    type = ActionComponentType.UPLOAD,
                    field = Field.MEDIA,
                    options = listOf(
                        ActionOptionSchema(
                            label = "upload_media",
                            value = "upload_media"
                        ),
                    )
                ),
                ActionComponentSchema(
                    type = ActionComponentType.QUICK_REPLY,
                    field = Field.MEDIA,
                    options = listOf(
                        ActionOptionSchema(
                            label = "skip_media",
                            value = "skip_media"
                        ),
                        ActionOptionSchema(
                            label = "review",
                            value = "review"
                        ),
                    )
                )
            )

            Field.TAGS -> listOf(
                ActionComponentSchema(
                    type = ActionComponentType.TAG_SELECTOR,
                    field = Field.TAGS,
                    options = buildTagOptions(draft)
                ),
                ActionComponentSchema(
                    type = ActionComponentType.QUICK_REPLY,
                    field = Field.TAGS,
                    options = listOf(
                        ActionOptionSchema(
                            label = "skip_tags",
                            value = "skip_tags"
                        ),
                        ActionOptionSchema(
                            label = "review",
                            value = "review"
                        ),
                    )
                )
            )

            Field.TITLE -> listOf(
                ActionComponentSchema(
                    type = ActionComponentType.INPUT,
                    field = Field.TITLE,
                    options = listOf(
                        ActionOptionSchema(
                            label = "input_title_hint",
                            value = "input_title_hint"
                        )
                    )
                ),
                ActionComponentSchema(
                    type = ActionComponentType.QUICK_REPLY,
                    field = Field.TITLE,
                    options = listOf(
                        ActionOptionSchema(
                            label = "review",
                            value = "review"
                        )
                    )
                )
            )

            else -> listOf(
                ActionComponentSchema(
                    type = ActionComponentType.QUICK_REPLY,
                    options = listOf(
                        ActionOptionSchema(
                            label = "review",
                            value = "review"
                        )
                    )
                )
            )
        }
    }
}

private fun buildTagOptions(
    draft: CaptureDraft?
): List<ActionOptionSchema> {
    val options = draft?.tags
        ?.take(5)
        ?.map { ActionOptionSchema(label = it, value = "tag:$it") }
        .orEmpty()
    if (options.isNotEmpty()) return options
    return listOf(
        ActionOptionSchema(label = "food", value = "tag:food"),
        ActionOptionSchema(label = "idea", value = "tag:idea"),
        ActionOptionSchema(label = "todo", value = "tag:todo"),
    )
}
