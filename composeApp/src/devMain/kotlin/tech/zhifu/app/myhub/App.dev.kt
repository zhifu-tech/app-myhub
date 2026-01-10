package tech.zhifu.app.myhub

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.navigation.Screen
import tech.zhifu.app.myhub.ui.WindowSizeClass

@Composable
@Preview
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
