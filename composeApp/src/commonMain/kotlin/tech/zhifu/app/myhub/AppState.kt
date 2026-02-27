package tech.zhifu.app.myhub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.feature.settings.settings.languageSetting
import tech.zhifu.app.myhub.feature.settings.settings.themeSetting
import tech.zhifu.app.myhub.language.AppLocale
import tech.zhifu.app.myhub.navigation.AppNavigationState
import tech.zhifu.app.myhub.navigation.navAppKeySet
import tech.zhifu.app.myhub.navigation.navAppStartKey
import tech.zhifu.app.myhub.navigation.navKeySerializerModule
import tech.zhifu.app.myhub.navigation.rememberAppNavigationState

@Composable
fun rememberAppState(
    settingsRepository: SettingsRepository,
): AppState {
    val navigationState = rememberAppNavigationState(
        startKey = navAppStartKey(),
        appKeys = navAppKeySet(),
        navKeysSerializerModule = navKeySerializerModule(),
    )

    val stableScope = remember {
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    val isDarkTheme = remember(settingsRepository) {
        settingsRepository.themeSetting.observe().stateIn(
            scope = stableScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )
    }

    val locale = remember(settingsRepository) {
        settingsRepository.languageSetting.observe().stateIn(
            scope = stableScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppLocale.DEFAULT,
        )
    }

    return remember(navigationState, isDarkTheme, locale) {
        AppState(
            navigationState = navigationState,
            isDarkTheme = isDarkTheme,
            locale = locale,
        )
    }
}

@Stable
class AppState(
    val navigationState: AppNavigationState,
    val isDarkTheme: StateFlow<Boolean>,
    val locale: StateFlow<String>,
) {
    override fun toString(): String {
        return "AppState(navigationState=$navigationState)"
    }
}
