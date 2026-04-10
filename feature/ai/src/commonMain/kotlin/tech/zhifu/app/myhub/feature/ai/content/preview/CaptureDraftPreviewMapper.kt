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
    location = "",
    updatedAt = Clock.System.now().toEpochMilliseconds(),
    status = CardStatus.DRAFT,
    tags = tags.toImmutableList(),
    cover = ContentCardCover(
        iconKey = "edit_note",
        background = Color(0xFFEFF6FF),
        tint = Color(0xFF6366F1),
    ),
    action = ContentCardAction(
        label = continueEdit,
        iconKey = "edit",
        color = Color(0xFF6366F1),
    ),
)
