package tech.zhifu.app.myhub

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.AppErrorScreen
import tech.zhifu.app.myhub.component.AppLoadingScreen
import tech.zhifu.app.myhub.core.navigation.AppNavKey
import tech.zhifu.app.myhub.core.navigation.AppNavKey.Dashboard
import tech.zhifu.app.myhub.core.navigation.AppNavigationState
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.core.navigation.rememberAppNavigationState
import tech.zhifu.app.myhub.core.navigation.toEntries
import tech.zhifu.app.myhub.dashboard.navigation.dashboardEntry
import tech.zhifu.app.myhub.local.LocalAppEnvironment
import tech.zhifu.app.myhub.local.LocalAppTheme
import tech.zhifu.app.myhub.navigation.AppNavigationBar
import tech.zhifu.app.myhub.navigation.AppNavigationRail
import tech.zhifu.app.myhub.navigation.ListDetailSceneStrategy
import tech.zhifu.app.myhub.navigation.rememberListDetailSceneStrategy
import tech.zhifu.app.myhub.profile.navigation.profileEntry
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.ProvideWindowSizeClass
import tech.zhifu.app.myhub.ui.isWidthCompact
import tech.zhifu.app.myhub.ui.rememberWindowSizeClass

@Composable
fun App(
    appViewModel: AppViewModel = koinInject()
) {
    val appState by appViewModel.uiState.collectAsState()
    val windowSizeClass = rememberWindowSizeClass()

    // 初始化应用
    LaunchedEffect(Unit) {
        appViewModel.initialize()
    }

    // 监听窗口大小变化
    LaunchedEffect(windowSizeClass) {
        // 当窗口大小变化时，可以在这里执行相关操作
        // 例如：记录分析事件、更新状态等
        appViewModel.onWindowSizeClassChanged(windowSizeClass)
    }

    App(
        appState = appState,
        windowSizeClass = windowSizeClass
    )
}

/**
 * 应用主入口 (Stateless)
 */
@Composable
fun App(
    appState: AppUiState,
    windowSizeClass: WindowSizeClass
) {
    when (appState) {
        is AppUiState.Loading -> {
            AppLoadingScreen()
        }

        is AppUiState.Ready -> {
            AppContent(
                isDarkTheme = appState.isDarkTheme,
                windowSizeClass = windowSizeClass
            )
        }

        is AppUiState.Error -> {
            AppErrorScreen(
                error = appState.error,
                onRetry = { /* TODO: Handle retry */ }
            )
        }
    }
}

@Composable
private fun AppContent(
    isDarkTheme: Boolean,
    windowSizeClass: WindowSizeClass
) {
    LocalAppEnvironment {
        CompositionLocalProvider(LocalAppTheme provides isDarkTheme) {
            AppTheme(darkTheme = isDarkTheme) {
                ProvideWindowSizeClass(windowSizeClass) {

                    val navigationState = rememberAppNavigationState(
                        startKey = Dashboard,
                        appKeys = setOf(
                            Dashboard,
                            AppNavKey.Profile
                        )
                    )

                    val navigator = remember { AppNavigator(navigationState) }

                    val entryProvider = entryProvider {
                        dashboardEntry(navigator)
                        profileEntry(navigator)
                    }

                    val entries = navigationState.toEntries(entryProvider)

                    // ⭐ SceneStrategy 不再依赖 WindowSizeClass
                    val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()

                    AppScaffold(
                        windowSizeClass = windowSizeClass,
                        navigationState = navigationState,
                        navigator = navigator
                    ) {
                        AppSceneContainer(
                            entries = entries,
                            navigator = navigator,
                            sceneStrategy = sceneStrategy
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AppScaffold(
    windowSizeClass: WindowSizeClass,
    navigationState: AppNavigationState,
    navigator: AppNavigator,
    content: @Composable (PaddingValues) -> Unit
) {
    val showNavigatorBar = windowSizeClass.isWidthCompact()
    val showNavigationRail = showNavigatorBar.not()

    Scaffold(
        bottomBar = {
            if (showNavigatorBar) {
                AppNavigationBar(
                    currentAppKey = navigationState.currentAppKey,
                    onNavigate = navigator::navigate
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Row(Modifier.fillMaxSize()) {
            if (showNavigationRail) {
                AppNavigationRail(
                    windowSizeClass = windowSizeClass,
                    currentAppKey = navigationState.currentAppKey,
                    onNavigate = navigator::navigate,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(if (showNavigatorBar) Modifier.padding(padding) else Modifier)
                    // 限制动画范围，防止溢出到 NavigationRail
                    .clipToBounds()
            ) {
                content(padding)
            }
        }
    }
}

@Composable
private fun AppSceneContainer(
    entries: SnapshotStateList<NavEntry<NavKey>>,
    navigator: AppNavigator,
    sceneStrategy: ListDetailSceneStrategy<NavKey>
) {
    NavDisplay(
        entries = entries,
        sceneStrategy = sceneStrategy,
        onBack = navigator::goBack
    )
}
