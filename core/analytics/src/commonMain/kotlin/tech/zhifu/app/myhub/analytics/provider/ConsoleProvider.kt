package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsProvider
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.analytics.BaseAnalyticsProvider
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.Region

/**
 * 控制台输出 Provider
 * 用于 Desktop 平台和测试环境
 */
class ConsoleProvider : BaseAnalyticsProvider() {
    override val name = "Console"
    override val supportedPlatforms = setOf(Platform.JVM, Platform.JS, Platform.WASM)
    override val supportedRegions = setOf(Region.DOMESTIC, Region.OVERSEAS)

    override suspend fun initialize(config: ProviderConfig) {
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        println("[Analytics] Event: ${event.name}")
        event.parameters.forEach { (key, value) ->
            println("  $key: ${formatValue(value)}")
        }
        event.value?.let { println("  value: $it") }
        event.currency?.let { println("  currency: $it") }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        println("[Analytics] UserProperty: $key = ${value?.let { formatValue(it) } ?: "null"}")
    }

    override fun setUserId(userId: String?) {
        println("[Analytics] UserId: $userId")
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        println("[Analytics] Screen: $screenName${screenClass?.let { " (class: $it)" } ?: ""}")
    }

    override fun reset() {
        println("[Analytics] Reset")
    }

    private fun formatValue(value: AnalyticsValue): String = when (value) {
        is AnalyticsValue.Str -> value.value
        is AnalyticsValue.Num -> value.value.toString()
        is AnalyticsValue.Int -> value.value.toString()
        is AnalyticsValue.Bool -> value.value.toString()
    }
}
