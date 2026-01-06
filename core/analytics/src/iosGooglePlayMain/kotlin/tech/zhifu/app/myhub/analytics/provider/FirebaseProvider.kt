package tech.zhifu.app.myhub.analytics.provider

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.analytics.BaseAnalyticsProvider
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn

/**
 * Firebase Analytics Provider Android 实现
 * 使用 GitLiveApp/firebase-kotlin-sdk
 */
class FirebaseProvider(
    private val logger: Logger = logger("Analytics.FirebaseProvider")
) : BaseAnalyticsProvider() {
    override val name = "Firebase"
    override val supportedPlatforms = setOf(Platform.ANDROID)
    override val supportedRegions = setOf(Region.OVERSEAS)

    private var analytics: FirebaseAnalytics? = null
    private val analyticsScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun initialize(config: ProviderConfig) {
        try {
            // firebase-kotlin-sdk 使用 Firebase.analytics 获取实例
            analytics = Firebase.analytics
            logger.info { "Firebase Analytics initialized successfully" }
            markAsReady()
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Firebase Analytics" }
            throw e
        }
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        val analyticsInstance = analytics ?: run {
            logger.warn { "Firebase Analytics not initialized, event dropped: ${event.name}" }
            return
        }

        analyticsScope.launch {
            try {
                // 转换参数为 Firebase 格式
                val params = mutableMapOf<String, Any>()

                // 添加事件参数
                event.parameters.forEach { (key, value) ->
                    params[key] = convertToFirebaseValue(value)
                }

                // 添加 value 和 currency（如果存在）
                event.value?.let { params["value"] = it }
                event.currency?.let { params["currency"] = it }

                // 记录事件
                analyticsInstance.logEvent(event.name) {
                    params.forEach { (key, value) ->
                        when (value) {
                            is String -> param(key, value)
                            is Long -> param(key, value)
                            is Double -> param(key, value)
                            is Boolean -> param(key, value)
                            else -> param(key, value.toString())
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to log Firebase event: ${event.name}" }
            }
        }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        val analyticsInstance = analytics ?: run {
            logger.warn { "Firebase Analytics not initialized, user property dropped: $key" }
            return
        }

        analyticsScope.launch {
            try {
                // Firebase setUserProperty 需要非 null 的 String，如果 value 为 null 则不设置
                val firebaseValue = value?.let { convertToFirebaseValue(it).toString() }
                if (firebaseValue != null) {
                    analyticsInstance.setUserProperty(key, firebaseValue)
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to set Firebase user property: $key" }
            }
        }
    }

    override fun setUserId(userId: String?) {
        val analyticsInstance = analytics ?: run {
            logger.warn { "Firebase Analytics not initialized, userId dropped" }
            return
        }

        analyticsScope.launch {
            try {
                analyticsInstance.setUserId(userId)
            } catch (e: Exception) {
                logger.error(e) { "Failed to set Firebase userId" }
            }
        }
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        val analyticsInstance = analytics ?: run {
            logger.warn { "Firebase Analytics not initialized, screen dropped: $screenName" }
            return
        }

        analyticsScope.launch {
            try {
                // Firebase Analytics 使用 logEvent 记录 screen_view 事件
                // setScreenName 已被弃用
                analyticsInstance.logEvent("screen_view") {
                    param("screen_name", screenName)
                    screenClass?.let { param("screen_class", it) }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to set Firebase screen: $screenName" }
            }
        }
    }

    override fun reset() {
        val analyticsInstance = analytics ?: run {
            logger.warn { "Firebase Analytics not initialized, reset skipped" }
            return
        }

        analyticsScope.launch {
            try {
                analyticsInstance.resetAnalyticsData()
            } catch (e: Exception) {
                logger.error(e) { "Failed to reset Firebase Analytics" }
            }
        }
    }

    override fun cleanup() {
        super.cleanup()
        analyticsScope.cancel()
    }

    /**
     * 将 AnalyticsValue 转换为 Firebase 支持的类型
     */
    private fun convertToFirebaseValue(value: AnalyticsValue): Any = when (value) {
        is AnalyticsValue.Str -> value.value
        is AnalyticsValue.Num -> value.value
        is AnalyticsValue.Int -> value.value
        is AnalyticsValue.Bool -> value.value
    }
}
