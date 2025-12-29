package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.datastore.model.Card

/**
 * 卡片组件工厂函数类型
 * 用于创建和渲染特定类型的卡片组件
 */
typealias CardComponentFactory = @Composable (
    card: Card,
    onEdit: (Card) -> Unit,
    onFavorite: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier
) -> Unit
