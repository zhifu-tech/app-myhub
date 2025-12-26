package tech.zhifu.app.myhub.datastore.network

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
    private const val DEFAULT_BASE_URL = "http://localhost:$DEFAULT_PORT"

    @Volatile
    private var _baseUrl: String? = null

    /**
     * API 基础 URL
     * 优先级：
     * 1. 通过 setBaseUrl 设置的 URL
     * 2. 系统属性 myhub.api.base.url
     * 3. 默认值
     */
    val BASE_URL: String
        get() {
            if (_baseUrl != null) {
                logger.info { "Using configured API base URL: $_baseUrl" }
                return _baseUrl!!
            }
            // 尝试从系统属性获取（Android 平台）
            val systemPropertyUrl = getSystemProperty("myhub.api.base.url")
            if (systemPropertyUrl != null) {
                logger.info { "Using system property API base URL: $systemPropertyUrl" }
                return systemPropertyUrl
            }
            logger.warn { "Using default API base URL: $DEFAULT_BASE_URL" }
            return DEFAULT_BASE_URL
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

