package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.card.resources.Res
import tech.zhifu.app.myhub.component.card.resources.component_card_type_dictionary
import tech.zhifu.app.myhub.datastore.model.domain.Card

internal class WordCardComponent : CardComponent {

    @Composable
    override fun CardComponent(
        card: Card,
        onEdit: (Card) -> Unit,
        onFavorite: (Card) -> Unit,
        onCardClick: (Card) -> Unit,
        modifier: Modifier,
        suppressDefaultBorder: Boolean
    ) = WordCard(
        card = card,
        onEdit = onEdit,
        onFavorite = onFavorite,
        onCardClick = onCardClick,
        modifier = modifier,
        suppressDefaultBorder = suppressDefaultBorder
    )

    @Composable
    override fun getDisplayTitle(card: Card): String {
        return card.title
            ?: card.content.split(" ").firstOrNull()
            ?: stringResource(Res.string.component_card_type_dictionary)
    }

    override fun getTypeIconColor(): Color {
        return Color(0xFF06B6D4) // cyan
    }

    override fun getTypeIconText(): String {
        return "D"
    }
}

