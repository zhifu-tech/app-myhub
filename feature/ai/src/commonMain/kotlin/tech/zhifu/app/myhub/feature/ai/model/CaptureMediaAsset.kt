package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class CaptureMediaAsset(
    val localUri: String,
    val mediaType: String,
    val sizeBytes: Long,
    val sha256: String,
)
