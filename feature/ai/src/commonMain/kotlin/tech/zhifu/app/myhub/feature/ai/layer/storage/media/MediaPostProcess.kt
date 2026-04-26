package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaPostProcessRequest(
    @SerialName("cardId")
    val cardId: String,
    @SerialName("mediaId")
    val mediaId: String,
    @SerialName("mediaStorageHandle")
    val mediaStorageHandle: String,
)

@Serializable
data class MediaPostProcessResponse(
    @SerialName("thumbAccessUrl")
    val thumbAccessUrl: String,
    @SerialName("durationMs")
    val durationMs: Long?,
)
