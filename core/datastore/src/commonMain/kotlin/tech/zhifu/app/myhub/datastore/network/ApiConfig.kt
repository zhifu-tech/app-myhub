package tech.zhifu.app.myhub.datastore.network

import tech.zhifu.app.myhub.SERVER_PORT

/**
 * API配置
 */
object ApiConfig {
    // 基础URL - 可以根据环境配置
    const val BASE_URL = "http://localhost:$SERVER_PORT"
    
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

