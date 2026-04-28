package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import org.jetbrains.compose.resources.StringResource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_capture_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_clear_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_delete_card
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_generate_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_new_capture
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_edit_empty
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_edit_media_value
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_edit_tags_value
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_publish
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_remove_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_replace_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_save_draft
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_upload_media

class ActionPlanner {
    fun actionsFor(
        state: ConversationState,
        draft: CaptureDraft,
        focusField: Field? = null,
        mediaGenerationProgress: ProviderImageGenerationProgress? = null,
    ): List<ActionComponentSchema> = when (state) {
        ConversationState.INFO_COLLECT -> infoCollectActions(
            draft = draft,
            focusField = focusField,
            mediaGenerationProgress = mediaGenerationProgress,
        )

        ConversationState.CARD_REVIEW -> reviewActions(draft = draft)

        ConversationState.MANUAL_EDIT -> manualEditActions(
            draft = draft,
            focusField = focusField,
            mediaGenerationProgress = mediaGenerationProgress,
        )

        ConversationState.COMPLETE -> listOf(
            ActionComponentSchema(
                kind = ActionComponentKind.QUICK_REPLY,
                actions = ActionSlots(
                    primary = listOf(
                        item(
                            id = "new-capture",
                            command = ActionCommand.NewCapture,
                            label = text(Res.string.feature_ai_action_new_capture),
                            style = ActionStyle.Primary,
                        )
                    )
                ),
            )
        )

        else -> emptyList()
    }
}

private fun infoCollectActions(
    draft: CaptureDraft,
    focusField: Field?,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
): List<ActionComponentSchema> = when (focusField) {
    Field.MEDIA -> listOf(
        mediaComponent(
            draft = draft,
            mediaGenerationProgress = mediaGenerationProgress,
            reviewMode = false
        )
    )

    Field.TAGS -> listOf(
        tagComponent(
            draft = draft,
            reviewMode = false
        )
    )

    Field.TITLE -> listOf(
        inputComponent(
            draft = draft,
            field = Field.TITLE,
            reviewMode = false
        )
    )

    Field.SUMMARY -> listOf(
        inputComponent(
            draft = draft,
            field = Field.SUMMARY,
            reviewMode = false
        )
    )

    Field.LOCATION -> listOf(
        locationComponent(
            draft = draft,
            reviewMode = false
        )
    )

    else -> emptyList()
}

private fun reviewActions(
    draft: CaptureDraft,
): List<ActionComponentSchema> = listOf(
    ActionComponentSchema(
        kind = ActionComponentKind.REVIEW,
        payload = ActionPayload.Review(
            fields = listOf(
                reviewFieldItem(
                    id = "edit-title",
                    command = ActionCommand.EditTitle,
                    label = text(Res.string.feature_ai_action_edit_title),
                    value = fieldValueText(ActionCommand.EditTitle, draft),
                ),
                reviewFieldItem(
                    id = "edit-media",
                    command = ActionCommand.EditMedia,
                    label = text(Res.string.feature_ai_action_edit_media),
                    value = fieldValueText(ActionCommand.EditMedia, draft),
                ),
                reviewFieldItem(
                    id = "edit-tags",
                    command = ActionCommand.EditTags,
                    label = text(Res.string.feature_ai_action_edit_tags),
                    value = fieldValueText(ActionCommand.EditTags, draft),
                ),
                reviewFieldItem(
                    id = "edit-summary",
                    command = ActionCommand.EditSummary,
                    label = text(Res.string.feature_ai_action_edit_summary),
                    value = fieldValueText(ActionCommand.EditSummary, draft),
                ),
                reviewFieldItem(
                    id = "edit-location",
                    command = ActionCommand.EditLocation,
                    label = text(Res.string.feature_ai_action_edit_location),
                    value = fieldValueText(ActionCommand.EditLocation, draft),
                ),
            )
        ),
    ),
    ActionComponentSchema(
        kind = ActionComponentKind.PUBLISH,
        actions = ActionSlots(
            primary = listOf(
                item(
                    id = "publish",
                    command = ActionCommand.Publish,
                    label = text(Res.string.feature_ai_action_publish),
                    style = ActionStyle.Primary,
                )
            ),
            secondary = listOf(
                item(
                    id = "save-draft",
                    command = ActionCommand.SaveDraft,
                    label = text(Res.string.feature_ai_action_save_draft),
                    style = ActionStyle.Tonal,
                ),
                item(
                    id = "delete-card",
                    command = ActionCommand.DeleteCard,
                    label = text(Res.string.feature_ai_action_delete_card),
                    style = ActionStyle.Destructive,
                ),
            )
        ),
    )
)

