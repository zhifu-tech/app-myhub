package tech.zhifu.app.myhub.feature.capture

import androidx.compose.ui.graphics.Color
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.repository.capture.AnalysisStatus


sealed class CaptureUiState(val state: State) {
    data class Input(
        val intent: CardType = CardType.Review,
        val input: InputCtx = InputCtx(),
        val media: MediaCtx = MediaCtx(),
        val error: CaptureError? = null
    ) : CaptureUiState(state = State.INPUT)

    data class Processing(
        val intent: CardType,
        val input: InputCtx,
        val media: MediaCtx,
        val analysis: AnalysisCtx = AnalysisCtx(status = AnalysisStatus.Queued, progress = 0)
    ) : CaptureUiState(state = State.PROCESSING)

    data class Review(
        val intent: CardType,
        val review: ReviewCtx,
        val error: CaptureError? = null
    ) : CaptureUiState(state = State.REVIEW)

    data class Publishing(
        val intent: CardType,
        val review: ReviewCtx
    ) : CaptureUiState(state = State.PUBLISHING)

    enum class State {
        INPUT, PROCESSING, REVIEW, PUBLISHING
    }
}

sealed interface CaptureError {
    val message: String

    data class AiFailed(override val message: String) : CaptureError
    data class PostFailed(override val message: String) : CaptureError
    data class UploadFailed(override val message: String) : CaptureError
}

data class InputCtx(
    val text: String = "",
    val focused: Boolean = false
)

data class MediaCtx(
    val items: List<MediaItem> = emptyList(),
    val uploadStates: Map<String, CaptureMediaUploadState> = emptyMap()
)

data class AnalysisCtx(
    val jobId: String? = null,
    val status: AnalysisStatus? = null,
    val progress: Int? = null
)

data class ReviewCtx(
    val text: String = "",
    val title: String = "",
    val source: CardSource = CardSource.Extract,
    val styleOptions: List<StyleOption> = emptyList(),
    val selectedStyleIndex: Int = 0,
    val tags: List<String> = emptyList(),
    val tagQuery: String = "",
    val code: String? = null,
    val codeLanguage: String? = null,
    val imageItem: MediaItem? = null,
    val imageOcrSummary: String? = null,
    val imageOcrInfo: String? = null,
    val videoItem: MediaItem? = null,
    val videoMetadataSummary: String? = null,
    val videoMetadataInfo: String? = null,
    val primaryContentType: ReviewContentType = ReviewContentType.Text
)

val CaptureUiState.intent: CardType
    get() = when (this) {
        is CaptureUiState.Input -> intent
        is CaptureUiState.Processing -> intent
        is CaptureUiState.Review -> intent
        is CaptureUiState.Publishing -> intent
    }

val CaptureUiState.input: InputCtx?
    get() = when (this) {
        is CaptureUiState.Input -> input
        is CaptureUiState.Processing -> input
        else -> null
    }

val CaptureUiState.media: MediaCtx?
    get() = when (this) {
        is CaptureUiState.Input -> media
        is CaptureUiState.Processing -> media
        else -> null
    }

val CaptureUiState.review: ReviewCtx?
    get() = when (this) {
        is CaptureUiState.Review -> review
        is CaptureUiState.Publishing -> review
        else -> null
    }

val CaptureUiState.error: CaptureError?
    get() = when (this) {
        is CaptureUiState.Input -> error
        is CaptureUiState.Review -> error
        else -> null
    }

val CaptureUiState.isTopBarWaiting: Boolean
    get() = this is CaptureUiState.Processing || this is CaptureUiState.Publishing

val CaptureUiState.showIntentSelector: Boolean
    get() = isReviewPhase

val CaptureUiState.isInputPhase: Boolean
    get() = this is CaptureUiState.Input || this is CaptureUiState.Processing

val CaptureUiState.isReviewPhase: Boolean
    get() = this is CaptureUiState.Review || this is CaptureUiState.Publishing

val CaptureUiState.canCapture: Boolean
    get() = when (this) {
        is CaptureUiState.Input -> input.text.isNotBlank() || media.items.isNotEmpty()
        is CaptureUiState.Review -> true
        is CaptureUiState.Processing,
        is CaptureUiState.Publishing -> false
    }

val CaptureUiState.hasUploadingMedia: Boolean
    get() = media?.uploadStates?.values?.any { it.state == MediaUploadLifecycle.Uploading } == true

val CaptureUiState.canEditInput: Boolean
    get() = this is CaptureUiState.Input

val CaptureUiState.canEditReview: Boolean
    get() = this is CaptureUiState.Review

data class CaptureMediaUploadState(
    val mediaItemId: String,
    val sessionId: String? = null,
    val mediaId: String? = null,
    val remoteUri: String? = null,
    val progress: Int = 0,
    val state: MediaUploadLifecycle = MediaUploadLifecycle.Pending,
    val errorMessage: String? = null
)

enum class MediaUploadLifecycle {
    Pending,
    Uploading,
    Uploaded,
    Failed
}

enum class ReviewContentType {
    Text,
    Code,
    Image,
    Video
}

data class StyleOption(
    val label: String,
    val color: Color
)
