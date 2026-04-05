package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaPostProcessRequest(
    @SerialName("cardId")
    val cardId: String,
    @SerialName("mediaId")
    val mediaId: String,
    @SerialName("mediaUri")
    val mediaUri: String,
)

@Serializable
data class MediaPostProcessResponse(
    @SerialName("thumbUri")
    val thumbUri: String,
    @SerialName("durationMs")
    val durationMs: Long?,
)

