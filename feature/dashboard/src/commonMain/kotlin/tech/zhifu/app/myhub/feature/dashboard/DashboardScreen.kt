package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectSideEffect
import tech.zhifu.app.myhub.feature.capture.api.navigation.navigateToCapture
import tech.zhifu.app.myhub.feature.card.api.navigateToCardDetail
import tech.zhifu.app.myhub.feature.dashboard.content.ContentRoute
import tech.zhifu.app.myhub.feature.dashboard.topbar.TopBarRoute
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.navigation.AppNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoute(
    navigator: AppNavigator,
    viewModel: DashboardViewModel = koinInject<DashboardViewModel>(),
) {
    logger.debug("Dashboard Screen") { "DashboardScreen" }
    CollectSideEffect(
        navigator = navigator,
        viewModel = viewModel,
    )
    val canShowTopBar by viewModel.collectFieldAsState {
        it.state == DashboardUiState.DASHBOARD_RESULT_OUTPUT_COMPLETED
    }
    DashboardScreen(
        topBar = {
            if (canShowTopBar) {
                TopBarRoute(
                    viewModel = viewModel,
                    scrollBehavior = it,
                )
            }
        },
        content = { innerPadding ->
            ContentRoute(
                viewModel = viewModel,
                innerPadding = innerPadding,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            topBar(scrollBehavior)
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun CollectSideEffect(
    navigator: AppNavigator,
    viewModel: DashboardViewModel
) {
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is DashboardSideEffect.NavigateToAuth -> {
                logger.warn { "navigate to auth called from Dashboard" }
            }

            is DashboardSideEffect.NavigateToCapture -> {
                logger.warn { "navigate to capture called from Dashboard" }
                navigator.navigateToCapture()
            }

            is DashboardSideEffect.NavigateToCardDetail -> {
                navigator.navigateToCardDetail(effect.cardId)
            }

            is DashboardSideEffect.NavigateToCardEdit -> {
                logger.warn { "navigate to edit called from Dashboard" }
            }

            is DashboardSideEffect.NavigateToCardList -> {
                logger.warn { "navigate to card list called from Dashboard" }
            }

            is DashboardSideEffect.NavigateToCollectionDetail -> {
                logger.warn { "navigate to collection detail called from Dashboard" }
            }

            DashboardSideEffect.NavigateToCollectionList -> {
                logger.warn { "navigate to collection list called from Dashboard" }
            }

            DashboardSideEffect.NavigateToReview -> {
                logger.warn { "navigate to review called from Dashboard" }
            }
        }
    }
}

@Preview
@Composable
fun DashboardScreenPreview() {

}
