package tech.zhifu.app.myhub

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.LocalAnalyticsService
import tech.zhifu.app.myhub.analytics.TrackAppStartedEvent
import tech.zhifu.app.myhub.core.navigation.AppNavKey
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.core.navigation.FeatureNavKey
import tech.zhifu.app.myhub.core.navigation.toEntries
import tech.zhifu.app.myhub.dashboard.navigation.dashboardEntry
import tech.zhifu.app.myhub.local.LocalAppLocale
import tech.zhifu.app.myhub.local.LocalAppTheme
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.navigation.NavItem
import tech.zhifu.app.myhub.navigation.rememberListDetailSceneStrategy
import tech.zhifu.app.myhub.profile.navigation.profileEntry
import tech.zhifu.app.myhub.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.isHeightCompact
import tech.zhifu.app.myhub.ui.isWidthCompact
import tech.zhifu.app.myhub.ui.rememberWindowSizeClass

@Composable
fun App(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
) {
    logger.debug { "App函数调用, 防止调用裂化" }
    val windowSizeClass = rememberWindowSizeClass(windowAdaptiveInfo)
    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass
    ) {
        AppEnvironment(windowSizeClass)
    }
}

@Composable
private fun AppEnvironment(
    windowSizeClass: WindowSizeClass,
    analyticsService: AnalyticsService = koinInject(),
    settingsRepository: SettingsRepository = koinInject(),
) {
    logger.debug { "AppEnvironment函数调用, 防止调用裂化" }
    val appState = rememberAppState(settingsRepository)
    val isDarkTheme by appState.isDarkTheme.collectAsState()
    val locale by appState.locale.collectAsState()

    CompositionLocalProvider(
        LocalAnalyticsService provides analyticsService,
        LocalAppTheme provides isDarkTheme,
        LocalAppLocale provides locale,
    ) {
        AppContent(
            appState = appState,
            isDarkTheme = isDarkTheme,
            windowSizeClass = windowSizeClass
        )
    }
}

@Composable
private fun AppContent(
    appState: AppState,
    isDarkTheme: Boolean,
    windowSizeClass: WindowSizeClass,
) {
    logger.debug { "AppContent函数调用" }
    TrackAppStartedEvent()

    AppTheme(darkTheme = isDarkTheme) {
        val navigator = AppNavigator(appState.navigationState)
        val entries = appState.navigationState.toEntries(entryProvider {
            dashboardEntry(navigator)
            profileEntry(navigator)
        })
        val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()
        val navSuitState = rememberNavigationSuiteScaffoldState()
        val navSuitType = when {
            windowSizeClass.isWidthCompact() -> NavigationSuiteType.ShortNavigationBarCompact
            windowSizeClass.isHeightCompact() -> NavigationSuiteType.ShortNavigationBarMedium
            else -> NavigationSuiteType.WideNavigationRailCollapsed
        }

        NavigationSuiteScaffold(
            state = navSuitState,
            navigationSuiteType = navSuitType,
            navigationItems = {
                NavigationItems(
                    items = navigationItems,
                    currentKey = appState.navigationState.currentAppKey,
                    navSuitType = navSuitType,
                    onNavigate = navigator::navigate
                )
            },
            primaryActionContent = {
                PrimaryActionContent(navSuitType = navSuitType)
            }
        ) {
            NavDisplay(
                entries = entries,
                sceneStrategy = sceneStrategy,
                onBack = navigator::goBack
            )
        }
    }
}

private val navigationItems = mapOf(
    AppNavKey.Dashboard to NavItem.Dashboard,
    AppNavKey.Profile to NavItem.Profile,
    FeatureNavKey.AllCards to NavItem.Explore,
    FeatureNavKey.CardDetail to NavItem.Favorites
)

@Composable
private fun NavigationItems(
    items: Map<Any, NavItem>,
    currentKey: NavKey?,
    navSuitType: NavigationSuiteType,
    onNavigate: (NavKey) -> Unit
) {
    items.forEach { (navKey, navItem) ->
        NavigationSuiteItem(
            navigationSuiteType = navSuitType,
            selected = currentKey == navKey,
            onClick = { onNavigate(navKey as NavKey) },
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = navItem.icon,
                    contentDescription = stringResource(navItem.labelKey)
                )
            },
            label = {
                Text(
                    text = stringResource(navItem.labelKey),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        )
    }
}

@Composable
private fun PrimaryActionContent(
    navSuitType: NavigationSuiteType
) {
    when {
        navSuitType.toString().contains("NavigationRail") -> {
            Box(Modifier.padding(start = 20.dp)) {
                FloatingActionButton(
                    onClick = {},
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null
                    )
                }
            }
        }

        else -> {
            Box(Modifier.padding(start = 20.dp)) {
                FloatingActionButton(
                    onClick = {},
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
