package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import tech.zhifu.app.myhub.datastore.model.Card

internal interface CardComponent {
    @Suppress("NotConstructor")
    @Composable
    fun CardComponent(
        card: Card,
        onEdit: (Card) -> Unit = {},
        onFavorite: (Card) -> Unit = {},
        onCardClick: (Card) -> Unit = {},
        modifier: Modifier = Modifier,
    )

    @Composable
    fun getDisplayTitle(card: Card): String

    /**
     * 获取卡片类型的图标颜色
     */
    fun getTypeIconColor(): Color

    /**
     * 获取卡片类型的图标文本
     */
    fun getTypeIconText(): String
}
