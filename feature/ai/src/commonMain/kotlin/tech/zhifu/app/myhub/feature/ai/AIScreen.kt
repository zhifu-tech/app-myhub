package tech.zhifu.app.myhub.feature.ai

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.ai.content.Content
import tech.zhifu.app.myhub.feature.ai.content.appbar.BottomBar
import tech.zhifu.app.myhub.feature.ai.content.appbar.TopBar
import tech.zhifu.app.myhub.feature.ai.content.preview.AIPreview
import tech.zhifu.app.myhub.feature.ai.content.statics.Error
import tech.zhifu.app.myhub.feature.ai.content.statics.Loading
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun AIScreen(
    navigator: AppNavigator,
    viewModel: AIViewModel = koinViewModel<AIViewModel>(),
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        it.state
    }
    AIScreenContent(
        state = state,
        topBar = {
            TopBar(navigator = navigator)
        },
        bottomBar = {
            BottomBar(viewModel = viewModel)
        },
        loading = { contentPadding ->
            Loading(contentPadding = contentPadding, viewModel = viewModel)
        },
        error = { contentPadding ->
            Error(
                contentPadding = contentPadding,
                viewModel = viewModel,
            )
        },
        content = { contentPadding ->
            Content(
                contentPadding = contentPadding,
                viewModel = viewModel,
            )
        }
    )
    AIPreview(viewModel = viewModel)
}

@Composable
internal fun AIScreenContent(
    state: AIUiState.State,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    loading: @Composable (PaddingValues) -> Unit,
    error: @Composable (PaddingValues) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        content = { contentPadding ->
            when (state) {
                AIUiState.State.IDLE,
                AIUiState.State.LOADING -> loading(contentPadding)

                AIUiState.State.ERROR -> error(contentPadding)
                AIUiState.State.CONTENT -> content(contentPadding)
            }
        },
    )
}
