package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.component.MediaGalleryDialog
import tech.zhifu.app.myhub.component.media.component.MediaGridNine
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress.Stage
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionEvent
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponent
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPayload
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.text
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.model.displayName
import tech.zhifu.app.myhub.feature.ai.model.isVideo
import tech.zhifu.app.myhub.feature.ai.resources.Res
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
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_remove_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_media
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun MediaActionPanel(
    component: ActionComponent,
    viewModel: AIViewModel,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { content ->
            MediaActionPanelState(
                draft = content.context.draft,
                generationProgress = content.context.mediaGenerationProgress,
            )
        }
    }
    val safeState = state ?: return
    MediaActionPanel(
        component = component,
        draft = safeState.draft,
        mediaGenerationProgress = safeState.generationProgress,
        onAction = viewModel::doAction,
    )
}

private class MediaActionPanelState(
    val draft: CaptureDraft,
    val generationProgress: ProviderImageGenerationProgress?,
)

@Composable
fun MediaActionPanel(
    component: ActionComponent,
    draft: CaptureDraft,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
    onAction: (ActionEvent) -> Unit,
) {
    val payload = component.payload as? ActionPayload.Media ?: return
    var mediaInitialIndex by remember(draft.mediaAssets) { mutableStateOf<Int?>(null) }
    val mediaItems = draft.mediaAssets.mapIndexed { index, asset ->
        asset.toMediaItem(index = index)
    }

    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_title_media),
        title = when {
            payload.isGenerating -> stringResource(Res.string.feature_ai_action_panel_media_title_generating)
            payload.hasMedia -> stringResource(
                Res.string.feature_ai_action_panel_media_title_ready,
                payload.mediaCount,
            )

            else -> stringResource(Res.string.feature_ai_action_panel_media_title_missing)
        },
        supporting = when {
            payload.isGenerating -> stringResource(Res.string.feature_ai_action_panel_media_support_generating)
            payload.hasMedia -> stringResource(Res.string.feature_ai_action_panel_media_support_ready)
            else -> stringResource(Res.string.feature_ai_action_panel_media_support_missing)
        },
    ) {
        if (payload.hasMedia || payload.isGenerating) {
            Text(
                text = stringResource(Res.string.feature_ai_action_panel_media_existing),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            if (payload.missingMediaCount > 0) {
                HintPill(
                    text = stringResource(
                        Res.string.feature_ai_action_panel_media_missing_hint,
                        payload.missingMediaCount,
                    )
                )
            }
            MediaGridNine(
                items = mediaItems,
                modifier = Modifier.fillMaxWidth(),
                onItemClick = { index ->
                    if (!draft.mediaAssets[index].isMissing) {
                        mediaInitialIndex = index
                    }
                },
                overlayContent = { index, _ ->
                    val overlayAction = component.actions.overlay.getOrNull(index) ?: return@MediaGridNine
                    IconButton(
                        onClick = { onAction(overlayAction.event) },
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
                            contentDescription = overlayAction.label.text(),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                },
            )
            if (payload.isGenerating) {
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
        ActionButtonsColumn(
            actions = component.actions.primary,
            onAction = onAction,
        )
        ActionButtonsFlow(
            actions = component.actions.secondary,
            onAction = onAction,
        )
    }
    mediaInitialIndex?.let {
        MediaGalleryDialog(
            items = mediaItems,
            initialIndex = it,
            onDismiss = { mediaInitialIndex = null },
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
                        .fillMaxWidth(fraction ?: 0.18f)
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
    Stage.PREPARING -> stringResource(Res.string.feature_ai_action_panel_media_stage_preparing)
    Stage.GENERATING -> stringResource(Res.string.feature_ai_action_panel_media_stage_generating)
    Stage.FINALIZING -> stringResource(Res.string.feature_ai_action_panel_media_stage_finalizing)
    else -> stringResource(Res.string.feature_ai_action_remove_media)
}

private fun CaptureMediaAsset.toMediaItem(
    index: Int,
): MediaItem = if (isMissing) {
    MediaItem(
        id = "${storageHandle}_missing_$index",
        name = displayName(index),
        previewUrl = "",
        isVideo = isVideo(),
    )
} else {
    MediaItem(
        id = storageHandle.ifBlank { "draft_media_$index" },
        name = displayName(index),
        previewUrl = accessUrl,
        isVideo = isVideo(),
    )
}
