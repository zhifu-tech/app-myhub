package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * 标签实体
 */
@Serializable
data class Tag(
    val id: String,
    val name: String,
    val color: String? = null, // 十六进制颜色值，如 "#FF5733"
    val description: String? = null,
    val cardCount: Int = 0, // 使用该标签的卡片数量
    val createdAt: Instant
)

/**
 * 标签统计信息
 */
@Serializable
data class TagStats(
    val tagId: String,
    val tagName: String,
    val totalCards: Int,
    val favoriteCards: Int,
    val recentCards: Int // 最近7天的卡片数
)

