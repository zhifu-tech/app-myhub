package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import tech.zhifu.app.myhub.component.LoadingWheel
import tech.zhifu.app.myhub.feature.capture.api.navigation.navigateToCapture
import tech.zhifu.app.myhub.feature.card.api.navigateToCardDetail
import tech.zhifu.app.myhub.feature.dashboard.content.ContentRoute
import tech.zhifu.app.myhub.feature.dashboard.content.ErrorRoute
import tech.zhifu.app.myhub.feature.dashboard.topbar.TopBarRoute
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.ui.State

@Composable
fun DashboardRoute(
    navigator: AppNavigator,
    viewModel: DashboardViewModel = koinViewModel<DashboardViewModel>(),
) {
    DashboardSideEffect(navigator = navigator, viewModel = viewModel)
    val state by viewModel.collectFieldAsState {
        it.state
    }
    DashboardScreen(
        state = state,
        topBar = {
            TopBarRoute(
                viewModel = viewModel,
                scrollBehavior = it,
            )
        },
        error = { modifier ->
            ErrorRoute(
                modifier = modifier,
                viewModel = viewModel,
            )
        },
        content = { modifier ->
            ContentRoute(
                modifier = modifier,
                viewModel = viewModel,
            )
        },
    )
}

@Composable
internal fun DashboardScreen(
    state: State,
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit,
    error: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val canShowTopBar = state == State.CONTENT
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scaffoldModifier = if (canShowTopBar) {
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        Modifier
    }

    Scaffold(
        modifier = scaffoldModifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (canShowTopBar) {
                topBar(scrollBehavior)
            }
        },
    ) { innerPadding ->

        when (state) {
            State.LOADING -> LoadingWheel(
                modifier = Modifier.padding(innerPadding),
                contentDesc = "加载内容", // fixme 翻译
            )

            State.ERROR -> error(
                Modifier.padding(innerPadding)
            )

            State.CONTENT -> content(
                Modifier.padding(innerPadding)
            )

            else -> Unit
        }
    }
}

@Composable
private fun DashboardSideEffect(
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
