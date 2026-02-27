package tech.zhifu.app.myhub.feature.capture.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.feature.capture.ReviewContentType
import tech.zhifu.app.myhub.ui.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.isWidthCompact

@Composable
internal fun ReviewMediaImage(
    imageItem: MediaItem?,
    summary: String,
    info: String,
    onPreview: (MediaItem) -> Unit,
    selectedContentType: ReviewContentType?,
    onSelectContent: (ReviewContentType) -> Unit
) {
    if (imageItem == null) return
    ReviewContentContainer(
        contentType = ReviewContentType.Image,
        selectedContentType = selectedContentType,
        onSelect = onSelectContent
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = LocalWindowSizeClass.current.isWidthCompact()
            if (isCompact) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ReviewMediaThumbnail(
                            item = imageItem,
                            onPreview = onPreview,
                            overlayLabel = imageItem.name.uppercase(),
                            showPlay = false
                        )
                    }
                    ReviewMediaDetails(
                        icon = Icons.Default.FilterCenterFocus,
                        title = "OCR Extraction · 文字提取",
                        summary = summary,
                        info = info,
                        summaryStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        infoStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ReviewMediaThumbnail(
                        item = imageItem,
                        onPreview = onPreview,
                        overlayLabel = imageItem.name.uppercase(),
                        showPlay = false
                    )
                    ReviewMediaDetails(
                        icon = Icons.Default.FilterCenterFocus,
                        title = "OCR Extraction · 文字提取",
                        summary = summary,
                        info = info,
                        summaryStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        infoStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                    )
                }
            }
        }
    }
}

@Composable

internal fun ReviewMediaVideo(
    videoItem: MediaItem?,
    summary: String,
    info: String,
    onPreview: (MediaItem) -> Unit,
    selectedContentType: ReviewContentType?,
    onSelectContent: (ReviewContentType) -> Unit
) {
    if (videoItem == null) return
    ReviewContentContainer(
        contentType = ReviewContentType.Video,
        selectedContentType = selectedContentType,
        onSelect = onSelectContent
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = LocalWindowSizeClass.current.isWidthCompact()
            if (isCompact) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ReviewMediaThumbnail(
                            item = videoItem,
                            onPreview = onPreview,
                            overlayLabel = videoItem.name.uppercase(),
                            showPlay = true
                        )
                    }
                    ReviewMediaDetails(
                        icon = Icons.Default.Movie,
                        title = "Video Metadata",
                        summary = summary,
                        info = info,
                        summaryStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        infoStyle = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ReviewMediaThumbnail(
                        item = videoItem,
                        onPreview = onPreview,
                        overlayLabel = videoItem.name.uppercase(),
                        showPlay = true
                    )
                    ReviewMediaDetails(
                        icon = Icons.Default.Movie,
                        title = "Video Metadata",
                        summary = summary,
                        info = info,
                        summaryStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        infoStyle = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewMediaThumbnail(
    item: MediaItem,
    onPreview: (MediaItem) -> Unit,
    overlayLabel: String,
    showPlay: Boolean
) {
    Box(
        modifier = Modifier.width(220.dp)
            .height(180.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            .clickable { onPreview(item) },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = item.file,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            error = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        )
        if (showPlay) {
            Box(
                modifier = Modifier.size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        Text(
            text = overlayLabel,
            style = TextStyle(
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            ),
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 10.dp)
        )
    }
}

@Composable
private fun ReviewMediaDetails(
    icon: ImageVector,
    title: String,
    summary: String,
    info: String,
    summaryStyle: TextStyle,
    infoStyle: TextStyle,
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth()
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            val trimmedSummary = summary.trim().take(64)
            if (trimmedSummary.isNotBlank()) {
                Text(
                    text = trimmedSummary,
                    style = summaryStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            val trimmedInfo = info.trim()
            if (trimmedInfo.isNotBlank()) {
                Text(
                    text = trimmedInfo,
                    style = infoStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

