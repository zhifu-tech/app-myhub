package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.attribution
import tech.zhifu.app.myhub.datastore.model.domain.carrierImage
import tech.zhifu.app.myhub.datastore.model.domain.carrierVideo
import tech.zhifu.app.myhub.datastore.model.domain.code
import tech.zhifu.app.myhub.datastore.model.domain.content

@Composable
fun CardPreview(
    card: Card,
    modifier: Modifier = Modifier,
    onClick: ((Card) -> Unit)? = null,
    onEdit: ((Card) -> Unit)? = null,
    editable: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val contentMeta = card.metadata.content
    val foreground = card.foregroundColor()

    Box(
        modifier = modifier
            .hoverable(interactionSource = interactionSource)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick(card) }
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = card.previewBackgroundColor(),
            border = BorderStroke(1.dp, foreground.copy(alpha = 0.15f)),
            shadowElevation = if (isHovered) 28.dp else 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            CardContent(
                card = card,
                title = contentMeta?.title?.takeIf { it.isNotBlank() } ?: card.type.wire,
                dateLabel = formatMonthDay(card.updatedAt.toLocalDateTime(TimeZone.currentSystemDefault())),
                summary = contentMeta?.summary ?: contentMeta?.content,
                codeSnippet = card.metadata.code?.snippet,
                foreground = foreground
            )
        }

        if (editable && onEdit != null && isHovered) {
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
    }
}

@Composable
private fun CardContent(
    card: Card,
    title: String,
    dateLabel: String,
    summary: String?,
    codeSnippet: String?,
    foreground: Color
) {
    Column(
        modifier = Modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeaderRow(
            title = title,
            dateLabel = dateLabel,
            foreground = foreground
        )

        if (!summary.isNullOrBlank()) {
            Text(
                text = summary,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = foreground
                )
            )
        }

        if (!codeSnippet.isNullOrBlank()) {
            CodeSnippetBlock(codeSnippet = codeSnippet)
        }

        card.metadata.carrierImage?.let { image ->
            CardMediaRow(
                isVideo = false,
                title = "Image OCR",
                subtitle = image.url?.substringAfterLast('/').orEmpty().ifBlank { "Image Asset" },
                thumbnailModel = image.thumbnailUrl ?: image.url,
                foreground = foreground
            )
        }

        card.metadata.carrierVideo?.let { video ->
            CardMediaRow(
                isVideo = true,
                title = "Video Metadata",
                subtitle = buildVideoSubtitle(video.platform, video.durationSeconds),
                thumbnailModel = video.coverImageUrl,
                foreground = foreground
            )
        }

        if (card.tags.isNotEmpty()) {
            TagFlow(
                tags = card.tags.map { it.name },
                foreground = foreground
            )
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    dateLabel: String,
    foreground: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = foreground.copy(alpha = 0.75f)
        )
        Text(
            text = dateLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold
            ),
            color = foreground.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun CodeSnippetBlock(codeSnippet: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            codeSnippet.lines().take(3).forEach { line ->
                Text(
                    text = line,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = Color(0xFFE6E1E5)
                    )
                )
            }
        }
    }
}

@Composable
private fun TagFlow(
    tags: List<String>,
    foreground: Color
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = foreground.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, foreground.copy(alpha = 0.24f))
            ) {
                Text(
                    text = "#${tag.uppercase()}",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    ),
                    color = foreground.copy(alpha = 0.78f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CardMediaRow(
    isVideo: Boolean,
    title: String,
    subtitle: String,
    thumbnailModel: Any?,
    foreground: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.07f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailModel != null) {
                SubcomposeAsyncImage(
                    model = thumbnailModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = if (isVideo) Icons.Default.Movie else Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White.copy(alpha = 0.84f)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = foreground
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontSize = 9.sp, letterSpacing = 0.4.sp),
                color = foreground.copy(alpha = 0.68f)
            )
        }
    }
}


private fun Card.previewBackgroundColor(): Color {
    metadata.attribution?.styleColor
        ?.toComposeColorOrNull()
        ?.let { return it }
    val palette = listOf(
        Color(0xFFF2B8B5),
        Color(0xFFE6C975),
        Color(0xFFB4A3FF),
        Color(0xFFAECBFA),
        Color(0xFF6DD58C),
        Color(0xFFE8DEF8)
    )
    return palette[(id.hashCode() and Int.MAX_VALUE) % palette.size]
}

private fun Card.foregroundColor(): Color {
    return if (previewBackgroundColor().luminance() > 0.62f) Color(0xFF1C1B1F) else Color(0xFFFDF8FF)
}

private fun String.toComposeColorOrNull(): Color? {
    val hex = trim().removePrefix("#")
    return runCatching {
        when (hex.length) {
            6 -> Color(0xFF000000 or hex.toLong(16))
            8 -> Color(hex.toLong(16))
            else -> return null
        }
    }.getOrNull()
}

private fun buildVideoSubtitle(platform: String?, durationSeconds: Long?): String {
    val platformText = platform?.takeIf { it.isNotBlank() } ?: "Video Asset"
    val durationText = durationSeconds?.takeIf { it > 0 }?.let { seconds ->
        val minute = seconds / 60
        val second = seconds % 60
        "%d:%02d".format(minute, second)
    }
    return listOfNotNull(platformText, durationText)
        .joinToString(" • ")
}
