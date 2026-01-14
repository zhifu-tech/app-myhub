package tech.zhifu.app.myhub.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

private val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("CompositionLocal LocalWindowSizeClass not present")
}

@Composable
fun ProvideWindowSizeClass(
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
        content()
    }
}

@Composable
fun windowSizeClass(): WindowSizeClass = LocalWindowSizeClass.current

/**
 * 记住并响应窗口大小类别的变化
 * 
 * 当窗口大小改变时，会自动更新返回的 WindowSizeClass
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = adaptiveInfo.windowSizeClass
    // 使用 windowSizeClass 作为 key，当窗口大小变化时会重新计算
    return remember(windowSizeClass) {
        windowSizeClass
    }
}


fun WindowSizeClass.isWidthCompact() = minWidthDp == 0

fun WindowSizeClass.isWidthMedium() = minWidthDp == WIDTH_DP_MEDIUM_LOWER_BOUND

fun WindowSizeClass.isWidthExpanded() = minWidthDp == WIDTH_DP_EXPANDED_LOWER_BOUND

fun WindowSizeClass.isWidthAtLeastExpanded() = minWidthDp >= WIDTH_DP_EXPANDED_LOWER_BOUND

fun WindowSizeClass.isWidthLarge() = minWidthDp == WIDTH_DP_LARGE_LOWER_BOUND

fun WindowSizeClass.isWidthExtraLarge() = minWidthDp == WIDTH_DP_EXTRA_LARGE_LOWER_BOUND

fun WindowSizeClass.isHeightCompact() = minHeightDp == 0

fun WindowSizeClass.isHeightMedium() = minHeightDp == HEIGHT_DP_MEDIUM_LOWER_BOUND

fun WindowSizeClass.isHeightExpanded() = minHeightDp == HEIGHT_DP_EXPANDED_LOWER_BOUND
