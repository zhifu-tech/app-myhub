package tech.zhifu.app.myhub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import tech.zhifu.app.myhub.core.navigation.AppNavigationState
import tech.zhifu.app.myhub.core.navigation.rememberAppNavigationState
import tech.zhifu.app.myhub.navigation.navAppKeySet
import tech.zhifu.app.myhub.navigation.navAppStartKey
import tech.zhifu.app.myhub.navigation.navKeySerializerModule
import tech.zhifu.app.myhub.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.settings.settings.languageSetting
import tech.zhifu.app.myhub.settings.settings.themeSetting


@Composable
fun rememberAppState(
    settingsRepository: SettingsRepository,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
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
        settingsRepository.themeSetting
            ?.observe()
            ?.stateIn(
                scope = stableScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = true,
            )
            ?: flowOf(true)
                .stateIn(
                    scope = stableScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = true,
                )
    }

    val locale = remember(settingsRepository) {
        settingsRepository.languageSetting
            ?.observe()
            ?.stateIn(
                scope = stableScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = "en",
            )
            ?: flowOf("en")
                .stateIn(
                    scope = stableScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = "en",
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
