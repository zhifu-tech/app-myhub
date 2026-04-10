package tech.zhifu.app.myhub.feature.ai.content.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.input.ChatInputBar
import tech.zhifu.app.myhub.feature.ai.content.preview.toPreviewCard
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_continue_edit
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_continue_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_untitled_draft
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.content.PreviewFloatThumbnail
import tech.zhifu.app.myhub.feature.preview.util.PreviewAnimatedVisibility
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun BottomBar(
    viewModel: AIViewModel,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { uiState ->
            BottomBarState(
                draft = uiState.draft,
                input = uiState.input,
                isPublishing = uiState.isPublishing,
                conversationState = uiState.conversationState,
                missingFields = uiState.missingFields,
                previewState = uiState.previewState,
            )
        }
    }
    val safeState = state ?: return
    val untitledDraft = stringResource(Res.string.feature_ai_preview_untitled_draft)
    val continueHint = stringResource(Res.string.feature_ai_preview_continue_hint)
    val continueEdit = stringResource(Res.string.feature_ai_preview_continue_edit)
    BottomBarContent(
        state = safeState,
        onDraftPreviewClick = { previewState, draft ->
            previewState.show(
                draft.toPreviewCard(
                    untitledDraft = untitledDraft,
                    continueHint = continueHint,
                    continueEdit = continueEdit,
                )
            )
        },
        inputBox = { modifier ->
            ChatInputBar(
                modifier = modifier,
                viewModel = viewModel,
                state = safeState,
            )
        }
    )
}

@Composable
fun BottomBarContent(
    state: BottomBarState,
    onDraftPreviewClick: (PreviewState, CaptureDraft) -> Unit = { _, _ -> },
    inputBox: @Composable (Modifier) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(16.dp), // 保持与键盘间距
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            InputBoxWrapper(inputBox)
        }

        // 非 Pinned 模式，显示预览缩略图
        if (!state.previewState.pined) {
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                PreviewAnimatedVisibility(
                    visible = state.previewState.isPreviewing().not(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = (-64).dp),
                ) {
                    val safeDraft = state.draft ?: return@PreviewAnimatedVisibility
                    PreviewFloatThumbnail(
                        previewKey = "content-preview-${safeDraft.id}",
                        coverKey = "content-image-${safeDraft.id}",
                        animatedVisibilityScope = this,
                        onClick = { onDraftPreviewClick(state.previewState, safeDraft) },
                    )
                }
            }
        }

    }
}

@Composable
private fun RowScope.InputBoxWrapper(inputBox: @Composable ((Modifier) -> Unit)) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val modifier = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
            Modifier.widthIn(min = 120.dp, max = 480.dp)
        }

        else -> {
            Modifier.weight(1f)
        }
    }

    inputBox(modifier)
}


@Immutable
data class BottomBarState(
    val draft: CaptureDraft?,
    val input: String,
    val isPublishing: Boolean,
    val conversationState: ConversationState,
    val missingFields: List<Field>,
    val previewState: PreviewState,
)
