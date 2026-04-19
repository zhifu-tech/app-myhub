package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class CaptureDraft(
    val id: String,
    val title: String = "",
    val summary: String = "",
    val tags: List<String> = emptyList(),
    val sourceText: String = "",
    val mediaAssets: List<CaptureMediaAsset> = emptyList(),
    val captureType: CaptureType? = null,
    val location: CaptureLocation? = null,
)
