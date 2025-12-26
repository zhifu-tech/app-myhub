package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * 卡片模板实体
 */
@Serializable
data class Template(
    val id: String,
    val name: String,
    val description: String? = null,
    val cardType: CardType,
    val previewImageUrl: String? = null,
    val defaultContent: String? = null,
    val defaultMetadata: CardMetadata? = null,
    val defaultTags: List<String> = emptyList(),
    val usageCount: Int = 0,
    val isSystemTemplate: Boolean = false, // 系统模板 vs 用户自定义模板
    val createdAt: Instant,
    val updatedAt: Instant
)
