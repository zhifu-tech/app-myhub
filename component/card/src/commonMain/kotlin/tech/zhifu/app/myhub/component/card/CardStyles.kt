package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
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
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.content

/**
 * 统一的卡片样式配置
 *
 * 遵循 Material Design 3 设计规范，确保所有卡片样式一致
 * 根据设计稿要求严格还原 UI
 */
object CardStyles {
    /**
     * 卡片圆角，与设计稿 rounded-2xl (16dp) 一致
     */
    val CornerRadius = 16.dp
    val Shape = RoundedCornerShape(CornerRadius)

    /**
     * 卡片阴影（Elevation）
     * Material 3 规范：
     * - 默认：1dp（卡片在表面）
     * - Hover：4dp（交互时提升）
     */
    @Composable
    fun cardElevation(isHovered: Boolean) = CardDefaults.cardElevation(
        defaultElevation = if (isHovered) 4.dp else 1.dp
    )
}

fun formatMonthDay(dateTime: LocalDateTime): String {
    val month = when (dateTime.month.number) {
        1 -> "JAN"
        2 -> "FEB"
        3 -> "MAR"
        4 -> "APR"
        5 -> "MAY"
        6 -> "JUN"
        7 -> "JUL"
        8 -> "AUG"
        9 -> "SEP"
        10 -> "OCT"
        11 -> "NOV"
        12 -> "DEC"
        else -> "JAN"
    }
    return "$month ${dateTime.day}"
}

fun Card.getContentPreview(maxLength: Int = 80): String {
    val content = metadata.content?.content.orEmpty()
    val preview = content.take(maxLength)
    return if (content.length > maxLength) "$preview..." else preview
}

@Composable
fun Card.formatCreatedTime(): String {
    val localDateTime = createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthName = when (localDateTime.month.number) {
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
    return "$monthName ${localDateTime.day}, ${localDateTime.year}"
}
