package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.card.resources.Res
import tech.zhifu.app.myhub.component.card.resources.component_card_type_quote
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.quoteMetadata

internal class QuoteCardComponent : CardComponent {

    @Composable
    override fun CardComponent(
        card: Card,
        onEdit: (Card) -> Unit,
        onFavorite: (Card) -> Unit,
        onCardClick: (Card) -> Unit,
        modifier: Modifier
    ) = QuoteCard(
        card = card,
        onEdit = onEdit,
        onFavorite = onFavorite,
        onCardClick = onCardClick,
        modifier = modifier,
    )

    @Composable
    override fun getDisplayTitle(card: Card): String {
        return card.quoteMetadata?.author
            ?: stringResource(Res.string.component_card_type_quote)
    }

    override fun getTypeIconColor(): Color {
        return Color(0xFF8B5CF6)
    }

    override fun getTypeIconText(): String {
        return "Q"
    }
}


