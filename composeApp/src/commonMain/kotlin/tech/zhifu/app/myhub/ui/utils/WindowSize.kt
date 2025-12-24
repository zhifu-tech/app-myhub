package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * 窗口尺寸类别，用于响应式布局
 */
enum class WindowSizeClass {
    Compact,    // 手机 (< 600dp)
    Medium,     // 平板 (600dp - 840dp)
    Expanded    // 桌面 (> 840dp)
}

/**
 * 计算窗口尺寸类别
 */
@Composable
fun calculateWindowSizeClass(width: DpSize): WindowSizeClass {
    val widthDp = width.width
    return when {
        widthDp < 600.dp -> WindowSizeClass.Compact
        widthDp < 840.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}

/**
 * Local 提供窗口尺寸类别
 */
val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

/**
 * 提供窗口尺寸类别的 Composable
 */
@Composable
fun ProvideWindowSizeClass(
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
        content()
    }
}

/**
 * 获取当前窗口尺寸类别
 */
@Composable
fun windowSizeClass(): WindowSizeClass = LocalWindowSizeClass.current
