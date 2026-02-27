package tech.zhifu.app.myhub.feature.capture.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.feature.capture.ReviewContentType
import tech.zhifu.app.myhub.feature.capture.ReviewCtx

@Composable
internal fun ReviewSectionContent(
    review: ReviewCtx,
    richTextState: RichTextState,
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
    horizontalPadding: Dp,
    isProcessing: Boolean
) {
    var selectedContentType by remember(review.primaryContentType) {
        mutableStateOf(review.primaryContentType)
    }
    val selectContent: (ReviewContentType) -> Unit = { type ->
        selectedContentType = type
        onReviewPrimaryChange(type)
    }
    Column(
        modifier = Modifier.widthIn(max = 860.dp)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(top = 32.dp, bottom = 80.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ReviewSource(
            sourceForm = review.source,
            onSourceFormChange = onReviewSourceFormChange
        )
        ReviewText(
            textState = richTextState,
            onTextChange = onReviewTextChange,
            enabled = !isProcessing,
            selectedContentType = selectedContentType,
            onSelectContent = selectContent
        )
        ReviewCode(
            code = review.code,
            language = review.codeLanguage,
            onLanguageChange = onReviewCodeLanguageChange,
            selectedContentType = selectedContentType,
            onSelectContent = selectContent
        )
        ReviewMediaImage(
            imageItem = review.imageItem,
            summary = review.imageOcrSummary.orEmpty(),
            info = review.imageOcrInfo.orEmpty(),
            onPreview = onPreviewMedia,
            selectedContentType = selectedContentType,
            onSelectContent = selectContent
        )
        ReviewMediaVideo(
            videoItem = review.videoItem,
            summary = review.videoMetadataSummary.orEmpty(),
            info = review.videoMetadataInfo.orEmpty(),
            onPreview = onPreviewMedia,
            selectedContentType = selectedContentType,
            onSelectContent = selectContent
        )
        ReviewTitle(
            title = review.title,
            onTitleChange = onReviewTitleChange,
            enabled = !isProcessing
        )
        ReviewStyle(
            options = review.styleOptions,
            selectedIndex = review.selectedStyleIndex,
            onSelect = onReviewStyleChange
        )
        ReviewTagsSection(
            tags = review.tags,
            tagQuery = review.tagQuery,
            onTagQueryChange = onReviewTagQueryChange,
            onAddTag = onAddReviewTag,
            onRemoveTag = onRemoveReviewTag
        )
    }
}

