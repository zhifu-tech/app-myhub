package tech.zhifu.app.myhub.component.card.utils

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant as KxInstant

/**
 * 格式化卡片日期为 "Oct 24, 2023" 格式
 *
 * 将 kotlin.time.Instant 转换为 kotlinx.datetime.Instant 进行格式化
 */
fun kotlin.time.Instant.formatCardDate(): String {
    // 转换为 kotlinx.datetime.Instant
    val kxInstant = KxInstant.fromEpochSeconds(this.epochSeconds)

    // 转换为本地时间
    val localDateTime = kxInstant.toLocalDateTime(TimeZone.currentSystemDefault())

    // 格式化
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val monthName = monthNames.getOrNull(localDateTime.monthNumber - 1) ?: "Jan"
    val day = localDateTime.dayOfMonth
    val year = localDateTime.year

    return "$monthName $day, $year"
}

/**
 * 格式化相对时间，如 "Added 2 hours ago"
 */
fun kotlin.time.Instant.formatRelativeTime(): String {
    val now = Clock.System.now()
    val diff = (now - this).inWholeMilliseconds

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> {
            val minutes = diff / 60_000
            "Added $minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        }
        diff < 86_400_000 -> {
            val hours = diff / 3_600_000
            "Added $hours ${if (hours == 1L) "hour" else "hours"} ago"
        }
        diff < 2_592_000_000 -> {
            val days = diff / 86_400_000
            "Added $days ${if (days == 1L) "day" else "days"} ago"
        }
        else -> {
            // 超过 30 天，使用绝对日期
            formatCardDate()
        }
    }
}
