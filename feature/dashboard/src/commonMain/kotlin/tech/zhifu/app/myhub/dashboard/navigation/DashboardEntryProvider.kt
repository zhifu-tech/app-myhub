package tech.zhifu.app.myhub.dashboard.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.dashboard.DashboardScreen
import tech.zhifu.app.myhub.feature.carddetail.api.navigateToCardDetail
import tech.zhifu.app.myhub.feature.dashboard.api.DashboardNavKey

fun EntryProviderScope<NavKey>.dashboardEntry(
    navigator: AppNavigator
) {
    entry<DashboardNavKey>(
//        metadata =
    ) {
        DashboardScreen(
            onNavigateToCardDetail = navigator::navigateToCardDetail,
        )
    }
}
