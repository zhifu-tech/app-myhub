package tech.zhifu.app.myhub.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.settings.content.Content
import tech.zhifu.app.myhub.feature.settings.content.TopBar
import tech.zhifu.app.myhub.navigation.AppNavigator

@Composable
fun SettingsRoute(
    navigator: AppNavigator,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {
    SettingsSideEffect(
        navigator = navigator,
        viewModel = viewModel
    )
    SettingsScaffold(
        viewModel = viewModel
    )
}

@Composable
fun SettingsScaffold(
    viewModel: SettingsViewModel
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                modifier = Modifier,
                viewModel = viewModel
            )
        }
    ) { padding ->
        SettingsContent(
            viewModel = viewModel,
            contentPadding = padding
        )
    }
}

@Composable
fun SettingsContent(
    viewModel: SettingsViewModel,
    contentPadding: PaddingValues
) {
    val state by viewModel.collectFieldAsState { it.state }
    when (state) {
        SettingsUiState.State.CONTENT -> {
            Content(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues = contentPadding)
            )
        }
    }
}

@Composable
private fun SettingsSideEffect(
    navigator: AppNavigator,
    viewModel: SettingsViewModel
) {
    viewModel.collectSharedSideEffect { effect ->
        when (effect) {
            SettingsSideEffect.NavigateBack -> {
                navigator.goBack()
            }
        }
    }
}
