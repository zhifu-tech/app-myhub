package tech.zhifu.app.myhub.dashboard.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.carddetail.CardDetailScreen
import tech.zhifu.app.myhub.core.navigation.AppNavKey
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.core.navigation.FeatureNavKey
import tech.zhifu.app.myhub.dashboard.DashboardScreen

/**
 * Dashboard Entry Provider
 *
 * 提供 Dashboard 相关的导航入口定义
 */
fun EntryProviderScope<NavKey>.dashboardEntry(
    navigator: AppNavigator
) {
    entry<AppNavKey.Dashboard> {
        DashboardScreen(
            onNavigateToCardDetail = { cardId ->
                navigator.navigate(FeatureNavKey.CardDetail(cardId))
            },
        )
    }

    entry<FeatureNavKey.CardDetail> { key ->
        CardDetailScreen(
            cardId = key.cardId,
            onNavigateBack = { navigator.goBack() }
        )
    }
}