private fun manualEditActions(
    draft: CaptureDraft,
    focusField: Field?,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
): List<ActionComponentSchema> = when (focusField) {
    Field.MEDIA -> listOf(mediaComponent(draft, mediaGenerationProgress, reviewMode = true))
    Field.TAGS -> listOf(tagComponent(draft, reviewMode = true))
    Field.SUMMARY -> listOf(inputComponent(draft, field = Field.SUMMARY, reviewMode = true))
    Field.LOCATION -> listOf(locationComponent(draft, reviewMode = true))
    else -> listOf(inputComponent(draft, field = Field.TITLE, reviewMode = true))
}

private fun mediaComponent(
    draft: CaptureDraft,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
    reviewMode: Boolean,
): ActionComponentSchema {
    val hasMedia = draft.mediaAssets.isNotEmpty()
    val isGenerating = mediaGenerationProgress != null
    return ActionComponentSchema(
        kind = ActionComponentKind.MEDIA,
        payload = ActionPayload.Media(
            hasMedia = hasMedia,
            mediaCount = draft.mediaAssets.size,
            missingMediaCount = draft.mediaAssets.count { it.isMissing },
            isGenerating = isGenerating,
        ),
        actions = ActionSlots(
            primary = buildList {
                add(
                    item(
                        id = "capture-media",
                        command = ActionCommand.CaptureMedia,
                        label = text(Res.string.feature_ai_action_capture_media),
                        style = if (hasMedia) {
                            ActionStyle.Secondary
                        } else {
                            ActionStyle.Primary
                        },
                        enabled = !isGenerating,
                    )
                )
                add(
                    item(
                        id = if (hasMedia) "replace-media" else "upload-media",
                        command = if (hasMedia) ActionCommand.ReplaceMedia else ActionCommand.UploadMedia,
                        label = text(
                            if (hasMedia) {
                                Res.string.feature_ai_action_replace_media
                            } else {
                                Res.string.feature_ai_action_upload_media
                            }
                        ),
                        style = ActionStyle.Secondary,
                        enabled = !isGenerating || !hasMedia,
                    )
                )
                add(
                    item(
                        id = "generate-media",
                        command = ActionCommand.GenerateMedia,
                        label = text(Res.string.feature_ai_action_generate_media),
                        style = if (hasMedia) {
                            ActionStyle.Tonal
                        } else {
                            ActionStyle.Primary
                        },
                        enabled = !isGenerating,
                    )
                )
            },
            secondary = buildList {
                if (hasMedia) {
                    add(
                        item(
                            id = "remove-media",
                            command = ActionCommand.RemoveMedia,
                            label = text(Res.string.feature_ai_action_remove_media),
                            style = ActionStyle.Destructive,
                        )
                    )
                }
                add(
                    if (reviewMode) {
                        item(
                            id = "review",
                            command = ActionCommand.Review,
                            label = text(Res.string.feature_ai_action_review),
                            style = ActionStyle.Tonal,
                        )
                    } else {
                        item(
                            id = "skip-media",
                            command = ActionCommand.SkipMedia,
                            label = text(Res.string.feature_ai_action_skip_media),
                            style = ActionStyle.Secondary,
                        )
                    }
                )
            },
            overlay = draft.mediaAssets.indices.map { index ->
                item(
                    id = "remove-media-$index",
                    command = ActionCommand.RemoveMediaAt(index),
                    label = text(Res.string.feature_ai_action_remove_media),
                    style = ActionStyle.Overlay,
                )
            }
        ),
    )
}

private fun tagComponent(
    draft: CaptureDraft,
    reviewMode: Boolean,
): ActionComponentSchema = ActionComponentSchema(
    kind = ActionComponentKind.TAGS,
    payload = ActionPayload.Tags(selectedCount = draft.tags.size),
    actions = ActionSlots(
        inline = buildTagActions(draft),
        secondary = listOf(
            if (reviewMode) {
                item(
                    id = "review",
                    command = ActionCommand.Review,
                    label = text(Res.string.feature_ai_action_review),
                    style = ActionStyle.Tonal,
                )
            } else {
                item(
                    id = "skip-tags",
                    command = ActionCommand.SkipTags,
                    label = text(Res.string.feature_ai_action_skip_tags),
                    style = ActionStyle.Secondary,
                )
            }
        )
    ),
)

