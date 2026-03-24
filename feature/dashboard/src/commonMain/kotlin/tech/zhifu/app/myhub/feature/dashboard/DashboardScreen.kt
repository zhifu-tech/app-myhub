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
import tech.zhifu.app.myhub.feature.dashboard.content.Content
import tech.zhifu.app.myhub.feature.dashboard.content.Error
import tech.zhifu.app.myhub.feature.dashboard.content.Loading
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.BottomBar
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBar
import tech.zhifu.app.myhub.feature.mixed.api.navigateToOpenSourceLicenses
import tech.zhifu.app.myhub.feature.mixed.api.navigateToSupport
import tech.zhifu.app.myhub.feature.preview.Preview
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.rememberPreviewState
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
    DashboardScreen(
        viewModel = viewModel,
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
        content = { paddingValues, modifier, predicate ->
            Content(
                paddingValues = paddingValues,
                modifier = modifier,
                viewModel = viewModel,
                previewState = predicate,
            )
        },
    )
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    topBar: @Composable (Modifier) -> Unit,
    bottomBar: @Composable (Modifier) -> Unit,
    loading: @Composable (Modifier) -> Unit,
    error: @Composable (Modifier) -> Unit,
    content: @Composable (PaddingValues, Modifier, PreviewState) -> Unit,
) {
    val previewState = rememberPreviewState()
    DashboardScaffold(
        viewModel = viewModel,
        topBar = topBar,
        bottomBar = bottomBar,
        loading = loading,
        error = error,
        previewState = previewState,
        content = content,
    )
    Preview(
        state = previewState,
    )
}

@Composable
internal fun DashboardScaffold(
    viewModel: DashboardViewModel,
    topBar: @Composable (Modifier) -> Unit,
    bottomBar: @Composable (Modifier) -> Unit,
    loading: @Composable (Modifier) -> Unit,
    error: @Composable (Modifier) -> Unit,
    previewState: PreviewState,
    content: @Composable (PaddingValues, Modifier, PreviewState) -> Unit,
) {
    val hazeState = rememberHazeState()
    val hazeStyle = HazeMaterials.regular(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
    val hazeInputScale: HazeInputScale = HazeInputScale.Default
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            topBar(
                Modifier.hazeEffect(state = hazeState, style = hazeStyle) {
                    inputScale = hazeInputScale
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
            )
        },
        bottomBar = {
            bottomBar(Modifier)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) { contentPadding ->
        DashboardContent(
            viewModel = viewModel,
            loading = loading,
            contentPadding = contentPadding,
            error = error,
            content = content,
            hazeState = hazeState,
            previewState = previewState,
        )
    }
}

@Composable
private fun DashboardContent(
    viewModel: DashboardViewModel,
    loading: @Composable ((Modifier) -> Unit),
    contentPadding: PaddingValues,
    error: @Composable ((Modifier) -> Unit),
    content: @Composable (PaddingValues, Modifier, PreviewState) -> Unit,
    hazeState: HazeState,
    previewState: PreviewState,
) {
    val state by viewModel.collectFieldAsState { it.state }
    when (state) {
        DashboardUiState.State.LOADING -> {
            loading(Modifier.padding(paddingValues = contentPadding))
        }

        DashboardUiState.State.ERROR -> {
            error(Modifier.padding(paddingValues = contentPadding))
        }

        DashboardUiState.State.CONTENT -> {
            content(contentPadding, Modifier.hazeSource(state = hazeState), previewState)
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
