package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.datastore.model.domain.Card

internal class VideoCardComponent : CardComponent {
    @Composable
    override fun CardComponent(
        card: Card,
        onEdit: (Card) -> Unit,
        onFavorite: (Card) -> Unit,
        onCardClick: (Card) -> Unit,
        modifier: Modifier
    ) = VideoCard(
        card = card,
        onEdit = onEdit,
        onFavorite = onFavorite,
        onCardClick = onCardClick,
        modifier = modifier
    )

    @Composable
    override fun getDisplayTitle(card: Card): String {
        return card.title ?: "Video"
    }

    override fun getTypeIconColor(): Color {
        return Color(0xFFEF4444) // red (YouTube-like)
    }

    override fun getTypeIconText(): String {
        return "V"
    }
}
