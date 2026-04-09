package tech.zhifu.app.myhub.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus

@Immutable
data class ContentCard(
    val id: String,
    val title: String,
    val summary: String,
    val location: String,
    val updatedAt: Long,
    val status: CardStatus,
    val tags: ImmutableList<String>,
    val cover: ContentCardCover,
    val action: ContentCardAction,
)

@Immutable
data class ContentCardCover(
    val iconKey: String? = null,
    val background: Color,
    val tint: Color? = null,
    val url: String? = null,
)

@Immutable
data class ContentCardAction(
    val label: String,
    val iconKey: String,
    val color: Color,
)
