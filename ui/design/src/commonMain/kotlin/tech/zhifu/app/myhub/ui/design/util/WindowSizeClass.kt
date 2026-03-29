package tech.zhifu.app.myhub.ui.design.util

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("CompositionLocal LocalWindowSizeClass not present")
}

@Composable
fun rememberWindowSizeClass(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
): WindowSizeClass {
    val windowSizeClass = windowAdaptiveInfo.windowSizeClass
    val minWidthDp = windowSizeClass.minWidthDp
    val minHeightDp = windowSizeClass.minHeightDp

    return remember(minWidthDp, minHeightDp) {
        windowSizeClass
    }
}

fun WindowSizeClass.isWidthCompact() = minWidthDp == 0

fun WindowSizeClass.isWidthMedium() = minWidthDp == WIDTH_DP_MEDIUM_LOWER_BOUND

fun WindowSizeClass.isWidthExpanded() = minWidthDp == WIDTH_DP_EXPANDED_LOWER_BOUND

fun WindowSizeClass.isWidthAtLeastExpanded() = minWidthDp >= WIDTH_DP_EXPANDED_LOWER_BOUND

fun WindowSizeClass.isWidthLarge() = minWidthDp == WIDTH_DP_LARGE_LOWER_BOUND

fun WindowSizeClass.isWidthAtLeastLarge() = minWidthDp >= WIDTH_DP_LARGE_LOWER_BOUND

fun WindowSizeClass.isWidthExtraLarge() = minWidthDp == WIDTH_DP_EXTRA_LARGE_LOWER_BOUND

fun WindowSizeClass.isHeightCompact() = minHeightDp == 0

fun WindowSizeClass.isHeightMedium() = minHeightDp == HEIGHT_DP_MEDIUM_LOWER_BOUND

fun WindowSizeClass.isHeightExpanded() = minHeightDp == HEIGHT_DP_EXPANDED_LOWER_BOUND
