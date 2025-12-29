package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.component.card.utils.formatCardDate
import tech.zhifu.app.myhub.datastore.model.Card

@Composable
fun QuoteCard(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
) {

    val isDark = isSystemInDarkTheme()
    val quoteBgColor = if (isDark) {
        Color(0xFF1e2025)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // 使用 remember 缓存格式化结果
    val formattedDate = remember(card.updatedAt) {
        card.updatedAt.formatCardDate()
    }

    // 使用 remember 缓存数据提取
    val author = remember(card) {
        card.metadata?.quoteAuthor
            ?: card.author
            ?: "Unknown Author"
    }

    val category = remember(card) {
        card.metadata?.quoteCategory ?: "GENERAL"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCardClick(card) }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = quoteBgColor
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 4.dp else 1.dp
        )
    ) {
        Box {
            // 编辑按钮（hover 时显示）
            if (isHovered) {
                IconButton(
                    onClick = { onEdit(card) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 分类标签和日期
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isDark) {
                            Color(0xFFFFB020).copy(alpha = 0.3f)
                        } else {
                            Color(0xFFFFF4E6)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) {
                                Color(0xFFFFB020)
                            } else {
                                Color(0xFF92400E)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 引言内容（Serif 字体，斜体）
                Text(
                    text = card.content,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    ),
                    lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.5,
                    color = if (isDark) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    } else {
                        Color(0xFF1e293b)
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 分隔线和作者
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (isDark) {
                        Color.White.copy(alpha = 0.05f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "— $author",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { onFavorite(card) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) {
                                Color(0xFFFFB020)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
