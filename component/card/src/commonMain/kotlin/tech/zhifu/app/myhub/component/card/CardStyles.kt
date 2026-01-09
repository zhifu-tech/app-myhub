package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 统一的卡片样式配置
 *
 * 遵循 Material Design 3 设计规范，确保所有卡片样式一致
 * 根据设计稿要求严格还原 UI
 */
object CardStyles {
    /**
     * 卡片圆角
     * Material 3 规范：小卡片使用 12dp 圆角
     */
    val CornerRadius = 12.dp
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

    /**
     * 卡片边框
     * Material 3 规范：使用 outline color，50% 透明度
     * 注意：需要在 @Composable 上下文中调用
     */
    @Composable
    fun defaultBorder() = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )

    /**
     * 卡片内边距
     * Material 3 规范：卡片内容区域使用 20-24dp 内边距
     */
    val ContentPadding = 24.dp
    val ContentPaddingSmall = 20.dp

    /**
     * IdeaCard 特定样式
     * 根据设计稿，IdeaCard 使用浅黄色背景
     */
    object IdeaCard {
        /**
         * 浅色模式：yellow-50 (#FEF9E7)
         * 深色模式：深棕色
         * 根据HTML设计稿：bg-yellow-50
         */
        fun backgroundColor(isDark: Boolean): Color {
            return if (isDark) {
                Color(0xFF2A261C) // 深棕色
            } else {
                Color(0xFFFEF9E7) // yellow-50，严格对齐设计稿
            }
        }

        /**
         * 边框颜色
         * 根据HTML设计稿：border-yellow-200 (#FDE68A)
         */
        fun borderColor(isDark: Boolean): Color {
            return if (isDark) {
                Color(0xFF92400E).copy(alpha = 0.5f)
            } else {
                Color(0xFFFDE68A) // yellow-200，严格对齐设计稿
            }
        }

        fun border(isDark: Boolean) = BorderStroke(
            width = 1.dp,
            color = borderColor(isDark)
        )
    }

    /**
     * QuoteCard 特定样式
     * 根据 HTML Demo：bg-[#fdfbf7] (浅米色背景)
     */
    object QuoteCard {
        /**
         * 浅色模式：#fdfbf7 (浅米色，对应 HTML bg-[#fdfbf7])
         * 深色模式：深灰色背景
         * 注意：需要在 @Composable 上下文中调用
         */
        @Composable
        fun backgroundColor(isDark: Boolean): Color {
            return if (isDark) {
                Color(0xFF1e2025) // 深灰色
            } else {
                Color(0xFFfdfbf7) // 浅米色，符合 HTML Demo
            }
        }
    }

    /**
     * ArticleCard 渐变颜色
     * 根据设计稿，使用紫色到粉色的渐变
     */
    object ArticleCard {
        val GradientColors = listOf(
            Color(0xFF6366f1), // indigo
            Color(0xFFa855f7), // purple
            Color(0xFFec4899)  // pink
        )
    }
}
