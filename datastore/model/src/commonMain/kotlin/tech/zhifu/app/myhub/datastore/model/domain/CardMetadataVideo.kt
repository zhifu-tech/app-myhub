package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

const val CARD_TYPE_VIDEO = "video"

val Card.isVideo: Boolean
    get() = type == CARD_TYPE_VIDEO

val String.isVideo: Boolean
    get() = this == CARD_TYPE_VIDEO

val Card.videoMetadata: CardMetadataVideo?
    get() = metadata as? CardMetadataVideo

/**
 * Video 卡片元数据
 */
@Serializable
data class CardMetadataVideo(
    val cardId: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int? = null,
    val platform: String? = null  // youtube, xiaohongshu, etc.
) : CardMetadata
