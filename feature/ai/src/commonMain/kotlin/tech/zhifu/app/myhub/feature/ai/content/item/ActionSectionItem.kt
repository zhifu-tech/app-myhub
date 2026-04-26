package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.component.MediaGalleryDialog
import tech.zhifu.app.myhub.component.media.component.MediaGridNine
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionType
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.model.CaptureType
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
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_input_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_input_title_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_new_capture
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_complete_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_complete_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_current_value
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_edit_empty
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_edit_media_value
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_edit_tags_value
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_current
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_empty_placeholder
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_example_city
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_example_district
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_example_store
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_support_missing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_support_ready
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_title_missing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_title_ready
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_empty_placeholder
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_existing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_missing_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_progress
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_stage_finalizing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_stage_generating
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_stage_preparing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_support_generating
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_support_missing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_support_ready
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_title_generating
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_title_missing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_media_title_ready
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_check_copy
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_check_cover
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_check_meta
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_status
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_support_floating
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_support_pinned
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_title_floating
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_title_pinned
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_publish_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_publish_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_custom_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_title_empty
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_title_selected
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_publish
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_remove_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_replace_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_save_draft
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_upload_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_capture_type_article
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_capture_type_idea
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_capture_type_person
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_capture_type_place

@Composable
fun InlineActionDeck(
    components: List<ActionComponentSchema>,
    draft: CaptureDraft,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
    selectedTags: List<String>,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (components.isEmpty()) return
    val upload = components.firstOrNull { it.type == ActionComponentType.UPLOAD }
    val tagSelector = components.firstOrNull { it.type == ActionComponentType.TAG_SELECTOR }
    val locationPicker = components.firstOrNull { it.type == ActionComponentType.LOCATION_PICKER }
    val input = components.firstOrNull { it.type == ActionComponentType.INPUT }
    val fieldEditor = components.firstOrNull { it.type == ActionComponentType.FIELD_EDITOR }
    val cardActions = components.firstOrNull { it.type == ActionComponentType.CARD_ACTIONS }
    val quickReplies = components
        .filter { it.type == ActionComponentType.QUICK_REPLY }
        .flatMap { it.options }
    val previewVisible = components.any { it.type == ActionComponentType.CARD_PREVIEW }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            previewVisible || fieldEditor != null || cardActions != null -> {
                ReviewActionPanel(
                    previewVisible = previewVisible,
                    draft = draft,
                    options = fieldEditor?.options.orEmpty(),
                    onAction = onAction,
                )
                cardActions?.let { component ->
                    PublishPanel(
                        options = component.options,
                        onAction = onAction,
                    )
                }
            }

            upload != null -> MediaPanel(
                draft = draft,
                mediaGenerationProgress = mediaGenerationProgress,
                options = mergeOptions(upload.options, quickReplies),
                onAction = onAction,
            )

            tagSelector != null -> TagPanel(
                options = mergeOptions(tagSelector.options, quickReplies),
                selectedTags = selectedTags,
                onAction = onAction,
            )

            locationPicker != null -> LocationPanel(
                draft = draft,
                options = mergeOptions(locationPicker.options, quickReplies),
                onAction = onAction,
            )

            input != null -> InputPanel(
                component = input,
                draft = draft,
                quickReplies = quickReplies,
                onAction = onAction,
            )

            quickReplies.isNotEmpty() -> QuickReplyPanel(
                options = quickReplies,
                onAction = onAction,
            )

            else -> GenericActionPanel(
                components = components,
                selectedTags = selectedTags,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun ReviewActionPanel(
    previewVisible: Boolean,
    draft: CaptureDraft,
    options: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
) {
    val previewPinned = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
    ActionSupportPanel(
        eyebrow = if (previewVisible) {
            stringResource(Res.string.feature_ai_action_panel_preview_status)
        } else {
            null
        },
        title = if (previewPinned) {
            stringResource(Res.string.feature_ai_action_panel_preview_title_pinned)
        } else {
            stringResource(Res.string.feature_ai_action_panel_preview_title_floating)
        },
        supporting = if (previewPinned) {
            stringResource(Res.string.feature_ai_action_panel_preview_support_pinned)
        } else {
            stringResource(Res.string.feature_ai_action_panel_preview_support_floating)
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DraftMetaPill(text = stringResource(Res.string.feature_ai_action_panel_preview_check_cover))
            DraftMetaPill(text = stringResource(Res.string.feature_ai_action_panel_preview_check_copy))
            DraftMetaPill(text = stringResource(Res.string.feature_ai_action_panel_preview_check_meta))
        }
        if (options.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    Surface(
                        onClick = { onAction(option.value) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = displayLabel(option),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = fieldValueText(option = option, draft = draft),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaPanel(
    draft: CaptureDraft,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
    options: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
) {
    val mediaPreviewer: MediaPreviewer = koinInject()
    var previewingIndex by remember(draft.mediaAssets) { mutableStateOf<Int?>(null) }
    val hasMedia = draft.mediaAssets.isNotEmpty()
    val missingMediaCount = draft.mediaAssets.count { it.isMissing }
    val isGenerating = mediaGenerationProgress != null
    val primaryOptions = options.filter {
        it.type in setOf(
            ActionOptionType.CAPTURE_MEDIA,
            ActionOptionType.UPLOAD_MEDIA,
            ActionOptionType.REPLACE_MEDIA,
            ActionOptionType.GENERATE_MEDIA,
        )
    }
    val secondary = options.filterNot { it in primaryOptions }
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_title_media),
        title = if (isGenerating) {
            stringResource(Res.string.feature_ai_action_panel_media_title_generating)
        } else if (hasMedia) {
            stringResource(Res.string.feature_ai_action_panel_media_title_ready, draft.mediaAssets.size)
        } else {
            stringResource(Res.string.feature_ai_action_panel_media_title_missing)
        },
        supporting = if (isGenerating) {
            stringResource(Res.string.feature_ai_action_panel_media_support_generating)
        } else if (hasMedia) {
            stringResource(Res.string.feature_ai_action_panel_media_support_ready)
        } else {
            stringResource(Res.string.feature_ai_action_panel_media_support_missing)
        },
    ) {
        if (hasMedia || isGenerating) {
            Text(
                text = stringResource(Res.string.feature_ai_action_panel_media_existing),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            if (missingMediaCount > 0) {
                HintPill(
                    text = stringResource(
                        Res.string.feature_ai_action_panel_media_missing_hint,
                        missingMediaCount,
                    )
                )
            }
            MediaGridNine(
                items = draft.mediaAssets.mapIndexed { index, asset ->
                    asset.toMediaItem(index = index)
                },
                modifier = Modifier.fillMaxWidth(),
                onItemClick = { index ->
                    if (!draft.mediaAssets[index].isMissing) {
                        previewingIndex = index
                    }
                },
                overlayContent = { index, _ ->
                    IconButton(
                        onClick = { onAction(ActionOptionType.encodeRemoveMediaAt(index)) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(999.dp),
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(Res.string.feature_ai_action_remove_media),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                },
            )
            if (isGenerating) {
                GeneratingMediaTile(progress = mediaGenerationProgress)
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(Res.string.feature_ai_action_panel_media_empty_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        primaryOptions.forEach { option ->
            when (option.type) {
                ActionOptionType.CAPTURE_MEDIA -> if (hasMedia) {
                    OutlinedButton(
                        onClick = { onAction(option.value) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(displayLabel(option))
                    }
                } else {
                    Button(
                        onClick = { onAction(option.value) },
                        enabled = !isGenerating,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(displayLabel(option))
                    }
                }

                ActionOptionType.GENERATE_MEDIA -> if (hasMedia) {
                    FilledTonalButton(
                        onClick = { onAction(option.value) },
                        enabled = !isGenerating,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(displayLabel(option))
                    }
                } else {
                    Button(
                        onClick = { onAction(option.value) },
                        enabled = !isGenerating,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(displayLabel(option))
                    }
                }

                ActionOptionType.UPLOAD_MEDIA -> OutlinedButton(
                    onClick = { onAction(option.value) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(displayLabel(option))
                }

                else -> Button(
                    onClick = { onAction(option.value) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(displayLabel(option))
                }
            }
        }
        SecondaryActions(
            options = secondary,
            onAction = onAction,
        )
    }
    previewingIndex?.let {
        MediaGalleryDialog(
            items = draft.mediaAssets.mapIndexed { index, asset ->
                asset.toMediaItem(index = index)
            },
            initialIndex = it,
            mediaPreviewer = mediaPreviewer,
            onDismiss = { previewingIndex = null },
        )
    }
}

@Composable
private fun GeneratingMediaTile(
    progress: ProviderImageGenerationProgress?,
) {
    val fraction = progress
        ?.takeIf { (it.total ?: 0) > 0 }
        ?.let { (it.completed ?: 0).toFloat() / (it.total ?: 1).toFloat() }
        ?.coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.size(108.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = mediaGenerationStageLabel(progress),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = fraction?.let {
                    stringResource(
                        Res.string.feature_ai_action_panel_media_progress,
                        progress?.completed ?: 0,
                        progress?.total ?: 0,
                    )
                } ?: progress?.status.orEmpty().ifBlank { "..." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction ?: 0.22f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
private fun mediaGenerationStageLabel(
    progress: ProviderImageGenerationProgress?,
): String = when (progress?.stage) {
    ProviderImageGenerationProgress.Stage.FINALIZING ->
        stringResource(Res.string.feature_ai_action_panel_media_stage_finalizing)

    ProviderImageGenerationProgress.Stage.GENERATING ->
        stringResource(Res.string.feature_ai_action_panel_media_stage_generating)

    ProviderImageGenerationProgress.Stage.PREPARING,
    null -> stringResource(Res.string.feature_ai_action_panel_media_stage_preparing)
}

@Composable
private fun MediaLoadingPlaceholder(
    text: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TagPanel(
    options: List<ActionOptionSchema>,
    selectedTags: List<String>,
    onAction: (String) -> Unit,
) {
    val quickReplies = options.filter { it.type in setOf(ActionOptionType.SKIP_TAGS, ActionOptionType.REVIEW) }
    val tagOptions = options.filterNot { it in quickReplies }
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_edit_tags),
        title = if (selectedTags.isEmpty()) {
            stringResource(Res.string.feature_ai_action_panel_tag_title_empty)
        } else {
            stringResource(Res.string.feature_ai_action_panel_tag_title_selected)
        },
        supporting = stringResource(Res.string.feature_ai_action_panel_tag_support),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tagOptions.forEach { option ->
                when (option.type) {
                    ActionOptionType.REMOVE_TAG -> InputChip(
                        selected = true,
                        onClick = { onAction(option.value) },
                        label = { Text(displayLabel(option)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                            )
                        },
                    )

                    else -> AssistChip(
                        onClick = { onAction(option.value) },
                        label = { Text(displayLabel(option)) },
                    )
                }
            }
        }
        HintPill(text = stringResource(Res.string.feature_ai_action_panel_tag_custom_hint))
        SecondaryActions(
            options = quickReplies,
            onAction = onAction,
        )
    }
}

@Composable
private fun LocationPanel(
    draft: CaptureDraft,
    options: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
) {
    val hasLocation = draft.location?.name.isNullOrBlank().not()
    val clearAction = options.firstOrNull { it.type == ActionOptionType.CLEAR_LOCATION }
    val quickReplies = options.filter { it.type == ActionOptionType.REVIEW }
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_edit_location),
        title = if (hasLocation) {
            stringResource(Res.string.feature_ai_action_panel_location_title_ready)
        } else {
            stringResource(Res.string.feature_ai_action_panel_location_title_missing)
        },
        supporting = if (hasLocation) {
            stringResource(Res.string.feature_ai_action_panel_location_support_ready)
        } else {
            stringResource(Res.string.feature_ai_action_panel_location_support_missing)
        },
    ) {
        if (hasLocation) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_ai_action_panel_location_current),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                    Text(
                        text = draft.location.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(Res.string.feature_ai_action_panel_location_empty_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                stringResource(Res.string.feature_ai_action_panel_location_example_city),
                stringResource(Res.string.feature_ai_action_panel_location_example_district),
                stringResource(Res.string.feature_ai_action_panel_location_example_store),
            ).forEach { example ->
                ExamplePill(
                    text = example,
                    onClick = {
                        onAction(
                            ActionOptionType.encodeSetLocation(
                                example.substringAfter('：').substringAfter(":").ifBlank { example }
                            )
                        )
                    },
                )
            }
        }
        SecondaryActions(
            options = listOfNotNull(clearAction) + quickReplies,
            onAction = onAction,
        )
    }
}

@Composable
private fun InputPanel(
    component: ActionComponentSchema,
    draft: CaptureDraft,
    quickReplies: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
) {
    val title = when (component.field) {
        tech.zhifu.app.myhub.feature.ai.model.Field.SUMMARY -> stringResource(Res.string.feature_ai_action_panel_input_summary)
        tech.zhifu.app.myhub.feature.ai.model.Field.LOCATION -> stringResource(Res.string.feature_ai_action_panel_input_location)
        else -> stringResource(Res.string.feature_ai_action_panel_input_title)
    }
    val currentValue = when (component.field) {
        tech.zhifu.app.myhub.feature.ai.model.Field.SUMMARY -> draft.summary
        tech.zhifu.app.myhub.feature.ai.model.Field.LOCATION -> draft.location?.name.orEmpty()
        else -> draft.title
    }
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_input_hint),
        title = title,
        supporting = stringResource(Res.string.feature_ai_action_panel_input_support),
    ) {
        currentValue
            .trim()
            .takeIf { it.isNotBlank() }
            ?.let { value ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.feature_ai_action_panel_current_value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        SecondaryActions(
            options = quickReplies,
            onAction = onAction,
        )
    }
}

@Composable
private fun PublishPanel(
    options: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
) {
    val primary = options.firstOrNull { it.type == ActionOptionType.PUBLISH }
    val secondary = options.filterNot { it == primary }
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_publish),
        title = stringResource(Res.string.feature_ai_action_panel_publish_title),
        supporting = stringResource(Res.string.feature_ai_action_panel_publish_support),
    ) {
        primary?.let { option ->
            Button(
                onClick = { onAction(option.value) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(displayLabel(option))
            }
        }
        SecondaryActions(
            options = secondary,
            onAction = onAction,
        )
    }
}

@Composable
private fun QuickReplyPanel(
    options: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
) {
    ActionSupportPanel(
        title = stringResource(Res.string.feature_ai_action_panel_complete_title),
        supporting = stringResource(Res.string.feature_ai_action_panel_complete_support),
    ) {
        options.forEach { option ->
            Button(
                onClick = { onAction(option.value) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(displayLabel(option))
            }
        }
    }
}

@Composable
private fun GenericActionPanel(
    components: List<ActionComponentSchema>,
    selectedTags: List<String>,
    onAction: (String) -> Unit,
) {
    val options = components.flatMap { it.options }
    if (options.isEmpty()) return
    ActionSupportPanel(
        title = stringResource(Res.string.feature_ai_action_input_hint),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                when (option.type) {
                    ActionOptionType.REMOVE_TAG -> InputChip(
                        selected = true,
                        onClick = { onAction(option.value) },
                        label = { Text(displayLabel(option)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                            )
                        },
                    )

                    ActionOptionType.TAG -> AssistChip(
                        onClick = {
                            if (option.label in selectedTags) {
                                onAction(ActionOptionType.encodeTagRemove(option.label))
                            } else {
                                onAction(option.value)
                            }
                        },
                        label = { Text(displayLabel(option)) },
                    )

                    else -> OutlinedButton(
                        onClick = { onAction(option.value) },
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(displayLabel(option))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionSupportPanel(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    supporting: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            eyebrow
                ?.takeIf { it.isNotBlank() }
                ?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            supporting
                ?.takeIf { it.isNotBlank() }
                ?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            content()
        }
    }
}

@Composable
private fun SecondaryActions(
    options: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
) {
    if (options.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            when (option.type) {
                ActionOptionType.REVIEW,
                ActionOptionType.SAVE_DRAFT -> FilledTonalButton(
                    onClick = { onAction(option.value) },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(displayLabel(option))
                }

                ActionOptionType.DELETE_CARD,
                ActionOptionType.REMOVE_MEDIA -> OutlinedButton(
                    onClick = { onAction(option.value) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.28f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(displayLabel(option))
                }

                else -> OutlinedButton(
                    onClick = { onAction(option.value) },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(displayLabel(option))
                }
            }
        }
    }
}

@Composable
private fun DraftMetaPill(
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HintPill(
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.76f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
private fun ExamplePill(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun CaptureMediaAsset.toMediaItem(index: Int = 0): MediaItem {
    return MediaItem(
        id = sha256.ifBlank { storageHandle },
        name = displayName(index),
        previewUrl = accessUrl.takeUnless { isMissing }.orEmpty(),
        isVideo = mediaType.startsWith("video/", ignoreCase = true),
    )
}

private fun CaptureMediaAsset.displayName(index: Int = 0): String =
    accessUrl.substringAfterLast('/').ifBlank {
        if (mediaType.startsWith("video/", ignoreCase = true)) {
            "video-${index + 1}"
        } else {
            "image-${index + 1}"
        }
    }

private fun mergeOptions(
    primary: List<ActionOptionSchema>,
    secondary: List<ActionOptionSchema>,
): List<ActionOptionSchema> = buildList {
    addAll(primary)
    secondary.forEach { option ->
        if (none { it.value == option.value }) add(option)
    }
}

@Composable
private fun displayCaptureType(type: CaptureType): String = when (type) {
    CaptureType.PLACE -> stringResource(Res.string.feature_ai_capture_type_place)
    CaptureType.IDEA -> stringResource(Res.string.feature_ai_capture_type_idea)
    CaptureType.ARTICLE -> stringResource(Res.string.feature_ai_capture_type_article)
    CaptureType.PERSON -> stringResource(Res.string.feature_ai_capture_type_person)
}

@Composable
private fun fieldValueText(
    option: ActionOptionSchema,
    draft: CaptureDraft,
): String = when (option.type) {
    ActionOptionType.EDIT_TITLE -> draft.title.ifBlank {
        stringResource(Res.string.feature_ai_action_panel_edit_empty)
    }

    ActionOptionType.EDIT_SUMMARY -> draft.summary.ifBlank {
        draft.sourceText.ifBlank {
            stringResource(Res.string.feature_ai_action_panel_edit_empty)
        }
    }

    ActionOptionType.EDIT_LOCATION -> draft.location?.name?.takeIf { it.isNotBlank() } ?: stringResource(
        Res.string.feature_ai_action_panel_edit_empty
    )

    ActionOptionType.EDIT_MEDIA -> if (draft.mediaAssets.isEmpty()) {
        stringResource(Res.string.feature_ai_action_panel_edit_empty)
    } else {
        stringResource(Res.string.feature_ai_action_panel_edit_media_value, draft.mediaAssets.size)
    }

    ActionOptionType.EDIT_TAGS -> if (draft.tags.isEmpty()) {
        stringResource(Res.string.feature_ai_action_panel_edit_empty)
    } else {
        stringResource(
            Res.string.feature_ai_action_panel_edit_tags_value,
            draft.tags.take(2).joinToString(" / "),
        )
    }

    else -> stringResource(Res.string.feature_ai_action_panel_edit_empty)
}

@Composable
private fun displayLabel(option: ActionOptionSchema): String = when (option.type) {
    ActionOptionType.CAPTURE_MEDIA -> stringResource(Res.string.feature_ai_action_capture_media)
    ActionOptionType.UPLOAD_MEDIA -> stringResource(Res.string.feature_ai_action_upload_media)
    ActionOptionType.REPLACE_MEDIA -> stringResource(Res.string.feature_ai_action_replace_media)
    ActionOptionType.GENERATE_MEDIA -> stringResource(Res.string.feature_ai_action_generate_media)
    ActionOptionType.REMOVE_MEDIA -> stringResource(Res.string.feature_ai_action_remove_media)
    ActionOptionType.SKIP_MEDIA -> stringResource(Res.string.feature_ai_action_skip_media)
    ActionOptionType.SKIP_TAGS -> stringResource(Res.string.feature_ai_action_skip_tags)
    ActionOptionType.REVIEW -> stringResource(Res.string.feature_ai_action_review)
    ActionOptionType.EDIT_MEDIA -> stringResource(Res.string.feature_ai_action_edit_media)
    ActionOptionType.EDIT_LOCATION -> stringResource(Res.string.feature_ai_action_edit_location)
    ActionOptionType.EDIT_TITLE -> stringResource(Res.string.feature_ai_action_edit_title)
    ActionOptionType.EDIT_TAGS -> stringResource(Res.string.feature_ai_action_edit_tags)
    ActionOptionType.EDIT_SUMMARY -> stringResource(Res.string.feature_ai_action_edit_summary)
    ActionOptionType.PUBLISH -> stringResource(Res.string.feature_ai_action_publish)
    ActionOptionType.SAVE_DRAFT -> stringResource(Res.string.feature_ai_action_save_draft)
    ActionOptionType.DELETE_CARD -> stringResource(Res.string.feature_ai_action_delete_card)
    ActionOptionType.NEW_CAPTURE -> stringResource(Res.string.feature_ai_action_new_capture)
    ActionOptionType.INPUT_TITLE_HINT -> stringResource(Res.string.feature_ai_action_input_title_hint)
    ActionOptionType.CLEAR_LOCATION -> stringResource(Res.string.feature_ai_action_clear_location)
    ActionOptionType.REMOVE_TAG -> option.label
    else -> option.label
}
