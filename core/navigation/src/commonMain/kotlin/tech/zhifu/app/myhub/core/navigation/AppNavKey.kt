package tech.zhifu.app.myhub.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 应用级导航键
 *
 * 用于标识应用级导航项（如 Dashboard、Profile），这些键作为 AppStack 的元素。
 * 应用级导航键只能作为 BackStack 的根，不允许作为子级 push。
 *
 * 注意：所有实现必须标记 @Serializable，以便 rememberNavBackStack 能够保存和恢复状态。
 */
@Serializable
sealed interface AppNavKey : NavKey {
    @Serializable
    data object Dashboard : AppNavKey

    @Serializable
    data object Profile : AppNavKey
}

/**
 * 功能导航键
 *
 * 用于标识功能级别的导航目的地（如 CardDetail、AllCards），这些键作为 SubStack 的元素。
 *
 * 注意：所有实现必须标记 @Serializable，以便 rememberNavBackStack 能够保存和恢复状态。
 */
@Serializable
sealed interface FeatureNavKey : NavKey {
    @Serializable
    data class CardDetail(val cardId: String) : FeatureNavKey

    @Serializable
    data class AllCards(val filter: String? = null) : FeatureNavKey
}
