package tech.zhifu.app.myhub.component.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
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
import tech.zhifu.app.myhub.component.card.resources.component_card_type_article
import tech.zhifu.app.myhub.component.card.resources.component_card_type_checklist
import tech.zhifu.app.myhub.component.card.resources.component_card_type_code
import tech.zhifu.app.myhub.component.card.resources.component_card_type_dictionary
import tech.zhifu.app.myhub.component.card.resources.component_card_type_idea
import tech.zhifu.app.myhub.component.card.resources.component_card_type_quote
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType

/**
 * Card 扩展方法
 * 提供卡片显示相关的属性和方法
 */

/**
 * 获取卡片的显示标题
 * 根据卡片类型返回合适的标题，支持多语言
 */
@Composable
fun Card.getDisplayTitle(): String {
    return title ?: when (type) {
        CardType.QUOTE -> metadata?.quoteAuthor ?: stringResource(Res.string.component_card_type_quote)
        CardType.CODE -> stringResource(Res.string.component_card_type_code)
        CardType.IDEA -> stringResource(Res.string.component_card_type_idea)
        CardType.ARTICLE -> stringResource(Res.string.component_card_type_article)
        CardType.DICTIONARY -> content.split(" ").firstOrNull()
            ?: stringResource(Res.string.component_card_type_dictionary)

        CardType.CHECKLIST -> stringResource(Res.string.component_card_type_checklist)
    }
}

/**
 * 获取卡片内容的预览（截取前N个字符）
 */
fun Card.getContentPreview(maxLength: Int = 80): String {
    val preview = content.take(maxLength)
    return if (content.length > maxLength) "$preview..." else preview
}

/**
 * 获取卡片类型的图标颜色
 */
val Card.typeIconColor: Color
    get() = when (type) {
        CardType.QUOTE -> Color(0xFF8B5CF6) // purple
        CardType.CODE -> Color(0xFF10B981) // green
        CardType.IDEA -> Color(0xFFF59E0B) // amber
        CardType.ARTICLE -> Color(0xFF3B82F6) // blue
        CardType.DICTIONARY -> Color(0xFF06B6D4) // cyan
        CardType.CHECKLIST -> Color(0xFFEF4444) // red
    }

/**
 * 获取卡片类型的图标文本
 */
val Card.typeIconText: String
    get() = when (type) {
        CardType.QUOTE -> "Q"
        CardType.CODE -> "C"
        CardType.IDEA -> "I"
        CardType.ARTICLE -> "A"
        CardType.DICTIONARY -> "D"
        CardType.CHECKLIST -> "✓"
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
