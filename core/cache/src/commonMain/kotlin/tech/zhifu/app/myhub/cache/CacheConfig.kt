package tech.zhifu.app.myhub.cache

import kotlin.time.Duration

/**
 * 缓存配置。
 *
 * @param maximumSize 最大缓存条目数，必须为正数
 * @param expireAfterWrite 写入后过期时间，为 null 表示不启用
 * @param expireAfterAccess 访问后过期时间，为 null 表示不启用
 */
data class CacheConfig(
    val maximumSize: Long = 1_000,
    val expireAfterWrite: Duration? = null,
    val expireAfterAccess: Duration? = null
) {
    init {
        require(maximumSize > 0) {
            "maximumSize must be positive"
        }
    }
}

