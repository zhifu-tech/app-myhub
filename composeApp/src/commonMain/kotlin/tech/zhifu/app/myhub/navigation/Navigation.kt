package tech.zhifu.app.myhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import tech.zhifu.app.myhub.feature.capture.api.navigation.CaptureNavKey
import tech.zhifu.app.myhub.feature.capture.navigation.captureEntry
import tech.zhifu.app.myhub.feature.card.api.CardNavKey
import tech.zhifu.app.myhub.feature.card.navigation.cardEntry
import tech.zhifu.app.myhub.feature.dashboard.api.navigation.DashboardNavItem
import tech.zhifu.app.myhub.feature.dashboard.api.navigation.DashboardNavKey
import tech.zhifu.app.myhub.feature.dashboard.navigation.dashboardEntry
import tech.zhifu.app.myhub.feature.profile.api.navigation.ProfileNavItem
import tech.zhifu.app.myhub.feature.profile.api.navigation.ProfileNavKey
import tech.zhifu.app.myhub.feature.profile.navigation.profileEntry

fun navAppStartKey(): NavKey = DashboardNavKey

fun navAppKeySet(): Set<NavKey> = setOf(
    DashboardNavKey,
    ExploreNavKey,
    FavoritesNavKey,
    ProfileNavKey,
)

@Composable
fun navAppKeyItemMap(): Map<NavKey, NavItem> = mapOf(
    DashboardNavKey to DashboardNavItem(),
    ExploreNavKey to ExploreNavItem(),
    FavoritesNavKey to FavoritesNavItem(),
    ProfileNavKey to ProfileNavItem(),
)

fun navKeySerializerModule() = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(DashboardNavKey::class)
        subclass(ExploreNavKey::class)
        subclass(FavoritesNavKey::class)
        subclass(ProfileNavKey::class)
        subclass(CaptureNavKey::class)
        subclass(CardNavKey::class)
    }
}

@Composable
fun AppNavigator.navEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    dashboardEntry(this@navEntryProvider) {
        this@navEntryProvider.navigate(ProfileNavKey)
    }
    entry<ExploreNavKey> { PlaceholderScreen("Explore") }
    entry<FavoritesNavKey> { PlaceholderScreen("Favorites") }
    profileEntry(this@navEntryProvider)
    captureEntry(this@navEntryProvider)
    cardEntry(this@navEntryProvider)
}
