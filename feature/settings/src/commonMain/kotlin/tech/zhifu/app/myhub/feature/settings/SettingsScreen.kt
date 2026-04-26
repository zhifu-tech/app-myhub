package tech.zhifu.app.myhub.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.mixed.api.navigateToOpenSourceLicenses
import tech.zhifu.app.myhub.feature.mixed.api.navigateToSupport
import tech.zhifu.app.myhub.feature.settings.content.Content
import tech.zhifu.app.myhub.feature.settings.content.TopBar
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.ui.viewmodel.CollectPredicatedSharedSideEffect
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun SettingsScreen(
    navigator: AppNavigator,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {
    SettingsSideEffect(
        navigator = navigator,
        viewModel = viewModel
    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                modifier = Modifier,
                navigator = navigator,
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
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        it.state
    }
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
    viewModel.sideEffect.CollectPredicatedSharedSideEffect { effect ->
        when (effect) {
            SettingsSideEffect.NavigateToOpenSourceLicenses -> {
                navigator.navigateToOpenSourceLicenses()
            }

            SettingsSideEffect.NavigateToSupport -> {
                navigator.navigateToSupport()
            }
        }
    }
}
