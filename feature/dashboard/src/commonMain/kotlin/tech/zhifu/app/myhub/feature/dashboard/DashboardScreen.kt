package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.feature.dashboard.content.ContentRoute
import tech.zhifu.app.myhub.feature.dashboard.topbar.TopBarRoute
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = koinInject<DashboardViewModel>(),
    onNavigateToCardDetail: (String) -> Unit,
    onNavigateToCardEdit: (String) -> Unit,
    onNavigateToCapture: () -> Unit,
    onNavigateToAuth: () -> Unit,
) {
    logger.debug("Dashboard Screen") { "DashboardScreen" }
    LaunchedEffect(Unit) {
        logger.debug("Dashboard Screen") { "DashboardSideEffect" }
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is DashboardSideEffect.NavigateToAuth -> onNavigateToAuth()
                is DashboardSideEffect.NavigateToCapture -> onNavigateToCapture()
                is DashboardSideEffect.NavigateToCardDetail -> onNavigateToCardDetail(effect.cardId)
                is DashboardSideEffect.NavigateToCardEdit -> onNavigateToCardEdit(effect.cardId)
            }
        }
    }
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
    logger.debug("Dashboard Screen") { "DashboardScreen == 2" }

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
