package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset

@Serializable
data class CaptureDraft(
    val id: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
    val sourceText: String,
    val mediaAssets: List<CaptureMediaAsset> = emptyList(),
)
