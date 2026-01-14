package tech.zhifu.app.myhub.ui

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

fun mockWindowSizeClassCompact() = WindowSizeClass(0f, 0f)
fun mockWindowSizeClassMedium() = WindowSizeClass(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND)
fun mockWindowSizeClassExpanded() = WindowSizeClass(WIDTH_DP_EXPANDED_LOWER_BOUND, HEIGHT_DP_EXPANDED_LOWER_BOUND)
