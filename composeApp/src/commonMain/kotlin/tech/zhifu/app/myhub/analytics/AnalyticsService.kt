package tech.zhifu.app.myhub.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import org.koin.compose.koinInject
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
    analyticsService: AnalyticsService = koinInject()
) = DisposableEffect(Unit) {
    analyticsService.logAppStarted()
    onDispose {}
}
