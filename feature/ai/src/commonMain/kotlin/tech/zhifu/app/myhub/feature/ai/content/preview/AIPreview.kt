package tech.zhifu.app.myhub.feature.ai.content.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.preview.Preview
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun AIPreview(viewModel: AIViewModel) {
    val previewState by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.previewState
    }
    val safePreviewState = previewState ?: return
    Preview(state = safePreviewState)
}
