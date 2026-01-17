package tech.zhifu.app.myhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
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
    ProfileNavKey,
)

@Composable
fun navAppKeyItemMap(): Map<NavKey, NavItem> = mapOf(
    DashboardNavKey to DashboardNavItem(),
    ProfileNavKey to ProfileNavItem(),
)

fun navKeySerializerModule() = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(DashboardNavKey::class)
        subclass(ProfileNavKey::class)
        subclass(CardNavKey::class)
    }
}

@Composable
fun AppNavigator.navEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    dashboardEntry(this@navEntryProvider)
    profileEntry(this@navEntryProvider)
    cardEntry(this@navEntryProvider)
}
