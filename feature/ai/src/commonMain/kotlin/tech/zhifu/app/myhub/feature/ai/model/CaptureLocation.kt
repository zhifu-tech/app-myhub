package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class CaptureLocation(
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
