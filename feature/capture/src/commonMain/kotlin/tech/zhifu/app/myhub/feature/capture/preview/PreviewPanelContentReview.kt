package tech.zhifu.app.myhub.feature.capture.preview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mohamedrejeb.richeditor.model.RichTextState
import tech.zhifu.app.myhub.component.card.CardPreview
import tech.zhifu.app.myhub.component.media.util.toPlayableUrl
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadata
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.feature.capture.ReviewCtx
import kotlin.time.Clock

@Composable
internal fun PreviewPanelContentReview(
    review: ReviewCtx,
    reviewRichTextState: RichTextState
) {
    val previewCard = review.toPreviewCard(
        summaryText = reviewRichTextState.annotatedString.text.ifBlank {
            review.text.toPreviewPlainText()
        }
    )

    CardPreview(
        card = previewCard,
        modifier = Modifier.fillMaxWidth(),
        editable = false
    )
}

private fun String.toPreviewPlainText(): String =
    replace(Regex("<[^>]*>"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun ReviewCtx.toPreviewCard(summaryText: String): Card {
    val now = Clock.System.now()

    return Card(
        id = "preview-card",
        type = CardType.Review,
        source = source,
        carriers = previewCarriersJson(),
        userId = "preview",
        createdAt = now,
        updatedAt = now,
        metadata = buildList {
            val selectedStyle = styleOptions.getOrNull(selectedStyleIndex)
            add(
                CardMetadata.Attribution(
                    styleKey = selectedStyle?.label,
                    styleColor = selectedStyle?.color?.toHexString()
                )
            )
            title.takeIf { it.isNotBlank() }?.let {
                add(
                    CardMetadata.Content(
                        title = title,
                        summary = summaryText,
                        content = text
                    )
                )
            }
            code?.takeIf { it.isNotBlank() }?.let {
                add(
                    CardMetadata.Code(
                        language = codeLanguage,
                        snippet = code
                    )
                )
            }
            imageItem?.also { item ->
                add(
                    CardMetadata.CarrierImage(
                        url = item.file.toPlayableUrl(),
                        thumbnailUrl = item.file.toPlayableUrl()
                    )
                )
            }
            videoItem?.let { item ->
                add(
                    CardMetadata.CarrierVideo(
                        videoUrl = item.file.toPlayableUrl(),
                        coverImageUrl = item.file.toPlayableUrl(),
                        platform = videoMetadataSummary?.takeIf { it.isNotBlank() } ?: "Video Metadata"
                    )
                )
            }
        },
        tags = tags.mapIndexed { index, tag ->
            Tag(
                id = "preview-tag-$index",
                name = tag,
                userId = "preview",
                createdAt = now,
                updatedAt = now
            )
        }
    )
}

private fun Color.toHexString(): String {
    val argb = toArgb()
    val rgb = argb and 0x00FFFFFF
    return "#${rgb.toString(16).padStart(6, '0').uppercase()}"
}

private fun ReviewCtx.previewCarriersJson(): String = buildList {
    if (text.isNotBlank()) add("text")
    if (!code.isNullOrBlank()) add("code")
    if (imageItem != null) add("image")
    if (videoItem != null) add("video")
}.let { carriers ->
    if (carriers.isEmpty()) "[]" else carriers.joinToString(
        prefix = "[\"",
        separator = "\",\"",
        postfix = "\"]"
    )
}
