package tech.zhifu.app.myhub.feature.settings

sealed interface SettingsSideEffect {
    object NavigateToOpenSourceLicenses : SettingsSideEffect
    object NavigateToSupport : SettingsSideEffect
}
