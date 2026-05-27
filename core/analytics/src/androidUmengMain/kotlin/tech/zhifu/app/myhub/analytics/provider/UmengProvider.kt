package tech.zhifu.app.myhub.analytics.provider

import android.content.Context
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
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
 * Umeng (友盟) Analytics Provider Android 实现
 * 使用友盟统计 SDK
 */
class UmengProvider(
    private val context: Context,
    override val name: String,
    override val supportedPlatforms: Set<Platform>,
    override val supportedRegions: Set<Region>,
) : BaseAnalyticsProvider() {
    private val analyticsScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val logger: Logger = logger("Analytics.UmengProvider")

    override suspend fun initialize(config: ProviderConfig) {

        try {

            val appKey = config.appKey
                ?: config.customParams["appKey"]
                ?: throw IllegalArgumentException("Umeng AppKey is required")
            val channel = config.customParams["channel"] ?: "default"
            val deviceType = config.customParams["deviceType"]?.toIntOrNull()
                ?: UMConfigure.DEVICE_TYPE_PHONE

            // 您务必确保用户同意《隐私政策》之后，
            // 在Applicaiton.onCreate函数中调用预初始化方法UMConfigure.preInit(...)，然后再调用注册用户ID方法。
            UMConfigure.preInit(context, appKey, channel)
            // 初始化友盟统计 SDK
            UMConfigure.init(context, appKey, channel, deviceType, null)

            // 设置自动页面统计模式（自动统计 Activity 页面）
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)

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
                // 转换参数为 Umeng 格式（Map<String, String>）
                val params = mutableMapOf<String, String>()

                // 添加事件参数
                event.parameters.forEach { (key, value) ->
                    params[key] = convertToUmengValue(value)
                }

                // 添加 value 和 currency（如果存在）
                event.value?.let { params["value"] = it.toString() }
                event.currency?.let { params["currency"] = it }

                // 记录事件
                if (params.isEmpty()) {
                    // 无参数事件
                    MobclickAgent.onEvent(context, event.name)
                } else {
                    // 有参数事件
                    MobclickAgent.onEvent(context, event.name, params)
                }
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
                // Umeng 使用 onProfileSignIn 设置用户属性
                // 注意：Umeng 的用户属性设置方式与 Firebase 不同
                // 这里使用自定义事件来记录用户属性
                val propertyValue = value?.let { convertToUmengValue(it) } ?: ""
                if (propertyValue.isNotEmpty()) {
                    MobclickAgent.onEvent(context, "user_property", mapOf(key to propertyValue))
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
                // Umeng 使用 onProfileSignIn 设置用户 ID
                if (userId != null) {
                    MobclickAgent.onProfileSignIn(userId)
                } else {
                    // 清除用户 ID
                    MobclickAgent.onProfileSignOff()
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
                // Umeng 使用 onPageStart 和 onPageEnd 记录页面
                // 注意：由于我们使用 AUTO 模式，通常不需要手动调用
                // 但如果需要手动记录，可以使用以下方式：
                MobclickAgent.onPageStart(screenName)
                screenClass?.let {
                    MobclickAgent.onEvent(context, "screen_view", mapOf("screen_class" to it))
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
                // Umeng 没有直接的 reset 方法
                // 可以清除用户 ID 来重置用户数据
                MobclickAgent.onProfileSignOff()
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
