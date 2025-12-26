package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * 用户实体
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Instant,
    val preferences: UserPreferences? = null
)

/**
 * 用户偏好设置
 */
@Serializable
data class UserPreferences(
    val theme: String = "dark", // "light", "dark", "auto"
    val language: String = "en",
    val defaultCardType: CardType? = null,
    val autoSync: Boolean = true,
    val syncInterval: Long = 3600000L // 毫秒，默认1小时
)

