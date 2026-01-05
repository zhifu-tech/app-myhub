package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.card.resources.Res
import tech.zhifu.app.myhub.component.card.resources.component_card_month_apr
import tech.zhifu.app.myhub.component.card.resources.component_card_month_aug
import tech.zhifu.app.myhub.component.card.resources.component_card_month_dec
import tech.zhifu.app.myhub.component.card.resources.component_card_month_feb
import tech.zhifu.app.myhub.component.card.resources.component_card_month_jan
import tech.zhifu.app.myhub.component.card.resources.component_card_month_jul
import tech.zhifu.app.myhub.component.card.resources.component_card_month_jun
import tech.zhifu.app.myhub.component.card.resources.component_card_month_mar
import tech.zhifu.app.myhub.component.card.resources.component_card_month_may
import tech.zhifu.app.myhub.component.card.resources.component_card_month_nov
import tech.zhifu.app.myhub.component.card.resources.component_card_month_oct
import tech.zhifu.app.myhub.component.card.resources.component_card_month_sep
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType

private lateinit var factories: Map<CardType, CardComponent>

@Composable
private fun Card.toComponent(): CardComponent {
    if (!::factories.isInitialized) {
        factories = koinInject()
    }
    return factories[type]
        ?: error("No component factory registered for card type: $type")
}

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
) = card.toComponent()
    .CardComponent(card, onEdit, onFavorite, onCardClick, modifier)

/**
 * 获取卡片的显示标题
 * 根据卡片类型返回合适的标题，支持多语言
 */
val Card.displayTitle: String
    @Composable
    get() = toComponent().getDisplayTitle(this)

/**
 * 获取卡片类型的图标颜色
 * 通过 CardComponent 接口获取，实现解耦
 */
val Card.typeIconColor: Color
    @Composable
    get() = toComponent().getTypeIconColor()

/**
 * 获取卡片类型的图标文本
 * 通过 CardComponent 接口获取，实现解耦
 */
val Card.typeIconText: String
    @Composable
    get() = toComponent().getTypeIconText()

/**
 * 获取卡片内容的预览（截取前N个字符）
 */
fun Card.getContentPreview(maxLength: Int = 80): String {
    val preview = content.take(maxLength)
    return if (content.length > maxLength) "$preview..." else preview
}

/**
 * 格式化卡片的更新时间
 * 格式：Oct 24, 2023（支持多语言）
 */
@Composable
fun Card.formatUpdatedTime(): String {
    val localDateTime = updatedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthName = getMonthName(localDateTime.month.number)
    val day = localDateTime.day
    val year = localDateTime.year
    return "$monthName $day, $year"
}

/**
 * 格式化卡片的创建时间
 * 格式：Oct 24, 2023（支持多语言）
 */
@Composable
fun Card.formatCreatedTime(): String {
    val localDateTime = createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthName = getMonthName(localDateTime.month.number)
    val day = localDateTime.day
    val year = localDateTime.year
    return "$monthName $day, $year"
}

/**
 * 获取月份名称（支持多语言）
 */
@Composable
private fun getMonthName(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> stringResource(Res.string.component_card_month_jan)
        2 -> stringResource(Res.string.component_card_month_feb)
        3 -> stringResource(Res.string.component_card_month_mar)
        4 -> stringResource(Res.string.component_card_month_apr)
        5 -> stringResource(Res.string.component_card_month_may)
        6 -> stringResource(Res.string.component_card_month_jun)
        7 -> stringResource(Res.string.component_card_month_jul)
        8 -> stringResource(Res.string.component_card_month_aug)
        9 -> stringResource(Res.string.component_card_month_sep)
        10 -> stringResource(Res.string.component_card_month_oct)
        11 -> stringResource(Res.string.component_card_month_nov)
        12 -> stringResource(Res.string.component_card_month_dec)
        else -> stringResource(Res.string.component_card_month_jan)
    }
}
