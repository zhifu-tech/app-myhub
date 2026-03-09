package tech.zhifu.app.myhub.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.settings.SettingsRoute
import tech.zhifu.app.myhub.feature.settings.api.SettingsNavKey
import tech.zhifu.app.myhub.navigation.AppNavigator

fun EntryProviderScope<NavKey>.settingsEntry(navigator: AppNavigator) {
    entry<SettingsNavKey> {
        SettingsRoute(navigator = navigator)
    }
}
