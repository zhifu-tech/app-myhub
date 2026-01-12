package tech.zhifu.app.myhub

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.navigation.Screen
import tech.zhifu.app.myhub.ui.WindowSizeClass

@Composable
@Preview
fun AppPreview() {
    // 使用 KoinContext 包装预览，防止 koinInject() 在预览环境中抛出异常
    // 注意：新的 App 使用 Navigation 3，需要完整的 Koin 上下文
    // 预览可能需要模拟 AppViewModel 或使用其他方式
    App(
        appState = AppUiState.Ready(
            currentScreen = Screen.Dashboard, // 保留用于兼容，但新导航系统不使用
            isDarkTheme = false,
            windowSizeClass = WindowSizeClass.Expanded
        ),
        windowSizeClass = WindowSizeClass.Expanded
    )
}
