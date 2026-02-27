package tech.zhifu.app.myhub.feature.capture

import androidx.compose.ui.graphics.Color
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.repository.capture.AnalysisStatus

data class CaptureUiState(
    val state: CaptureState = CaptureState.ReadyIdle,
    val intent: CardType = CardType.Review,
    val input: InputCtx = InputCtx(),
    val media: MediaCtx = MediaCtx(),
    val analysis: AnalysisCtx? = null,
    val review: ReviewCtx? = null,
    val error: CaptureError? = null
) {
    companion object {
        fun readyIdle(
            intent: CardType = CardType.Review,
            input: InputCtx = InputCtx(focused = false),
            media: MediaCtx = MediaCtx(),
            error: CaptureError? = null
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.ReadyIdle,
            intent = intent,
            input = input.copy(focused = false),
            media = media,
            analysis = null,
            review = null,
            error = error
        )

        fun readyFocused(
            intent: CardType = CardType.Review,
            input: InputCtx = InputCtx(focused = true),
            media: MediaCtx = MediaCtx(),
            error: CaptureError? = null
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.ReadyFocused,
            intent = intent,
            input = input.copy(focused = true),
            media = media,
            analysis = null,
            review = null,
            error = error
        )

        fun aiProcessing(
            intent: CardType,
            input: InputCtx,
            media: MediaCtx,
            analysis: AnalysisCtx = AnalysisCtx(status = AnalysisStatus.Queued, progress = 0),
            error: CaptureError? = null
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.AiProcessing,
            intent = intent,
            input = input.copy(focused = false),
            media = media,
            analysis = analysis,
            review = null,
            error = error
        )

        fun aiFailed(
            intent: CardType = CardType.Review,
            input: InputCtx = InputCtx(),
            media: MediaCtx = MediaCtx(),
            message: String
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.AiFailed,
            intent = intent,
            input = input,
            media = media,
            analysis = null,
            review = null,
            error = CaptureError.AiFailed(message)
        )

        fun reviewEditing(
            intent: CardType = CardType.Review,
            review: ReviewCtx,
            error: CaptureError? = null
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.ReviewEditing,
            intent = intent,
            input = InputCtx(),
            media = MediaCtx(),
            analysis = null,
            review = review,
            error = error
        )

        fun postProcessing(
            intent: CardType = CardType.Review,
            review: ReviewCtx,
            error: CaptureError? = null
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.PostProcessing,
            intent = intent,
            input = InputCtx(),
            media = MediaCtx(),
            analysis = null,
            review = review,
            error = error
        )

        fun postSuccess(
            intent: CardType = CardType.Review,
            review: ReviewCtx,
            error: CaptureError? = null
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.PostSuccess,
            intent = intent,
            input = InputCtx(),
            media = MediaCtx(),
            analysis = null,
            review = review,
            error = error
        )

        fun postFailed(
            intent: CardType = CardType.Review,
            review: ReviewCtx,
            message: String
        ): CaptureUiState = CaptureUiState(
            state = CaptureState.PostFailed,
            intent = intent,
            input = InputCtx(),
            media = MediaCtx(),
            analysis = null,
            review = review,
            error = CaptureError.PostFailed(message)
        )
    }
}

enum class CaptureState {
    ReadyIdle,
    ReadyFocused,
    AiProcessing,
    AiFailed,
    ReviewEditing,
    PostProcessing,
    PostSuccess,
    PostFailed
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

val CaptureUiState.isTopBarWaiting: Boolean
    get() = state == CaptureState.AiProcessing || state == CaptureState.PostProcessing

val CaptureUiState.showIntentSelector: Boolean
    get() = state == CaptureState.ReviewEditing ||
        state == CaptureState.PostProcessing ||
        state == CaptureState.PostFailed ||
        state == CaptureState.PostSuccess

val CaptureUiState.isInputPhase: Boolean
    get() = state == CaptureState.ReadyIdle ||
        state == CaptureState.ReadyFocused ||
        state == CaptureState.AiProcessing ||
        state == CaptureState.AiFailed

val CaptureUiState.isReviewPhase: Boolean
    get() = state == CaptureState.ReviewEditing ||
        state == CaptureState.PostProcessing ||
        state == CaptureState.PostSuccess ||
        state == CaptureState.PostFailed

val CaptureUiState.canCapture: Boolean
    get() {
        return when (state) {
            CaptureState.ReadyIdle,
            CaptureState.ReadyFocused,
            CaptureState.AiFailed -> input.text.isNotBlank() || media.items.isNotEmpty()

            CaptureState.ReviewEditing,
            CaptureState.PostFailed -> true

            CaptureState.AiProcessing,
            CaptureState.PostProcessing,
            CaptureState.PostSuccess -> false
        }
    }

val CaptureUiState.hasUploadingMedia: Boolean
    get() = media.uploadStates.values.any { it.state == MediaUploadLifecycle.Uploading }

val CaptureUiState.canEditInput: Boolean
    get() = state == CaptureState.ReadyIdle ||
        state == CaptureState.ReadyFocused ||
        state == CaptureState.AiFailed

val CaptureUiState.canEditReview: Boolean
    get() = state == CaptureState.ReviewEditing

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
