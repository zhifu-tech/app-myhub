package tech.zhifu.app.myhub.feature.capture.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.richeditor.model.RichTextState
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.feature.capture.CaptureError
import tech.zhifu.app.myhub.feature.capture.CaptureUiState
import tech.zhifu.app.myhub.feature.capture.ReviewContentType
import tech.zhifu.app.myhub.feature.capture.ReviewCtx

@Composable
internal fun ReviewSection(
    state: CaptureUiState,
    error: CaptureError?,
    review: ReviewCtx,
    reviewRichTextState: RichTextState,
    horizontalPadding: Dp,
    isProcessing: Boolean,
    onReviewTextChange: (String) -> Unit,
    onReviewTitleChange: (String) -> Unit,
    onReviewSourceFormChange: (CardSource) -> Unit,
    onReviewStyleChange: (Int) -> Unit,
    onReviewCodeLanguageChange: (String) -> Unit,
    onReviewTagQueryChange: (String) -> Unit,
    onAddReviewTag: (String) -> Unit,
    onRemoveReviewTag: (String) -> Unit,
    onReviewPrimaryChange: (ReviewContentType) -> Unit,
    onPreviewMedia: (MediaItem) -> Unit,
    onRetry: () -> Unit,
    onBackToReview: () -> Unit
) {
    LaunchedEffect(review.text) {
        val content = review.text
        when {
            isLikelyHtml(content) -> {
                if (reviewRichTextState.toHtml() != content) {
                    reviewRichTextState.setHtml(content)
                }
            }

            reviewRichTextState.toMarkdown() != content -> {
                reviewRichTextState.setMarkdown(content)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ReviewSectionContent(
            review = review,
            richTextState = reviewRichTextState,
            onReviewTextChange = onReviewTextChange,
            onReviewTitleChange = onReviewTitleChange,
            onReviewSourceFormChange = onReviewSourceFormChange,
            onReviewStyleChange = onReviewStyleChange,
            onReviewCodeLanguageChange = onReviewCodeLanguageChange,
            onReviewTagQueryChange = onReviewTagQueryChange,
            onAddReviewTag = onAddReviewTag,
            onRemoveReviewTag = onRemoveReviewTag,
            onReviewPrimaryChange = onReviewPrimaryChange,
            onPreviewMedia = onPreviewMedia,
            horizontalPadding = horizontalPadding,
            isProcessing = isProcessing
        )
    }

    when (state) {
        is CaptureUiState.Publishing -> CapturePostWaitingOverlay()
        is CaptureUiState.Review -> {
            if (error is CaptureError.PostFailed) {
                CapturePostFailedOverlay(
                    message = error.message,
                    onRetry = onRetry,
                    onBackToReview = onBackToReview
                )
            }
        }
        else -> Unit
    }
}
