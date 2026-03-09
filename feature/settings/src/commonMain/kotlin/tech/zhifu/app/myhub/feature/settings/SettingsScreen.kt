package tech.zhifu.app.myhub.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.settings.content.ResultErrorDisabledRoute
import tech.zhifu.app.myhub.feature.settings.content.ResultOutputCompletedRoute
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.platform.resources.settings
import tech.zhifu.app.myhub.ui.State
import tech.zhifu.app.myhub.ui.content.InitGlobalPending
import tech.zhifu.app.myhub.ui.isInitGlobalLoading
import tech.zhifu.app.myhub.ui.isResultErrorDisabled
import tech.zhifu.app.myhub.ui.isResultOutputCompleted
import tech.zhifu.app.myhub.platform.resources.Res as PlatformRes

@Composable
fun SettingsRoute(
    navigator: AppNavigator,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {
    SettingsSideEffect(navigator = navigator, viewModel = viewModel)
    val state by viewModel.collectFieldAsState {
        it.state
    }
    SettingsScreen(
        state = state,
        initGlobalPending = { modifier ->
            InitGlobalPending(
                modifier = modifier
            )
        },
        resultErrorDisabled = { modifier ->
            ResultErrorDisabledRoute(
                viewModel = viewModel,
                modifier = modifier
            )
        },
        resultOutputCompleted = { modifier ->
            ResultOutputCompletedRoute(
                viewModel = viewModel,
                modifier = modifier
            )
        }
    )
}

@Composable
fun SettingsScreen(
    state: State,
    initGlobalPending: @Composable (Modifier) -> Unit,
    resultErrorDisabled: @Composable (Modifier) -> Unit,
    resultOutputCompleted: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(PlatformRes.string.settings))
                }
            )
        }
    ) { padding ->
        when {
            state.isInitGlobalLoading() -> initGlobalPending(
                Modifier.fillMaxSize()
                    .padding(paddingValues = padding)
            )

            state.isResultErrorDisabled() -> resultErrorDisabled(
                Modifier.fillMaxSize()
                    .padding(paddingValues = padding)
            )

            state.isResultOutputCompleted() -> resultOutputCompleted(
                Modifier.fillMaxSize()
                    .padding(paddingValues = padding)
            )
        }
    }
}

@Composable
private fun SettingsSideEffect(
    navigator: AppNavigator,
    viewModel: SettingsViewModel,
) {
    // LEFT-EMPTY NOW
}

