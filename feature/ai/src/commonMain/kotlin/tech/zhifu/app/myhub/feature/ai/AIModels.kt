package tech.zhifu.app.myhub.feature.ai

import kotlinx.serialization.Serializable

data class AIMsg(
    val id: String,
    val role: Role,
    val text: String,
) {
    enum class Role {
        AI,
        USER,
        SYSTEM
    }
}

enum class CaptureState {
    IDLE,
    INTENT_DETECT,
    DRAFT_CREATE,
    INFO_COLLECT,
    CARD_REVIEW,
    PUBLISH_CONFIRM,
    COMPLETE,
    MANUAL_EDIT
}

@Serializable
data class ActionComponentSchema(
    val type: String,
    val field: String? = null,
    val options: List<ActionOptionSchema> = emptyList(),
)

@Serializable
data class ActionOptionSchema(
    val label: String,
    val value: String,
)

@Serializable
data class CaptureDraft(
    val id: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
    val sourceText: String,
    val mediaAssets: List<CaptureMediaAsset> = emptyList(),
)

@Serializable
data class CaptureMediaAsset(
    val localUri: String,
    val mediaType: String,
    val sizeBytes: Long,
    val sha256: String,
)
