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
import tech.zhifu.app.myhub.component.LoadingWheel
import tech.zhifu.app.myhub.feature.settings.content.ContentRoute
import tech.zhifu.app.myhub.feature.settings.content.ErrorRoute
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.platform.resources.settings
import tech.zhifu.app.myhub.ui.State
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
        error = { modifier ->
            ErrorRoute(
                viewModel = viewModel,
                modifier = modifier
            )
        },
        content = { modifier ->
            ContentRoute(
                viewModel = viewModel,
                modifier = modifier
            )
        }
    )
}

@Composable
fun SettingsScreen(
    state: State,
    error: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit,
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
        when (state) {
            State.LOADING -> LoadingWheel(
                modifier = Modifier.padding(padding),
                contentDesc = "加载内容", // fixme 翻译
            )

            State.ERROR -> error(
                Modifier.fillMaxSize()
                    .padding(paddingValues = padding)
            )

            State.CONTENT -> content(
                Modifier.fillMaxSize()
                    .padding(paddingValues = padding)
            )

            else -> Unit
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

