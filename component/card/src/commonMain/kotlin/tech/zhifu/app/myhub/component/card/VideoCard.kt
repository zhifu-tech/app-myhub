package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.videoMetadata
import tech.zhifu.app.myhub.component.card.CardStyles

/**
 * Video 卡片组件
 * 根据设计稿实现：封面图 + 播放按钮 + 标题 + 时长
 */
@Composable
fun VideoCard(
    card: Card,
    onEdit: (Card) -> Unit = {},
    onFavorite: (Card) -> Unit = {},
    onCardClick: (Card) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val metadata = remember(card) {
        card.videoMetadata
    }
    
    val title = remember(card) {
        card.title ?: "Video"
    }
    
    val durationText = remember(metadata) {
        metadata?.durationSeconds?.let { seconds ->
            val minutes = seconds / 60
            val secs = seconds % 60
            "$minutes:${secs.toString().padStart(2, '0')}"
        } ?: ""
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                onClick = { onCardClick(card) }
            ),
        shape = RoundedCornerShape(16.dp), // rounded-2xl = 16.dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardStyles.defaultBorder(),
        elevation = CardStyles.cardElevation(isHovered)
    ) {
        Column {
            // 视频封面区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                // 封面图（如果有）
                if (metadata?.thumbnailUrl != null) {
                    // TODO: 使用图片加载库加载封面图
                    // 暂时使用占位符
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Video",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // 占位符背景
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Video",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // 播放按钮（居中）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { /* TODO: 打开视频链接 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f),
                        tonalElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play video",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // "VIDEO SUMMARY" 标签（左上角）
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = "VIDEO SUMMARY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            // 标题和时长
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // p-4 = 16.dp
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    if (durationText.isNotEmpty()) {
                        Text(
                            text = "$durationText • Watch later",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 收藏按钮（hover 时显示）
                if (isHovered) {
                    Icon(
                        imageVector = Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onFavorite(card) }
                    )
                }
            }
        }
    }
}
