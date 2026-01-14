package tech.zhifu.app.myhub

import tech.zhifu.app.myhub.navigation.Screen

/**
 * 应用 UI 状态
 *
 * 表示应用的整体状态，包括加载、就绪和错误状态
 *
 * 注意：WindowSizeClass 不再存储在 AppUiState 中，而是从 Composable 上下文获取
 */
sealed class AppUiState {
    /**
     * 加载状态
     * 应用正在初始化
     */
    object Loading : AppUiState()

    /**
     * 就绪状态
     * 应用已初始化完成，可以正常使用
     */
    data class Ready(
        val currentScreen: Screen,
        val isDarkTheme: Boolean
    ) : AppUiState()

    /**
     * 错误状态
     * 应用初始化失败
     */
    data class Error(
        val error: Throwable
    ) : AppUiState()
}


