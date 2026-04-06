package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.ai.api.navigation.navigateToAICapture
import tech.zhifu.app.myhub.feature.dashboard.content.Content
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.BottomBar
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBar
import tech.zhifu.app.myhub.feature.dashboard.content.preview.DashboardPreview
import tech.zhifu.app.myhub.feature.dashboard.content.statics.Error
import tech.zhifu.app.myhub.feature.dashboard.content.statics.Loading
import tech.zhifu.app.myhub.feature.settings.api.navigateToSettings
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.ui.viewmodel.collectAsState
import tech.zhifu.app.myhub.ui.viewmodel.collectSharedSideEffect

@Composable
fun DashboardScreen(
    navigator: AppNavigator,
    viewModel: DashboardViewModel = koinViewModel<DashboardViewModel>(),
) {
    DashboardSideEffect(
        navigator = navigator,
        viewModel = viewModel
    )
    val state by viewModel.collectAsState { it.state }
    DashboardScreenContent(
        state = state,
        topBar = { hazeState ->
            TopBar(viewModel = viewModel, hazeState = hazeState)
        },
        bottomBar = {
            BottomBar(modifier = Modifier, viewModel = viewModel)
        },
        loading = { contentPadding ->
            Loading(contentPadding = contentPadding, viewModel = viewModel)
        },
        error = { contentPadding ->
            Error(contentPadding = contentPadding, viewModel = viewModel)
        },
        content = { contentPadding, hazeState ->
            Content(
                paddingValues = contentPadding,
                hazeState = hazeState,
                viewModel = viewModel,
            )
        }
    )
    DashboardPreview(viewModel = viewModel)
}

@Composable
internal fun DashboardScreenContent(
    state: DashboardUiState.State,
    topBar: @Composable (HazeState) -> Unit,
    bottomBar: @Composable () -> Unit,
    loading: @Composable (PaddingValues) -> Unit,
    error: @Composable (PaddingValues) -> Unit,
    content: @Composable (PaddingValues, HazeState) -> Unit,
) {
    val hazeState = rememberHazeState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { topBar(hazeState) },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        content = { contentPadding ->
            when (state) {
                DashboardUiState.State.IDLE,
                DashboardUiState.State.LOADING -> loading(contentPadding)

                DashboardUiState.State.ERROR -> error(contentPadding)
                DashboardUiState.State.CONTENT -> content(contentPadding, hazeState)
            }
        },
    )
}

@Composable
private fun DashboardSideEffect(
    navigator: AppNavigator,
    viewModel: DashboardViewModel
) {
    viewModel.collectSharedSideEffect { effect ->
        when (effect) {
            DashboardSideEffect.NavigateToAiCapture -> {
                navigator.navigateToAICapture()
            }

            DashboardSideEffect.NavigateToSettings -> {
                navigator.navigateToSettings()
            }

            else -> Unit
        }
    }
}
