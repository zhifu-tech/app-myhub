package tech.zhifu.app.myhub.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
        updatedTimeMs = updatedAt.toEpochMilliseconds(),
        tags = tags,
        cover = ContentCardCover(
            icon = ui?.cover?.iconKey?.let(::iconFromKey),
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
        icon = ContentCardTokens.draftActionIcon,
        color = ContentCardTokens.draftActionColor,
    )

    CardStatus.PUBLISHED -> ContentCardAction(
        label = ContentCardTokens.reviewLabel,
        icon = ContentCardTokens.reviewIcon,
        color = ContentCardTokens.reviewColor,
    )

    CardStatus.ARCHIVED -> ContentCardAction(
        label = ContentCardTokens.archivedLabel,
        icon = ContentCardTokens.archivedIcon,
        color = ContentCardTokens.archivedColor,
    )
}

internal fun iconFromKey(
    key: String
): ImageVector? =
    when (key.lowercase()) {
        "psychology" -> Icons.Outlined.Psychology
        "visibility" -> Icons.Outlined.Visibility
        "edit_note" -> Icons.Outlined.EditNote
        "edit" -> Icons.Outlined.Edit
        "rocket_launch" -> Icons.Outlined.RocketLaunch
        "play_circle" -> Icons.Outlined.PlayCircle
        else -> null
    }

internal fun parseHexColor(hex: String): Color {
    val normalized = hex.removePrefix("#")
    val argb = when (normalized.length) {
        6 -> ("FF$normalized").toLongOrNull(16)
        8 -> normalized.toLongOrNull(16)
        else -> null
    } ?: return ContentCardColors.slate100
    return Color(argb.toInt())
}
