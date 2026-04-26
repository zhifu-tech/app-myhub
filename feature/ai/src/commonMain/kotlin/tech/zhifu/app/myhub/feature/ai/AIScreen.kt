package tech.zhifu.app.myhub.feature.ai

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.ai.content.Content
import tech.zhifu.app.myhub.feature.ai.content.appbar.BottomBar
import tech.zhifu.app.myhub.feature.ai.content.appbar.TopBar
import tech.zhifu.app.myhub.feature.ai.content.preview.AIPreview
import tech.zhifu.app.myhub.feature.ai.content.statics.Loading
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.ui.design.util.LocalSharedTransitionScope
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun AIScreen(
    navigator: AppNavigator,
    viewModel: AIViewModel = koinViewModel<AIViewModel>(),
) {
    AIScreen(
        content = {
            val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
                it.state
            }
            AIScreenContent(
                state = state,
                topBar = {
                    TopBar(navigator = navigator, viewModel = viewModel)
                },
                bottomBar = {
                    BottomBar(viewModel = viewModel)
                },
                loading = { contentPadding ->
                    Loading(contentPadding = contentPadding, viewModel = viewModel)
                },
                content = { contentPadding ->
                    Content(contentPadding = contentPadding, viewModel = viewModel)
                }
            )
        },
        preview = { pinned, modifier ->
            AIPreview(
                viewModel = viewModel,
                modifier = modifier,
                pinned = pinned,
            )
        }
    )
}

@Composable
fun AIScreen(
    content: @Composable BoxScope.() -> Unit,
    preview: @Composable (Boolean, Modifier) -> Unit,
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            when {
                windowSizeClass.isWidthAtLeastBreakpoint(
                    widthDpBreakpoint = WIDTH_DP_EXPANDED_LOWER_BOUND
                ) -> {
                    AIScreenTwoPanel(content = content, preview = preview)
                }

                else -> {
                    AIScreenSinglePanel(content = content, preview = preview)
                }
            }
        }
    }
}

@Composable
fun AIScreenSinglePanel(
    content: @Composable BoxScope.() -> Unit,
    preview: @Composable (Boolean, Modifier) -> Unit,
) {
    Box {
        content()
        preview(false, Modifier)
    }
}

@Composable
fun AIScreenTwoPanel(
    content: @Composable BoxScope.() -> Unit,
    preview: @Composable (Boolean, Modifier) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        val minPreviewWidth = 420.dp
        val maxPreviewWidth = (maxWidth - 480.dp)
            .coerceAtLeast(minimumValue = minPreviewWidth)
        var previewHostWidth by remember {
            val initialValue = minOf(a = 600.dp, b = maxPreviewWidth)
            mutableStateOf(value = initialValue)
        }
        val density = LocalDensity.current
        Row(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                content()
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { deltaPx ->
                            val delta = with(density) { deltaPx.toDp() }
                            previewHostWidth = (previewHostWidth - delta)
                                .coerceIn(
                                    minimumValue = minPreviewWidth,
                                    maximumValue = maxPreviewWidth,
                                )
                        },
                    ),
            )
            preview(
                true,
                Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .width(previewHostWidth)
            )
        }
    }
}

@Composable
internal fun AIScreenContent(
    state: AIUiState.State,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    loading: @Composable (PaddingValues) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        content = { contentPadding ->
            when (state) {
                AIUiState.State.LOADING -> loading(contentPadding)
                AIUiState.State.CONTENT -> content(contentPadding)
            }
        },
    )
}
