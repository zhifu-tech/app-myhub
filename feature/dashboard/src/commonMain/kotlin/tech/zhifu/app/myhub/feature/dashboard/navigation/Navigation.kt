package tech.zhifu.app.myhub.feature.dashboard.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.dashboard.DashboardRoute
import tech.zhifu.app.myhub.feature.dashboard.api.navigation.DashboardNavKey
import tech.zhifu.app.myhub.navigation.AppNavigator

fun EntryProviderScope<NavKey>.dashboardEntry(
    navigator: AppNavigator,
) {
    entry<DashboardNavKey> {
        DashboardRoute(
            navigator = navigator,
        )
    }
}
