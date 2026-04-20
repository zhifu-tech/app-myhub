package tech.zhifu.app.myhub.feature.ai.content.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.hasVisibleContent
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_button_label
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_continue_edit
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_continue_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_untitled_draft
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.content.PreviewFloatThumbnail
import tech.zhifu.app.myhub.feature.preview.util.PreviewAnimatedVisibility
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun BoxScope.PreviewThumbnail(
    viewModel: AIViewModel
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { uiState ->
            if (uiState.previewState.pined) {
                return@let null
            }
            PreviewThumbnailState(
                previewState = uiState.previewState,
                draft = uiState.context.draft.takeIf { it.hasVisibleContent() },
            )
        }
    }
    val safeState = state ?: return
    val untitledDraft = stringResource(Res.string.feature_ai_preview_untitled_draft)
    val continueHint = stringResource(Res.string.feature_ai_preview_continue_hint)
    val continueEdit = stringResource(Res.string.feature_ai_preview_continue_edit)
    PreviewThumbnailStateContent(
        state = safeState,
        onClick = { previewState, draft ->
            previewState.show(
                draft.toPreviewCard(
                    untitledDraft = untitledDraft,
                    continueHint = continueHint,
                    continueEdit = continueEdit,
                )
            )
        }
    )
}

internal data class PreviewThumbnailState(
    val previewState: PreviewState,
    val draft: CaptureDraft? = null,
)

@Composable
internal fun BoxScope.PreviewThumbnailStateContent(
    state: PreviewThumbnailState,
    onClick: (PreviewState, CaptureDraft) -> Unit,
) {
    Box(
        modifier = Modifier.matchParentSize()
    ) {
        PreviewAnimatedVisibility(
            visible = state.previewState.isPreviewing().not() && state.draft != null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-84).dp),
        ) {
            val safeDraft = state.draft ?: return@PreviewAnimatedVisibility
            val animatedScope = this
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PreviewFloatThumbnail(
                    previewKey = "content-preview-${safeDraft.id}",
                    coverKey = "content-image-${safeDraft.id}",
                    coverUrl = safeDraft.previewCoverUrl(),
                    animatedVisibilityScope = animatedScope,
                    onClick = { onClick(state.previewState, safeDraft) },
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            onClick(state.previewState, safeDraft)
                        },
                ) {
                    Text(
                        text = stringResource(Res.string.feature_ai_preview_button_label),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
