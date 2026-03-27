package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CardMedia(
    val type: CardMediaType,
    val url: String,
    val caption: String? = null,
    val meta: JsonObject? = null,
)

@Serializable
data class CardMediaType(
    val type: String,
    val subtype: String,
) {
    val value: String
        get() = "$type/$subtype"

    companion object {
        val IMAGE_JPEG = CardMediaType(type = "image", subtype = "jpeg")
        val IMAGE_PNG = CardMediaType(type = "image", subtype = "png")
        val IMAGE_WEBP = CardMediaType(type = "image", subtype = "webp")
        val AUDIO_MPEG = CardMediaType(type = "audio", subtype = "mpeg")
        val AUDIO_WAV = CardMediaType(type = "audio", subtype = "wav")
        val VIDEO_MP4 = CardMediaType(type = "video", subtype = "mp4")
        val VIDEO_QUICKTIME = CardMediaType(type = "video", subtype = "quicktime")
        val APPLICATION_PDF = CardMediaType(type = "application", subtype = "pdf")
    }
}
