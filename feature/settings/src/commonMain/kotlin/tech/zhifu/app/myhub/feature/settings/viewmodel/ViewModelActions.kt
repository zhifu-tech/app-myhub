package tech.zhifu.app.myhub.feature.settings.viewmodel

import tech.zhifu.app.myhub.feature.settings.SettingsSideEffect
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel


fun SettingsViewModel.navigateBack() = intent {
    postSideEffect(SettingsSideEffect.NavigateBack)
}

fun SettingsViewModel.navigateToOpenSourceLicenses() = intent {
    postSideEffect(SettingsSideEffect.NavigateToOpenSourceLicenses)
}

fun SettingsViewModel.navigateToSupport() = intent {
    postSideEffect(SettingsSideEffect.NavigateToSupport)
}
