package tech.zhifu.app.myhub.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.toImmutableList
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.location
import tech.zhifu.app.myhub.datastore.model.domain.tags
import tech.zhifu.app.myhub.datastore.model.domain.ui

fun Card.toDashboardContentCard(): ContentCard =
    ContentCard(
        id = id,
        title = title,
        summary = summary,
        location = location?.name.orEmpty(),
        updatedAt = updatedAt.toEpochMilliseconds(),
        status = status,
        tags = tags.toImmutableList(),
        cover = ContentCardCover(
            iconKey = ui?.cover?.iconKey,
            background = ui?.cover?.bgColor?.let(::parseHexColor)
                ?: ContentCardColors.slate100,
            tint = ui?.cover?.tintColor?.let(::parseHexColor),
            url = ui?.cover?.imageUrl,
        ),
        action = actionForStatus(status),
    )

internal fun actionForStatus(
    status: CardStatus
) = when (status) {
    CardStatus.DRAFT -> ContentCardAction(
        label = ContentCardTokens.draftActionLabel,
        iconKey = ContentCardTokens.draftActionIcon.name,
        color = ContentCardTokens.draftActionColor,
    )

    CardStatus.PUBLISHED -> ContentCardAction(
        label = ContentCardTokens.reviewLabel,
        iconKey = ContentCardTokens.reviewIcon.name,
        color = ContentCardTokens.reviewColor,
    )

    CardStatus.ARCHIVED -> ContentCardAction(
        label = ContentCardTokens.archivedLabel,
        iconKey = ContentCardTokens.archivedIcon.name,
        color = ContentCardTokens.archivedColor,
    )
}

fun String.toImageVector(): ImageVector? = ContentCardIcon.fromKey(this)?.icon

internal fun parseHexColor(hex: String): Color {
    val normalized = hex.removePrefix("#")
    val argb = when (normalized.length) {
        6 -> ("FF$normalized").toLongOrNull(16)
        8 -> normalized.toLongOrNull(16)
        else -> null
    } ?: return ContentCardColors.slate100
    return Color(argb.toInt())
}
