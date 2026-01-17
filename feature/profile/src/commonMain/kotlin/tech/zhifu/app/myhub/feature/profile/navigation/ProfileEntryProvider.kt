package tech.zhifu.app.myhub.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.profile.ProfileScreen
import tech.zhifu.app.myhub.feature.profile.api.navigation.ProfileNavKey
import tech.zhifu.app.myhub.navigation.AppNavigator

/**
 * Profile Entry Provider
 *
 * 提供 Profile 相关的导航入口定义
 */
fun EntryProviderScope<NavKey>.profileEntry(
    navigator: AppNavigator
) {
    entry<ProfileNavKey>() {
        ProfileScreen()
    }
}
