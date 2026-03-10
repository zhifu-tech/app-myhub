package tech.zhifu.app.myhub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.LocalAnalyticsService
import tech.zhifu.app.myhub.analytics.TrackAppStartedEvent
import tech.zhifu.app.myhub.feature.auth.api.session.AuthSessionCoordinator
import tech.zhifu.app.myhub.feature.auth.api.session.AuthSessionEvent
import tech.zhifu.app.myhub.feature.settings.api.navigateToSettings
import tech.zhifu.app.myhub.local.LocalAppLocale
import tech.zhifu.app.myhub.local.LocalAppTheme
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.navigation.LoginNavKey
import tech.zhifu.app.myhub.navigation.navAppKeyItemMap
import tech.zhifu.app.myhub.navigation.navEntryProvider
import tech.zhifu.app.myhub.navigation.rememberListDetailSceneStrategy
import tech.zhifu.app.myhub.navigation.toEntries
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.isHeightCompact
import tech.zhifu.app.myhub.ui.isWidthCompact
import tech.zhifu.app.myhub.ui.rememberWindowSizeClass

@Composable
fun App() {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = rememberWindowSizeClass(windowAdaptiveInfo)
    AppEnvironment(windowSizeClass)
}

@Composable
internal fun AppEnvironment(
    windowSizeClass: WindowSizeClass,
    analyticsService: AnalyticsService = koinInject(),
) {
    val appState = rememberAppState()
    val isDarkTheme by appState.isDarkTheme.collectAsState()
    val locale by appState.locale.collectAsState()

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass,
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
    authSessionCoordinator: AuthSessionCoordinator = koinInject(),
) {
    TrackAppStartedEvent()

    AppTheme(darkTheme = isDarkTheme) {
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    addPlatformFileSupport()
                }
                .build()
        }
        val navigator = AppNavigator(appState.navigationState)
        LaunchedEffect(authSessionCoordinator, navigator) {
            authSessionCoordinator.events.collect { event ->
                if (event is AuthSessionEvent.Expired) {
                    navigator.redirectToLogin(LoginNavKey)
                }
            }
        }
        val entries = appState.navigationState.toEntries(navigator.navEntryProvider())
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
                navAppKeyItemMap().forEach { (navKey, navItem) ->
                    val selected = navKey == appState.navigationState.currentAppKey
                    NavigationSuiteItem(
                        navigationSuiteType = navSuitType,
                        selected = selected,
                        onClick = { navigator.navigate(navKey) },
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = if (selected) navItem.selectedIcon else navItem.unselectedIcon,
                                contentDescription = navItem.iconText
                            )
                        },
                        label = {
                            Text(
                                text = navItem.iconText,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            },
            primaryActionContent = {
                Column(Modifier.padding(start = 20.dp)) {
                    FloatingActionButton(
                        onClick = { navigator.navigateToSettings() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
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

private fun AppNavigator.redirectToLogin(loginNavKey: NavKey) {
    state.currentSubStack.run {
        clear()
        add(state.currentAppKey)
        add(loginNavKey)
    }
}
