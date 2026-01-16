package tech.zhifu.app.myhub.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import tech.zhifu.app.myhub.config.AppBuildConfig

fun AnalyticsService.logAppStarted() {
    logEvent(
        AnalyticsEvent(
            name = "app_started",
            parameters = mapOf(
                "environment" to AnalyticsValue.Str(AppBuildConfig.appEnv),
                "version_type" to AnalyticsValue.Str(AppBuildConfig.appTier)
            )
        )
    )
}

@Composable
fun TrackAppStartedEvent(
    analyticsService: AnalyticsService = LocalAnalyticsService.current
) = DisposableEffect(Unit) {
    analyticsService.logAppStarted()
    onDispose {}
}
