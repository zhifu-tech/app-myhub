package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.MockContent
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.BottomBar
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBar
import tech.zhifu.app.myhub.feature.mixed.api.navigateToOpenSourceLicenses
import tech.zhifu.app.myhub.feature.mixed.api.navigateToSupport
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.util.tapToClearFocus

@Composable
fun DashboardRoute(
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

    DashboardScreen(
        state = state,
        topBar = {
            TopBar(
                modifier = it,
                viewModel = viewModel,
            )
        },
        bottomBar = {
            BottomBar(
                modifier = it,
                viewModel = viewModel,
            )
        },
        error = { modifier ->
//            ErrorRoute(
//                modifier = modifier,
//                viewModel = viewModel,
//            )
        },
        content = { modifier ->
            MockContent(modifier)
//            ContentRoute(
//                modifier = modifier,
//                viewModel = viewModel,
//            )
        },
    )
}

@Composable
internal fun DashboardScreen(
    state: DashboardUiState.State,
    topBar: @Composable (Modifier) -> Unit,
    error: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit,
    bottomBar: @Composable (Modifier) -> Unit,
) {
    val gridState = rememberLazyGridState()
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
        Column {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(128.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = contentPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .tapToClearFocus()
                    .testTag("lazy_grid")
                    .hazeSource(state = hazeState),
            ) {
                items(50) { index ->
                    ImageItem(
                        text = "${index + 1}",
                        index = index,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3 / 4f),
                    )
                }
            }
        }
//        when (state) {
//            DashboardUiState.State.LOADING -> LoadingWheel(
//                modifier = Modifier.padding(innerPadding),
//                contentDesc = "加载内容", // fixme 翻译
//            )
//
//            DashboardUiState.State.ERROR -> error(
//                Modifier.padding(innerPadding)
//            )
//
//            DashboardUiState.State.CONTENT -> content(
//                Modifier.padding(paddingValues = innerPadding)
//                    .hazeSource(state = hazeState)
//            )
//        }
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


/**
 * Simple pager item which displays an image
 */
@Composable
internal fun ImageItem(
    text: String,
    index: Int = -1,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        Box {
            AsyncImage(
                model = rememberRandomSampleImageUrl(index),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )

            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
                    .padding(8.dp)
                    .wrapContentSize(Alignment.Center),
            )
        }
    }
}

val precannedImageUrls: List<String> by lazy {
    (0 until 50).map { randomSampleImageUrl() }
}

private val rangeForRandom = (0..100_000)

fun randomSampleImageUrl(
    seed: Int = rangeForRandom.random(),
    width: Int = 800,
    height: Int = width,
): String = "https://picsum.photos/seed/$seed/$width/$height"

@Composable
fun rememberRandomSampleImageUrl(index: Int = -1): String = rememberSaveable(index) {
    precannedImageUrls.getOrNull(index) ?: randomSampleImageUrl()
}
