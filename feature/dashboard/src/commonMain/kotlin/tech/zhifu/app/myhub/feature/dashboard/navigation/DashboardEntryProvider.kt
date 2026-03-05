package tech.zhifu.app.myhub.feature.dashboard.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.capture.api.navigation.navigateToCapture
import tech.zhifu.app.myhub.feature.card.api.navigateToCardDetail
import tech.zhifu.app.myhub.feature.dashboard.DashboardRoute
import tech.zhifu.app.myhub.feature.dashboard.api.navigation.DashboardNavKey
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.navigation.AppNavigator

fun EntryProviderScope<NavKey>.dashboardEntry(
    navigator: AppNavigator,
) {
    entry<DashboardNavKey> {
        DashboardRoute(
            onNavigateToCardEdit = {
                logger.warn { "navigate to edit called from Dashboard" }
            },
            onNavigateToCardDetail = navigator::navigateToCardDetail,
            onNavigateToCapture = navigator::navigateToCapture,
            onNavigateToAuth = {
                logger.warn { "navigate to auth called from Dashboard" }
            }
        )
    }
}
