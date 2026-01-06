package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.analytics.BaseAnalyticsProvider
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger

/**
 * 控制台输出 Provider
 * 用于所有平台的测试和调试环境
 * 通过 logger 输出统计事件，便于开发和调试
 */
class ConsoleProvider(
    private val logger: Logger = logger("Analytics.ConsoleProvider")
) : BaseAnalyticsProvider() {
    override val name = "Console"
    override val supportedPlatforms = setOf(
        Platform.ANDROID,
        Platform.IOS,
        Platform.JVM,
        Platform.JS,
        Platform.WASM
    )
    override val supportedRegions = setOf(Region.DOMESTIC, Region.OVERSEAS)

    override suspend fun initialize(config: ProviderConfig) {
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        val params = buildString {
            append("Event: ${event.name}")
            if (event.parameters.isNotEmpty()) {
                append("\n  Parameters:")
                event.parameters.forEach { (key, value) ->
                    append("\n    $key: ${formatValue(value)}")
                }
            }
            event.value?.let { append("\n  value: $it") }
            event.currency?.let { append("\n  currency: $it") }
        }
        logger.info { params }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        logger.info { "UserProperty: $key = ${value?.let { formatValue(it) } ?: "null"}" }
    }

    override fun setUserId(userId: String?) {
        logger.info { "UserId: $userId" }
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        val screenInfo = buildString {
            append("Screen: $screenName")
            screenClass?.let { append(" (class: $it)") }
        }
        logger.info { screenInfo }
    }

    override fun reset() {
        logger.info { "Reset" }
    }

    private fun formatValue(value: AnalyticsValue): String = when (value) {
        is AnalyticsValue.Str -> value.value
        is AnalyticsValue.Num -> value.value.toString()
        is AnalyticsValue.Int -> value.value.toString()
        is AnalyticsValue.Bool -> value.value.toString()
    }
}
