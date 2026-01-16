package tech.zhifu.app.myhub.analytics

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAnalyticsService = staticCompositionLocalOf<AnalyticsService> {
    error("No AnalyticsService provided")
}
