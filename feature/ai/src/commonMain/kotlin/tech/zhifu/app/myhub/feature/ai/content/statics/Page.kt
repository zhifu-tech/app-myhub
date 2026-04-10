package tech.zhifu.app.myhub.feature.ai.content.statics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.statics.page.ErrorContent
import tech.zhifu.app.myhub.component.statics.page.LoadingContent
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_error_fallback_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_loading_subtitle
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_loading_title
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun Error(
    contentPadding: PaddingValues,
    viewModel: AIViewModel
) {
    val errorState by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        it as? AIUiState.Error
    }
    ErrorContent(
        modifier = Modifier.padding(paddingValues = contentPadding),
        title = errorState?.message?.ifBlank {
            stringResource(Res.string.feature_ai_error_fallback_title)
        }.orEmpty(),
        onRetry = {
        }
    )
}

@Composable
fun Loading(
    contentPadding: PaddingValues,
    viewModel: AIViewModel
) {
    LoadingContent(
        modifier = Modifier.padding(paddingValues = contentPadding),
        title = stringResource(Res.string.feature_ai_loading_title),
        subTitle = stringResource(Res.string.feature_ai_loading_subtitle),
    )
}
