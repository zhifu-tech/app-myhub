package tech.zhifu.app.myhub

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.dashboard.DashboardScreen
import tech.zhifu.app.myhub.local.LocalAppEnvironment
import tech.zhifu.app.myhub.navigation.AppNavigationBar
import tech.zhifu.app.myhub.navigation.AppNavigationRail
import tech.zhifu.app.myhub.navigation.Screen
import tech.zhifu.app.myhub.placeholder.PlaceholderScreen
import tech.zhifu.app.myhub.profile.ProfileScreen
import tech.zhifu.app.myhub.settings.SettingsScreen
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.AppErrorScreen
import tech.zhifu.app.myhub.ui.AppLoadingScreen
import tech.zhifu.app.myhub.ui.AppTopBar
import tech.zhifu.app.myhub.ui.ProvideWindowSizeClass
import tech.zhifu.app.myhub.ui.WindowSizeClass
import tech.zhifu.app.myhub.ui.calculateWindowSizeClass
import tech.zhifu.app.myhub.ui.getWindowSize
import tech.zhifu.app.myhub.ui.isCompact
import tech.zhifu.app.myhub.ui.isExpanded
import tech.zhifu.app.myhub.ui.isMedium

/**
 * 应用主入口 (Stateful)
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
        onNavigate = appViewModel::navigateTo,
        onRetry = appViewModel::retry
    )
}

/**
 * 应用主入口 (Stateless) - 方便测试和预览
 */
@Composable
fun App(
    appState: AppUiState,
    onNavigate: (Screen) -> Unit,
    onRetry: () -> Unit
) {
    // 根据加载状态显示不同内容
    when (val state = appState) {
        is AppUiState.Loading -> {
            AppLoadingScreen()
        }

        is AppUiState.Ready -> {
            AppContent(
                currentScreen = state.currentScreen,
                onNavigate = onNavigate,
                isDarkTheme = state.isDarkTheme, // 使用 state 中的主题设置，而不是全局变量
                windowSizeClass = state.windowSizeClass
            )
        }

        is AppUiState.Error -> {
            AppErrorScreen(
                error = state.error,
                onRetry = onRetry
            )
        }
    }
}

/**
 * 应用主要内容
 */
@Composable
private fun AppContent(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    isDarkTheme: Boolean,
    windowSizeClass: WindowSizeClass
) {
    // 提供应用环境上下文
    LocalAppEnvironment {
        // 应用主题
        AppTheme(darkTheme = isDarkTheme) {
            // 提供窗口大小类
            ProvideWindowSizeClass(windowSizeClass) {
                // 响应式布局
                ResponsiveAppLayout(
                    windowSizeClass = windowSizeClass,
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
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
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    when {
        windowSizeClass.isCompact -> {
            CompactLayout(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }

        windowSizeClass.isMedium -> {
            MediumLayout(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }

        windowSizeClass.isExpanded -> {
            ExpandedLayout(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    }
}

/**
 * 紧凑布局（移动端）
 */
@Composable
private fun CompactLayout(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Scaffold(
        topBar = { AppTopBar() },
        bottomBar = {
            AppNavigationBar(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AppNavigation(currentScreen = currentScreen)
        }
    }
}

/**
 * 中等布局（平板）
 */
@Composable
private fun MediumLayout(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AppNavigationRail(
            currentScreen = currentScreen,
            onNavigate = onNavigate,
            isExpanded = false
        )
        Box(modifier = Modifier.weight(1f)) {
            AppNavigation(currentScreen = currentScreen)
        }
    }
}

/**
 * 扩展布局（桌面）
 */
@Composable
private fun ExpandedLayout(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AppNavigationRail(
            currentScreen = currentScreen,
            onNavigate = onNavigate,
            isExpanded = true
        )
        Box(modifier = Modifier.weight(1f)) {
            AppNavigation(currentScreen = currentScreen)
        }
    }
}

/**
 * 应用导航容器
 */
@Composable
fun AppNavigation(currentScreen: Screen) {
    when (currentScreen) {
        is Screen.Dashboard -> DashboardScreen()
        is Screen.Settings -> SettingsScreen()
        is Screen.Profile -> ProfileScreen(
            onNavigateToSettings = { /* TODO: Navigate to Settings */ }
        )

        else -> PlaceholderScreen(currentScreen)
    }
}

@Preview
@Composable
fun AppPreview() {
    // 使用 KoinContext 包装预览，防止 koinInject() 在预览环境中抛出异常
    App(
        appState = AppUiState.Ready(
            currentScreen = Screen.Dashboard,
            isDarkTheme = false,
            windowSizeClass = WindowSizeClass.Expanded
        ),
        onNavigate = {},
        onRetry = {}
    )
}
