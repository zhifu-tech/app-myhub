package tech.zhifu.app.myhub.feature.ai.content.preview

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.toImmutableList
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.model.ContentCardAction
import tech.zhifu.app.myhub.ui.model.ContentCardCover
import kotlin.time.Clock

fun CaptureDraft.toPreviewCard(
    untitledDraft: String,
    continueHint: String,
    continueEdit: String,
): ContentCard = ContentCard(
    id = id,
    title = title.ifBlank { untitledDraft },
    summary = summary.ifBlank { sourceText.ifBlank { continueHint } },
    location = location?.name.orEmpty(),
    updatedAt = Clock.System.now().toEpochMilliseconds(),
    status = CardStatus.DRAFT,
    tags = tags.toImmutableList(),
    cover = run {
        val coverUrl = previewCoverUrl()
        ContentCardCover(
            iconKey = if (coverUrl == null) "edit_note" else null,
            background = Color(0xFFEFF6FF),
            tint = if (coverUrl == null) Color(0xFF6366F1) else null,
            url = coverUrl,
        )
    },
    action = ContentCardAction(
        label = continueEdit,
        iconKey = "edit",
        color = Color(0xFF6366F1),
    ),
)

fun CaptureDraft.previewCoverUrl(): String? {
    return mediaAssets
        .firstOrNull { asset ->
            val mime = asset.mediaType.lowercase()
            mime.startsWith(prefix = "image/") || asset.localUri.lowercase().let { uri ->
                uri.endsWith(".jpg") ||
                    uri.endsWith(".jpeg") ||
                    uri.endsWith(".png") ||
                    uri.endsWith(".webp") ||
                    uri.endsWith(".heic") ||
                    uri.endsWith(".gif")
            }
        }
        ?.localUri
}
