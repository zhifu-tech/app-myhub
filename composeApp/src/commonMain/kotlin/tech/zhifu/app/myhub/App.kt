package tech.zhifu.app.myhub

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.LocalSharedTransitionScope
import tech.zhifu.app.myhub.ui.LocalSnabackbarState
import tech.zhifu.app.myhub.ui.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.rememberWindowSizeClass

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
    val navigator = AppNavigator(navigationState)
    val entries = navigationState.toEntries(entryProvider = navigator.navEntryProvider())
    val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()
    val snackbarHostState = remember { SnackbarHostState() }
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
            LocalSnabackbarState provides snackbarHostState,
        ) {
            NavDisplay(
                entries = entries,
                sceneStrategy = sceneStrategy,
                onBack = navigator::goBack,
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
            )
        }
    }
}
