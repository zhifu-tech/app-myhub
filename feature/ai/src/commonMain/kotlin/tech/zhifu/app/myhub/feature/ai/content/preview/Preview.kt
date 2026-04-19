package tech.zhifu.app.myhub.feature.ai.content.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_continue_edit
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_continue_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_preview_untitled_draft
import tech.zhifu.app.myhub.feature.preview.Preview
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun AIPreview(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier,
    pinned: Boolean = false,
) {
    val previewState by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.previewState
    }
    val safePreviewState = previewState ?: return
    if (pinned) {
        // 需要实时预览
        val draft by viewModel.uiState.collectAsSelectedStateWithLifecycle {
            (it as? AIUiState.Content)?.context?.draft
        }
        safePreviewState.pined = true
        safePreviewState.card.value = draft?.toPreviewCard(
            untitledDraft = stringResource(Res.string.feature_ai_preview_untitled_draft),
            continueHint = stringResource(Res.string.feature_ai_preview_continue_hint),
            continueEdit = stringResource(Res.string.feature_ai_preview_continue_edit),
        )
    } else {
        // 需要点击设置
        safePreviewState.pined = false
        safePreviewState.card.value = null
    }

    Preview(
        modifier = modifier,
        state = safePreviewState,
    )
}
