package tech.zhifu.app.myhub.analytics.provider

import kotlinx.cinterop.ExperimentalForeignApi
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
import tech.zhifu.app.myhub.analytics.provider.umeng.*
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn

/**
 * Umeng (友盟) Analytics Provider iOS 实现
 * 使用友盟统计 iOS SDK
 *
 * 参考文档：https://developer.umeng.com/docs/119267/detail/119508
 *
 * 集成状态：
 * - ✅ Podfile 已配置：UMCommon 和 UMDevice
 * - ✅ cinterop 已配置：可以正确调用 Umeng SDK
 * - ✅ 所有接口方法已实现：使用 cinterop 生成的绑定
 *
 * 注意：使用 cinterop 生成的绑定来调用 Umeng SDK，提供类型安全的 API 调用
 */
@OptIn(ExperimentalForeignApi::class)
class UmengProvider(
    override val name: String,
    override val supportedPlatforms: Set<Platform>,
    override val supportedRegions: Set<Region>,
) : BaseAnalyticsProvider() {
    private val analyticsScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val logger: Logger = logger("Analytics.UmengProvider")

    override suspend fun initialize(config: ProviderConfig) {
        try {
            val appKey = config.appKey ?: config.customParams["appKey"]
            ?: throw IllegalArgumentException("Umeng AppKey is required")

            val channel = config.customParams["channel"] ?: "App Store"

            // 初始化友盟统计 SDK
            // 使用 cinterop 生成的绑定调用 Umeng SDK
            UMConfigure.initWithAppkey(appKey, channel)

            // 设置日志开关（仅在调试模式下启用）
            if (config.customParams["debugMode"] == "true") {
                UMConfigure.setLogEnabled(true)
            }

            markAsReady()
            logger.info { "Umeng Analytics initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Umeng Analytics" }
            throw e
        }
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        if (!isInitialized) {
            logger.warn { "Umeng Analytics not initialized, event dropped: ${event.name}" }
            return
        }

        analyticsScope.launch {
            try {
                // 转换参数为 Umeng 格式（Map<Any?, *>）
                val params = mutableMapOf<Any?, Any?>()
                event.parameters.forEach { (key, value) ->
                    params[key] = convertToUmengValue(value)
                }
                event.value?.let { params["value"] = it.toString() }
                event.currency?.let { params["currency"] = it }

                // 使用 cinterop 生成的绑定调用 MobClick.event:attributes:
                MobClick.event(event.name, attributes = params)
            } catch (e: Exception) {
                logger.error(e) { "Failed to log Umeng event: ${event.name}" }
            }
        }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        if (!isInitialized) {
            logger.warn { "Umeng Analytics not initialized, user property dropped: $key" }
            return
        }

        analyticsScope.launch {
            try {
                // Umeng iOS SDK 不支持直接设置用户属性
                // 可以使用自定义事件来记录用户属性
                val propertyValue = value?.let { convertToUmengValue(it) } ?: ""
                if (propertyValue.isNotEmpty()) {
                    // 使用自定义事件记录用户属性
                    val params = mapOf<Any?, Any?>(key to propertyValue)
                    MobClick.event("user_property", attributes = params)
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to set Umeng user property: $key" }
            }
        }
    }

    override fun setUserId(userId: String?) {
        if (!isInitialized) {
            logger.warn { "Umeng Analytics not initialized, userId dropped" }
            return
        }

        analyticsScope.launch {
            try {
                // Umeng iOS SDK 使用 profileSignInWithPUID 设置用户 ID
                if (userId != null) {
                    MobClick.profileSignInWithPUID(userId)
                } else {
                    // 清除用户 ID
                    MobClick.profileSignOff()
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to set Umeng userId" }
            }
        }
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        if (!isInitialized) {
            logger.warn { "Umeng Analytics not initialized, screen dropped: $screenName" }
            return
        }

        analyticsScope.launch {
            try {
                // Umeng iOS SDK 使用 beginLogPageView 记录页面
                MobClick.beginLogPageView(screenName)

                screenClass?.let {
                    // 使用自定义事件记录 screen_class
                    val params = mapOf<Any?, Any?>("screen_class" to it)
                    MobClick.event("screen_view", attributes = params)
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to set Umeng screen: $screenName" }
            }
        }
    }

    override fun reset() {
        if (!isInitialized) {
            logger.warn { "Umeng Analytics not initialized, reset skipped" }
            return
        }

        analyticsScope.launch {
            try {
                // Umeng iOS SDK 没有直接的 reset 方法
                // 可以清除用户 ID 来重置用户数据
                MobClick.profileSignOff()
            } catch (e: Exception) {
                logger.error(e) { "Failed to reset Umeng Analytics" }
            }
        }
    }

    override fun cleanup() {
        super.cleanup()
        analyticsScope.cancel()
    }

    /**
     * 将 AnalyticsValue 转换为 Umeng 支持的类型（String）
     */
    private fun convertToUmengValue(value: AnalyticsValue): String = when (value) {
        is AnalyticsValue.Str -> value.value
        is AnalyticsValue.Num -> value.value.toString()
        is AnalyticsValue.Int -> value.value.toString()
        is AnalyticsValue.Bool -> value.value.toString()
    }
}
