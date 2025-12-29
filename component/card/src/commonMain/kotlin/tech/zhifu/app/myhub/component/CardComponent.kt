package tech.zhifu.app.myhub.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.card.CardComponentFactory
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType

/**
 * 统一的卡片组件
 * 根据 Card.type 自动选择对应的组件进行渲染
 *
 * 通过 Koin 注入卡片组件工厂映射表，实现组件类型的自动路由
 */
@Composable
fun CardComponent(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val cardComponentMap: Map<CardType, CardComponentFactory> = koinInject()
    val factory = cardComponentMap[card.type]
        ?: error("No component factory registered for card type: ${card.type}")

    factory(card, onEdit, onFavorite, onCardClick, modifier)
}
