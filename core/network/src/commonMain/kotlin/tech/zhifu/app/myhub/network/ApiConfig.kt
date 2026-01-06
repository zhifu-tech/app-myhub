package tech.zhifu.app.myhub.network

import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.system.getSystemProperty
import kotlin.concurrent.Volatile

/**
 * API配置
 */
object ApiConfig {
    // 基础URL - 可以通过 setBaseUrl 动态设置
    private const val DEFAULT_PORT = 8083

    private const val DEFAULT_REMOTE_IP = "192.168.0.123"

    @Volatile
    private var _baseUrl: String? = null

    /**
     * API 基础 URL
     * 优先级：
     * 1. 通过 setBaseUrl 设置的 URL
     * 2. 系统属性 myhub.api.base.url
     * 3. 环境变量 MYHUB_API_BASE_URL
     * 4. 默认值 (10.0.2.2 for Android emulator or 192.168.0.123)
     */
    val BASE_URL: String
        get() {
            if (_baseUrl != null) {
                logger.info { "Using configured API base URL: $_baseUrl" }
                return _baseUrl!!
            }

            // 尝试从系统属性获取（Android 平台或 JVM）
            val systemPropertyUrl = getSystemProperty("myhub.api.base.url")
            if (!systemPropertyUrl.isNullOrBlank()) {
                logger.info { "Using system property API base URL: $systemPropertyUrl" }
                return systemPropertyUrl
            }

            val defaultUrl = "http://$DEFAULT_REMOTE_IP:$DEFAULT_PORT"
            logger.warn { "Using default API base URL: $defaultUrl" }
            return defaultUrl
        }

    /**
     * 设置 API 基础 URL
     * 应该在应用启动时调用，传入 AppBuildConfig.apiBaseUrl
     */
    fun setBaseUrl(url: String) {
        _baseUrl = url
    }

    // API路径
    const val CARDS_PATH = "/api/cards"
    const val TAGS_PATH = "/api/tags"
    const val TEMPLATES_PATH = "/api/templates"
    const val USERS_PATH = "/api/users"
    const val STATISTICS_PATH = "/api/statistics"

    // 超时配置（毫秒）
    const val CONNECT_TIMEOUT = 30_000L
    const val SOCKET_TIMEOUT = 30_000L
}
