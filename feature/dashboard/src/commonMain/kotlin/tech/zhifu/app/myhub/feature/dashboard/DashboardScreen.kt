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
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.Content
import tech.zhifu.app.myhub.feature.dashboard.content.Error
import tech.zhifu.app.myhub.feature.dashboard.content.Loading
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.BottomBar
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBar
import tech.zhifu.app.myhub.feature.mixed.api.navigateToOpenSourceLicenses
import tech.zhifu.app.myhub.feature.mixed.api.navigateToSupport
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.navigation.AppNavigator

@Composable
fun DashboardScreen(
    navigator: AppNavigator,
    viewModel: DashboardViewModel = koinViewModel<DashboardViewModel>(),
) {
    DashboardSideEffect(
        navigator = navigator,
        viewModel = viewModel
    )

    val state by viewModel.collectFieldAsState {
        it.state
    }

    logger.debug { "DashboardScreen state is $state" }

    DashboardScreenContent(
        state = state,
        topBar = {
            TopBar(modifier = it, viewModel = viewModel)
        },
        bottomBar = {
            BottomBar(modifier = it, viewModel = viewModel)
        },
        loading = {
            Loading(modifier = it, viewModel = viewModel)
        },
        error = {
            Error(modifier = it, viewModel = viewModel)
        },
        content = { paddingValues, modifier ->
            Content(
                paddingValues = paddingValues,
                modifier = modifier,
                viewModel = viewModel
            )
        }
    )
}

@Composable
internal fun DashboardScreenContent(
    state: DashboardUiState.State,
    topBar: @Composable (Modifier) -> Unit,
    bottomBar: @Composable (Modifier) -> Unit,
    loading: @Composable (Modifier) -> Unit,
    error: @Composable (Modifier) -> Unit,
    content: @Composable (PaddingValues, Modifier) -> Unit,
) {
    val hazeState = rememberHazeState()
    val hazeStyle = HazeMaterials.regular(containerColor = MaterialTheme.colorScheme.surface)
    val hazeInputScale: HazeInputScale = HazeInputScale.Default
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            topBar(
                Modifier.hazeEffect(state = hazeState, style = hazeStyle) {
                    this.inputScale = hazeInputScale
                    this.progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
            )
        },
        bottomBar = {
            bottomBar(Modifier)
        },
    ) { contentPadding ->
        when (state) {
            DashboardUiState.State.LOADING -> {
                loading(Modifier.padding(paddingValues = contentPadding))
            }

            DashboardUiState.State.ERROR -> {
                error(Modifier.padding(paddingValues = contentPadding))
            }

            DashboardUiState.State.CONTENT -> {
                content(
                    contentPadding,
                    Modifier.hazeSource(state = hazeState)
                )
            }
        }
    }
}

@Composable
private fun DashboardSideEffect(
    navigator: AppNavigator,
    viewModel: DashboardViewModel
) {
    viewModel.collectSharedSideEffect { effect ->
        logger.warn { "navigate collectSharedSideEffect is  $effect" }
        when (effect) {

            DashboardSideEffect.NavigateToOpenSourceLicenses -> {
                navigator.navigateToOpenSourceLicenses()
            }

            DashboardSideEffect.NavigateToSupport -> {
                navigator.navigateToSupport()
            }

            else -> Unit
        }
    }
}
