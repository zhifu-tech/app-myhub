package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.ai.api.navigation.navigateToAICapture
import tech.zhifu.app.myhub.feature.dashboard.content.Content
import tech.zhifu.app.myhub.feature.dashboard.content.Error
import tech.zhifu.app.myhub.feature.dashboard.content.Loading
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.BottomBar
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBar
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.selectedCard
import tech.zhifu.app.myhub.feature.preview.Preview
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
    DashboardScreenContent(
        scaffold = {
            DashboardScaffold(
                viewModel = viewModel,
            )
        },
        preview = {
            DashboardPreview(
                viewModel = viewModel,
            )
        }
    )
}

@Composable
fun DashboardScreenContent(
    scaffold: @Composable () -> Unit,
    preview: @Composable () -> Unit,
) {
    scaffold()
    preview()
}

@Composable
internal fun DashboardScaffold(
    viewModel: DashboardViewModel,
) {
    DashboardScaffoldContent(
        topBar = { hazeState ->
            TopBar(
                viewModel = viewModel,
                hazeState = hazeState,
            )
        },
        bottomBar = {
            BottomBar(modifier = Modifier, viewModel = viewModel)
        },
        content = { contentPadding, hazeState ->
            DashboardContent(
                viewModel = viewModel,
                contentPadding = contentPadding,
                hazeState = hazeState,
            )
        }
    )
}

@Composable
internal fun DashboardScaffoldContent(
    topBar: @Composable (HazeState) -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, HazeState) -> Unit
) {
    val hazeState = rememberHazeState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            topBar(hazeState)
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        content(it, hazeState)
    }
}

@Composable
internal fun DashboardContent(
    viewModel: DashboardViewModel,
    contentPadding: PaddingValues,
    hazeState: HazeState,
) {
    val state by viewModel.collectAsState { it.state }
    DashboardContentContent(
        state = state,
        loading = {
            Loading(modifier = Modifier.padding(paddingValues = contentPadding))
        },
        error = {
            Error(
                modifier = Modifier.padding(paddingValues = contentPadding),
                viewModel = viewModel
            )
        },
        content = {
            Content(
                paddingValues = contentPadding,
                modifier = Modifier.hazeSource(state = hazeState),
                viewModel = viewModel,
            )
        }
    )
}

@Composable
internal fun DashboardContentContent(
    state: DashboardUiState.State,
    loading: @Composable () -> Unit,
    error: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    when (state) {
        DashboardUiState.State.LOADING -> loading()
        DashboardUiState.State.ERROR -> error()
        DashboardUiState.State.CONTENT -> content()
        else -> {}
    }
}

@Composable
fun DashboardPreview(
    viewModel: DashboardViewModel
) {
    val selectedCard by viewModel.collectAsState {
        (it as? DashboardUiState.Content)?.selectedCard
    }

    Preview(
        card = selectedCard,
        actionHide = {
            viewModel.selectedCard(null)
        }
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
