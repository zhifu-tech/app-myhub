package tech.zhifu.app.myhub.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
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

/**
 * WindowSizeClass 扩展方法
 */

/**
 * 判断是否为紧凑型（手机）布局
 */
val WindowSizeClass.isCompact: Boolean
    get() = this == WindowSizeClass.Compact

/**
 * 判断是否为中等型（平板）布局
 */
val WindowSizeClass.isMedium: Boolean
    get() = this == WindowSizeClass.Medium

/**
 * 判断是否为扩展型（桌面）布局
 */
val WindowSizeClass.isExpanded: Boolean
    get() = this == WindowSizeClass.Expanded

