package tech.zhifu.app.myhub

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.analytics.TrackAppStartedEvent
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.navigation.navAppKeySet
import tech.zhifu.app.myhub.navigation.navAppStartKey
import tech.zhifu.app.myhub.navigation.navEntryProvider
import tech.zhifu.app.myhub.navigation.navKeySerializerModule
import tech.zhifu.app.myhub.navigation.rememberAppNavigationState
import tech.zhifu.app.myhub.navigation.rememberPageForwardTransitionSpec
import tech.zhifu.app.myhub.navigation.rememberPagePopTransitionSpec
import tech.zhifu.app.myhub.navigation.rememberPagePredictivePopTransitionSpec
import tech.zhifu.app.myhub.navigation.toEntries
import tech.zhifu.app.myhub.ui.design.language.LocalAppLocale
import tech.zhifu.app.myhub.ui.design.theme.AppTheme
import tech.zhifu.app.myhub.ui.design.util.LocalSnackbarState
import tech.zhifu.app.myhub.ui.state.theme.collectAsDarkThemeStateWithLifecycle

@Composable
fun App(
    appViewModel: AppViewModel = koinViewModel(),
) {
    val darkTheme by appViewModel.theme.collectAsDarkThemeStateWithLifecycle()
    val appLanguage by appViewModel.language.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalAppLocale provides appLanguage.languageTag,
    ) {
        AppTheme(darkTheme = darkTheme) {
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
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(
        LocalSnackbarState provides snackbarHostState,
    ) {
        NavDisplay(
            entries = entries,
            onBack = navigator::goBack,
            transitionSpec = rememberPageForwardTransitionSpec(),
            popTransitionSpec = rememberPagePopTransitionSpec(),
            predictivePopTransitionSpec = rememberPagePredictivePopTransitionSpec()
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
        )
    }
}
