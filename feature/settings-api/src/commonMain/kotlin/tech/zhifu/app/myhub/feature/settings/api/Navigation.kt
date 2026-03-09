package tech.zhifu.app.myhub.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.navigation.AppNavigator

@Serializable
object SettingsNavKey : NavKey

fun AppNavigator.navigateToSettings() {
    navigate(SettingsNavKey)
}
