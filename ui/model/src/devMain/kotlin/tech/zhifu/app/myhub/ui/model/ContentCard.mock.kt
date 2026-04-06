package tech.zhifu.app.myhub.ui.model

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Clock

fun mockContentCards() = listOf<ContentCard>(
    ContentCard(
        id = "1",
        title = "title",
        summary = "summary",
        location = "location",
        updatedAt = Clock.System.now().toEpochMilliseconds(),
        tags = persistentListOf("tag1", "tag2"),
        cover = ContentCardCover(
            iconKey = "iconKey",
            background = Color(0xFF000000),
            tint = Color(0xFFFFFFFF),
            url = "url",
        ),
        action = ContentCardAction(
            label = "label",
            iconKey = "iconKey",
            color = Color(0xFFFFFFFF),
        )
    )
)

