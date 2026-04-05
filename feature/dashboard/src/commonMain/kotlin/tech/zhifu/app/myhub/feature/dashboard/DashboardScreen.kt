package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
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
    DashboardScaffold(
        viewModel = viewModel,
    )
    DashboardPreview(
        viewModel = viewModel,
    )
}

@Composable
internal fun DashboardScaffold(
    viewModel: DashboardViewModel,
) {
    val hazeState = rememberHazeState()
    val hazeStyle = HazeMaterials.regular(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
    val hazeInputScale: HazeInputScale = HazeInputScale.Default
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                modifier = Modifier.hazeEffect(state = hazeState, style = hazeStyle) {
                    inputScale = hazeInputScale
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                },
                viewModel = viewModel,
            )
        },
        bottomBar = {
            BottomBar(modifier = Modifier, viewModel = viewModel)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) { contentPadding ->
        DashboardContent(
            viewModel = viewModel,
            contentPadding = contentPadding,
            hazeState = hazeState,
        )
    }
}

@Composable
private fun DashboardContent(
    viewModel: DashboardViewModel,
    contentPadding: PaddingValues,
    hazeState: HazeState,
) {
    val state by viewModel.collectAsState { it.state }
    when (state) {
        DashboardUiState.State.LOADING -> {
            Loading(
                modifier = Modifier.padding(paddingValues = contentPadding),
                viewModel = viewModel
            )
        }

        DashboardUiState.State.ERROR -> {
            Error(
                modifier = Modifier.padding(paddingValues = contentPadding),
                viewModel = viewModel
            )
        }

        DashboardUiState.State.CONTENT -> {
            Content(
                paddingValues = contentPadding,
                modifier = Modifier.hazeSource(state = hazeState),
                viewModel = viewModel,
            )
        }

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
