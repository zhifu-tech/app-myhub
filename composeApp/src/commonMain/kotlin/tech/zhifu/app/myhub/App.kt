package tech.zhifu.app.myhub

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.LocalAnalyticsService
import tech.zhifu.app.myhub.analytics.TrackAppStartedEvent
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.navigation.navAppKeySet
import tech.zhifu.app.myhub.navigation.navAppStartKey
import tech.zhifu.app.myhub.navigation.navEntryProvider
import tech.zhifu.app.myhub.navigation.navKeySerializerModule
import tech.zhifu.app.myhub.navigation.rememberAppNavigationState
import tech.zhifu.app.myhub.navigation.rememberListDetailSceneStrategy
import tech.zhifu.app.myhub.navigation.toEntries
import tech.zhifu.app.myhub.ui.design.theme.AppTheme
import tech.zhifu.app.myhub.ui.design.util.LocalSharedTransitionScope
import tech.zhifu.app.myhub.ui.design.util.LocalSnackbarState
import tech.zhifu.app.myhub.ui.design.util.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.design.util.rememberWindowSizeClass

@Composable
fun App(
    analyticsService: AnalyticsService = koinInject(),
) {
    val windowSizeClass = rememberWindowSizeClass()
    CompositionLocalProvider(
        LocalAnalyticsService provides analyticsService,
        LocalWindowSizeClass provides windowSizeClass
    ) {
        AppTheme {
            AppContent()
        }
    }
}

@Composable
internal fun AppContent() {
    TrackAppStartedEvent()
    val navigationState = rememberAppNavigationState(
        startKey = navAppStartKey(),
        appKeys = navAppKeySet(),
        navKeysSerializerModule = navKeySerializerModule(),
    )
    val navigator = remember(navigationState) {
        AppNavigator(navigationState)
    }
    val entries = navigationState.toEntries(
        entryProvider = navigator.navEntryProvider()
    )
    val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()
    val snackbarHostState = remember { SnackbarHostState() }
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
            LocalSnackbarState provides snackbarHostState,
        ) {
            NavDisplay(
                entries = entries,
                sceneStrategy = sceneStrategy,
                onBack = navigator::goBack,
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
        )
    }
}
