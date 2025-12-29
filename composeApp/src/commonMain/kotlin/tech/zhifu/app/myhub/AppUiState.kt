package tech.zhifu.app.myhub

import tech.zhifu.app.myhub.navigation.Screen
import tech.zhifu.app.myhub.ui.WindowSizeClass

/**
 * 应用 UI 状态
 *
 * 表示应用的整体状态，包括加载、就绪和错误状态
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
        val isDarkTheme: Boolean,
        val windowSizeClass: WindowSizeClass
    ) : AppUiState()

    /**
     * 错误状态
     * 应用初始化失败
     */
    data class Error(
        val error: Throwable
    ) : AppUiState()
}