private fun inputComponent(
    draft: CaptureDraft,
    field: Field,
    reviewMode: Boolean,
): ActionComponentSchema = ActionComponentSchema(
    kind = ActionComponentKind.INPUT,
    payload = ActionPayload.Input(
        field = field,
        currentValue = when (field) {
            Field.SUMMARY -> draft.summary
            Field.LOCATION -> draft.location?.name.orEmpty()
            else -> draft.title
        }
    ),
    actions = ActionSlots(
        secondary = if (reviewMode) {
            listOf(
                item(
                    id = "review",
                    command = ActionCommand.Review,
                    label = text(Res.string.feature_ai_action_review),
                    style = ActionStyle.Tonal,
                )
            )
        } else {
            emptyList()
        }
    ),
)

private fun locationComponent(
    draft: CaptureDraft,
    reviewMode: Boolean,
): ActionComponentSchema = ActionComponentSchema(
    kind = ActionComponentKind.LOCATION,
    payload = ActionPayload.Location(
        currentLocation = draft.location?.name.orEmpty(),
    ),
    actions = ActionSlots(
        inline = listOf(
            locationExample("city", "城市：上海市"),
            locationExample("district", "区域：浦东新区"),
            locationExample("store", "地点：星巴克世纪汇店"),
        ),
        secondary = buildList {
            if (draft.location?.name.isNullOrBlank().not()) {
                add(
                    item(
                        id = "clear-location",
                        command = ActionCommand.ClearLocation,
                        label = text(Res.string.feature_ai_action_clear_location),
                        style = ActionStyle.Secondary,
                    )
                )
            }
            if (reviewMode) {
                add(
                    item(
                        id = "review",
                        command = ActionCommand.Review,
                        label = text(Res.string.feature_ai_action_review),
                        style = ActionStyle.Tonal,
                    )
                )
            }
        }
    ),
)

private fun reviewFieldItem(
    id: String,
    command: ActionCommand,
    label: ActionText,
    value: ActionText,
): ReviewFieldItem = ReviewFieldItem(
    id = id,
    action = item(
        id = id,
        command = command,
        label = label,
        style = ActionStyle.Secondary,
    ),
    value = value,
)

private fun buildTagActions(
    draft: CaptureDraft,
): List<ActionItemSchema> = buildList {
    addAll(listOf("灵感", "待办", "工作", "生活", "美食", "餐厅"))
    addAll(draft.tags)
}
    .asSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()
    .take(12)
    .map { tag ->
        item(
            id = "tag-$tag",
            command = ActionCommand.AddTag(tag),
            label = ActionText.Plain(tag),
            style = if (tag in draft.tags) {
                ActionStyle.SelectedChip
            } else {
                ActionStyle.Chip
            },
        )
    }
    .toList()

private fun fieldValueText(
    command: ActionCommand,
    draft: CaptureDraft,
): ActionText = when (command) {
    ActionCommand.EditTitle -> draft.title.ifBlankText()
    ActionCommand.EditSummary -> draft.summary.ifBlank {
        draft.sourceText
    }.ifBlankText()

    ActionCommand.EditLocation -> draft.location?.name.orEmpty().ifBlankText()
    ActionCommand.EditMedia -> if (draft.mediaAssets.isEmpty()) {
        text(Res.string.feature_ai_action_panel_edit_empty)
    } else {
        text(Res.string.feature_ai_action_panel_edit_media_value, draft.mediaAssets.size)
    }

    ActionCommand.EditTags -> if (draft.tags.isEmpty()) {
        text(Res.string.feature_ai_action_panel_edit_empty)
    } else {
        text(
            Res.string.feature_ai_action_panel_edit_tags_value,
            draft.tags.take(2).joinToString(" / "),
        )
    }

    else -> text(Res.string.feature_ai_action_panel_edit_empty)
}

private fun locationExample(
    id: String,
    value: String,
): ActionItemSchema = item(
    id = "location-example-$id",
    command = ActionCommand.SetLocation(
        value.substringAfter('：')
            .substringAfter(":")
            .ifBlank { value }
    ),
    label = ActionText.Plain(value),
    style = ActionStyle.InlineSuggestion,
)

private fun item(
    id: String,
    command: ActionCommand,
    label: ActionText,
    style: ActionStyle,
    enabled: Boolean = true,
) = ActionItemSchema(
    id = id,
    command = command,
    label = label,
    style = style,
    enabled = enabled,
)

private fun String.ifBlankText(): ActionText = if (isBlank()) {
    text(Res.string.feature_ai_action_panel_edit_empty)
} else {
    ActionText.Plain(this)
}

fun text(
    resource: StringResource,
    vararg formatArgs: Any,
): ActionText = ActionText.Resource(
    resource = resource,
    formatArgs = formatArgs.toList(),
)
