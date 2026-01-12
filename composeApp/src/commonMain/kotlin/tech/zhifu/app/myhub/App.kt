package tech.zhifu.app.myhub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.AppErrorScreen
import tech.zhifu.app.myhub.component.AppLoadingScreen
import tech.zhifu.app.myhub.core.navigation.AppNavKey
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
import tech.zhifu.app.myhub.ui.WindowSizeClass
import tech.zhifu.app.myhub.ui.calculateWindowSizeClass
import tech.zhifu.app.myhub.ui.getWindowSize
import tech.zhifu.app.myhub.ui.isCompact
import tech.zhifu.app.myhub.ui.isExpanded
import tech.zhifu.app.myhub.ui.isMedium

/**
 * 应用主入口 (Stateful) - 使用 Navigation 3
 */
@Composable
fun App(
    windowSize: DpSize? = null,
    appViewModel: AppViewModel = koinInject()
) {
    // 观察应用状态
    val appState by appViewModel.uiState.collectAsState()

    // 计算窗口大小类
    val actualWindowSize = windowSize ?: getWindowSize()
    val sizeClass = calculateWindowSizeClass(actualWindowSize)

    // 初始化应用
    LaunchedEffect(Unit) {
        appViewModel.initialize(initialWindowSizeClass = sizeClass)
    }

    // 当窗口大小变化时更新 ViewModel
    LaunchedEffect(sizeClass) {
        appViewModel.updateWindowSizeClass(sizeClass)
    }

    App(
        appState = appState,
        windowSizeClass = sizeClass
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

/**
 * 应用主要内容
 */
@Composable
private fun AppContent(
    isDarkTheme: Boolean,
    windowSizeClass: WindowSizeClass
) {
    // 提供应用环境上下文
    LocalAppEnvironment {
        // 提供应用主题状态（供卡片组件等使用）
        CompositionLocalProvider(LocalAppTheme provides isDarkTheme) {
            // 应用主题
            AppTheme(darkTheme = isDarkTheme) {
                // 提供窗口大小类
                ProvideWindowSizeClass(windowSizeClass) {
                    // 初始化 Navigation 3 状态
                    val navigationState = rememberAppNavigationState(
                        startKey = AppNavKey.Dashboard,
                        appKeys = setOf(
                            AppNavKey.Dashboard,
                            AppNavKey.Profile
                        )
                    )

                    val navigator = remember { AppNavigator(navigationState) }

                    val entryProvider = entryProvider {
                        dashboardEntry(navigator)
                        profileEntry(navigator)
                    }

                    val entries = navigationState.toEntries(entryProvider)
                    val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()

                    // 响应式布局
                    ResponsiveAppLayout(
                        windowSizeClass = windowSizeClass,
                        navigationState = navigationState,
                        navigator = navigator,
                        entries = entries,
                        sceneStrategy = sceneStrategy
                    )
                }
            }
        }
    }
}

/**
 * 响应式应用布局
 */
@Composable
private fun ResponsiveAppLayout(
    windowSizeClass: WindowSizeClass,
    navigationState: AppNavigationState,
    navigator: AppNavigator,
    entries: SnapshotStateList<NavEntry<NavKey>>,
    sceneStrategy: ListDetailSceneStrategy<NavKey>
) {
    when {
        windowSizeClass.isCompact -> {
            CompactLayout(
                navigationState = navigationState,
                navigator = navigator,
                entries = entries,
                sceneStrategy = sceneStrategy
            )
        }

        windowSizeClass.isMedium -> {
            MediumLayout(
                navigationState = navigationState,
                navigator = navigator,
                entries = entries,
                sceneStrategy = sceneStrategy
            )
        }

        windowSizeClass.isExpanded -> {
            ExpandedLayout(
                navigationState = navigationState,
                navigator = navigator,
                entries = entries,
                sceneStrategy = sceneStrategy
            )
        }
    }
}

/**
 * 紧凑布局
 */
@Composable
private fun CompactLayout(
    navigationState: AppNavigationState,
    navigator: AppNavigator,
    entries: SnapshotStateList<NavEntry<NavKey>>,
    sceneStrategy: ListDetailSceneStrategy<NavKey>
) {
    androidx.compose.material3.Scaffold(
        bottomBar = {
            AppNavigationBar(
                currentAppKey = navigationState.currentAppKey,
                onNavigate = { key -> navigator.navigate(key) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavDisplay(
                entries = entries,
                sceneStrategy = sceneStrategy,
                onBack = { navigator.goBack() },
            )
        }
    }
}

/**
 * 中等布局（平板）
 */
@Composable
private fun MediumLayout(
    navigationState: AppNavigationState,
    navigator: AppNavigator,
    entries: SnapshotStateList<NavEntry<NavKey>>,
    sceneStrategy: ListDetailSceneStrategy<NavKey>
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 0.dp
        ) {
            AppNavigationRail(
                currentAppKey = navigationState.currentAppKey,
                onNavigate = { key -> navigator.navigate(key) },
                isExpanded = false
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavDisplay(
                entries = entries,
                sceneStrategy = sceneStrategy,
                onBack = { navigator.goBack() },
            )
        }
    }
}

/**
 * 扩展布局
 */
@Composable
private fun ExpandedLayout(
    navigationState: AppNavigationState,
    navigator: AppNavigator,
    entries: SnapshotStateList<NavEntry<NavKey>>,
    sceneStrategy: ListDetailSceneStrategy<NavKey>
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 0.dp
        ) {
            AppNavigationRail(
                currentAppKey = navigationState.currentAppKey,
                onNavigate = { key -> navigator.navigate(key) },
                isExpanded = true
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavDisplay(
                entries = entries,
                sceneStrategy = sceneStrategy,
                onBack = { navigator.goBack() },
            )
        }
    }
}
