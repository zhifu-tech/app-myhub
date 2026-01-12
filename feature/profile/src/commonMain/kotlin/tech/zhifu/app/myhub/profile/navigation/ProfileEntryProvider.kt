package tech.zhifu.app.myhub.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.core.navigation.AppNavKey
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.profile.ProfileScreen

/**
 * Profile Entry Provider
 *
 * 提供 Profile 相关的导航入口定义
 */
fun EntryProviderScope<NavKey>.profileEntry(
    navigator: AppNavigator
) {
    entry<AppNavKey.Profile> {
        ProfileScreen()
    }
}
