package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.card.resources.Res
import tech.zhifu.app.myhub.component.card.resources.component_card_type_code
import tech.zhifu.app.myhub.datastore.model.domain.Card

internal class CodeCardComponent : CardComponent {

    @Composable
    override fun CardComponent(
        card: Card,
        onEdit: (Card) -> Unit,
        onFavorite: (Card) -> Unit,
        onCardClick: (Card) -> Unit,
        modifier: Modifier,
        suppressDefaultBorder: Boolean
    ) = CodeCard(
        card = card,
        onEdit = onEdit,
        onFavorite = onFavorite,
        onCardClick = onCardClick,
        modifier = modifier,
        suppressDefaultBorder = suppressDefaultBorder
    )

    @Composable
    override fun getDisplayTitle(card: Card): String {
        return card.title ?: stringResource(Res.string.component_card_type_code)
    }

    override fun getTypeIconColor(): Color {
        return Color(0xFF10B981) // green
    }

    override fun getTypeIconText(): String {
        return "C"
    }
}

