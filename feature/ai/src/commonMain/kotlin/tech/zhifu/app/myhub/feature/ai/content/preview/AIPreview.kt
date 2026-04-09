package tech.zhifu.app.myhub.feature.ai.content.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
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
            (it as? AIUiState.Content)?.draft
        }
        safePreviewState.pined = pinned
        safePreviewState.card.value = draft?.toPreviewCard()
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
