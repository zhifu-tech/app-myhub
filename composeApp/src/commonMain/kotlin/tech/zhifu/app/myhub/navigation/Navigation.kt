package tech.zhifu.app.myhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import tech.zhifu.app.myhub.carddetail.navigation.cardEntry
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.core.navigation.NavItem
import tech.zhifu.app.myhub.dashboard.navigation.dashboardEntry
import tech.zhifu.app.myhub.feature.carddetail.api.CardNavKey
import tech.zhifu.app.myhub.feature.dashboard.api.DashboardNavItem
import tech.zhifu.app.myhub.feature.dashboard.api.DashboardNavKey
import tech.zhifu.app.myhub.feature.dashboard.api.navigation.ProfileNavItem
import tech.zhifu.app.myhub.feature.dashboard.api.navigation.ProfileNavKey
import tech.zhifu.app.myhub.profile.navigation.profileEntry

fun navAppStartKey(): NavKey = DashboardNavKey

fun navAppKeySet() = setOf<NavKey>(
    DashboardNavKey,
    ProfileNavKey,
)

@Composable
fun navAppKeyItemMap() = mapOf<NavKey, NavItem>(
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
