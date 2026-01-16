package tech.zhifu.app.myhub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import tech.zhifu.app.myhub.platform.resources.settings
import tech.zhifu.app.myhub.resources.Res
import tech.zhifu.app.myhub.resources.all_cards
import tech.zhifu.app.myhub.resources.dashboard
import tech.zhifu.app.myhub.resources.favorites
import tech.zhifu.app.myhub.resources.new_card
import tech.zhifu.app.myhub.resources.profile
import tech.zhifu.app.myhub.platform.resources.Res as PlatformRes

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object AllCards : Screen("all_cards", "All Cards")
    object New : Screen("new", "New")
    object Templates : Screen("templates", "Templates")
    object Favorites : Screen("favorites", "Favorites")
    object Settings : Screen("settings", "Settings")
    object Profile : Screen("profile", "Profile")

    /**
     * 卡片详情页
     *
     * @param cardId 卡片 ID
     */
    data class CardDetail(
        val cardId: String
    ) : Screen("card_detail/$cardId", "Card Detail") {
        companion object {
            fun createRoute(cardId: String) = "card_detail/$cardId"
        }
    }
}

sealed class NavItem(val screen: Screen, val icon: ImageVector, val labelKey: StringResource) {
    object Explore : NavItem(Screen.AllCards, Icons.Default.Style, Res.string.all_cards)
    object Dashboard : NavItem(Screen.Dashboard, Icons.Default.Dashboard, Res.string.dashboard)
    object Favorites : NavItem(Screen.Favorites, Icons.Default.Favorite, Res.string.favorites)
    object New : NavItem(Screen.New, Icons.Default.Add, Res.string.new_card)
    object Settings : NavItem(Screen.Settings, Icons.Default.Settings, PlatformRes.string.settings)
    object Profile : NavItem(Screen.Profile, Icons.Default.Person, Res.string.profile)
}
