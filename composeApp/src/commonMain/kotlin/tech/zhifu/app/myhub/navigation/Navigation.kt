package tech.zhifu.app.myhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import tech.zhifu.app.myhub.feature.ai.api.navigation.AiNavKey
import tech.zhifu.app.myhub.feature.ai.navigation.aiEntry
import tech.zhifu.app.myhub.feature.dashboard.api.navigation.DashboardNavKey
import tech.zhifu.app.myhub.feature.dashboard.navigation.dashboardEntry
import tech.zhifu.app.myhub.feature.mixed.api.OpenSourceLicensesNavKey
import tech.zhifu.app.myhub.feature.mixed.api.SupportNavKey
import tech.zhifu.app.myhub.feature.mixed.navigation.mixedEntry
import tech.zhifu.app.myhub.feature.settings.api.SettingsNavKey
import tech.zhifu.app.myhub.feature.settings.navigation.settingsEntry

fun navAppStartKey(): NavKey = DashboardNavKey

fun navAppKeySet(): Set<NavKey> = setOf(
    DashboardNavKey,
)

fun navKeySerializerModule() = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(DashboardNavKey::class)
        subclass(AiNavKey::class)
        subclass(OpenSourceLicensesNavKey::class)
        subclass(SupportNavKey::class)
        subclass(SettingsNavKey::class)
    }
}

@Composable
fun AppNavigator.navEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    dashboardEntry(this@navEntryProvider)
    aiEntry(this@navEntryProvider)
    mixedEntry(this@navEntryProvider)
    settingsEntry(this@navEntryProvider)
}
