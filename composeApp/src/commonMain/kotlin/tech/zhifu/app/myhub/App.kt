package tech.zhifu.app.myhub

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
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
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.local.LocalAppLocale
import tech.zhifu.app.myhub.local.LocalAppTheme
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.navigation.LoginNavKey
import tech.zhifu.app.myhub.navigation.NavItem
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
fun App(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
) {
//    logger.debug { "App函数调用, 防止调用裂化" } fixme
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
//    logger.debug { "AppEnvironment函数调用, 防止调用裂化" } fixme
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
    authSessionCoordinator: AuthSessionCoordinator = koinInject(),
) {
    logger.debug { "AppContent函数调用" }
    TrackAppStartedEvent()

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addPlatformFileSupport()
            }
            .build()
    }

    AppTheme(darkTheme = isDarkTheme) {
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

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            NavigationSuiteScaffold(
                state = navSuitState,
                navigationSuiteType = navSuitType,
                navigationItems = {
                    NavigationItems(
                        appState = appState,
                        items = navAppKeyItemMap(),
                        currentKey = appState.navigationState.currentAppKey,
                        navSuitType = navSuitType,
                        onNavigate = navigator::navigate
                    )
                },
                primaryActionContent = {
                    PrimaryActionContent()
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
}

private fun AppNavigator.redirectToLogin(loginNavKey: NavKey) {
    state.currentSubStack.run {
        clear()
        add(state.currentAppKey)
        add(loginNavKey)
    }
}

@Composable
private fun PrimaryActionContent() {
    Column(Modifier.padding(start = 20.dp)) {
        FloatingActionButton(
            onClick = { /* Logo，可扩展为回到首页等 */ },
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

@Composable
private fun NavigationItems(
    appState: AppState,
    items: Map<NavKey, NavItem>,
    currentKey: NavKey?,
    navSuitType: NavigationSuiteType,
    onNavigate: (NavKey) -> Unit
) {
    items.forEach { (navKey, navItem) ->
        val selected = navKey == appState.navigationState.currentAppKey
        NavigationSuiteItem(
            navigationSuiteType = navSuitType,
            selected = currentKey == navKey,
            onClick = { onNavigate(navKey) },
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
}
