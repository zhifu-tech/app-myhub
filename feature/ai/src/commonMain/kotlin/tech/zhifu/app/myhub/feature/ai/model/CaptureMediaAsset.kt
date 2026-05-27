package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class CaptureMediaAsset(
    val storageHandle: String,
    val mediaType: String,
    val sizeBytes: Long,
    val sha256: String,
    val isMissing: Boolean = false,
    @Transient
    val accessUrl: String = "",
)
