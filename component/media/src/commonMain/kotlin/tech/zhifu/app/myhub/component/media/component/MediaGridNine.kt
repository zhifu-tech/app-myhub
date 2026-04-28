package tech.zhifu.app.myhub.component.media.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.displayImageModel

@Composable
fun MediaGridNine(
    items: List<MediaItem>,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 9,
    onItemClick: (Int) -> Unit,
    overlayContent: @Composable BoxScope.(index: Int, item: MediaItem) -> Unit = { _, _ -> },
) {
    if (items.isEmpty()) return
    val visibleItems = items.take(maxVisibleItems)
    val layoutSpec = mediaGridLayoutSpec(visibleItems.size)
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val spacing = 6.dp
        Layout(
            content = {
                visibleItems.forEachIndexed { index, item ->
                    MediaGridTile(
                        item = item,
                        index = index,
                        visibleCount = visibleItems.size,
                        totalCount = items.size,
                        onClick = { onItemClick(index) },
                        overlayContent = overlayContent,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { measurables, constraints ->
            val spacingPx = spacing.roundToPx()
            val cols = 6
            val rows = layoutSpec.maxOf { it.y + it.h }
            val availableWidth = (constraints.maxWidth - spacingPx * (cols - 1)).coerceAtLeast(0)
            val baseColUnit = availableWidth / cols
            var remainderWidth = availableWidth % cols
            val colUnits = IntArray(cols) {
                baseColUnit + if (remainderWidth-- > 0) 1 else 0
            }
            val colOffsets = IntArray(cols + 1)
            for (i in 0 until cols) {
                colOffsets[i + 1] = colOffsets[i] + colUnits[i]
            }

            val availableHeight = if (constraints.hasBoundedHeight) {
                (constraints.maxHeight - spacingPx * (rows - 1)).coerceAtLeast(0)
            } else {
                colUnits.sum() * rows / cols
            }
            val baseRowUnit = availableHeight / rows
            var remainderHeight = availableHeight % rows
            val rowUnits = IntArray(rows) {
                baseRowUnit + if (remainderHeight-- > 0) 1 else 0
            }
            val rowOffsets = IntArray(rows + 1)
            for (i in 0 until rows) {
                rowOffsets[i + 1] = rowOffsets[i] + rowUnits[i]
            }

            val placeables = measurables.mapIndexed { index, measurable ->
                val cell = layoutSpec[index]
                val itemWidth = (colOffsets[cell.x + cell.w] - colOffsets[cell.x]) +
                    spacingPx * (cell.w - 1)
                val itemHeight = (rowOffsets[cell.y + cell.h] - rowOffsets[cell.y]) +
                    spacingPx * (cell.h - 1)
                measurable.measure(
                    Constraints.fixed(
                        width = itemWidth,
                        height = itemHeight,
                    )
                )
            }
            val layoutWidth = constraints.maxWidth
            val layoutHeight = rowUnits.sum() + spacingPx * (rows - 1)
            layout(layoutWidth, layoutHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val cell = layoutSpec[index]
                    val x = colOffsets[cell.x] + spacingPx * cell.x
                    val y = rowOffsets[cell.y] + spacingPx * cell.y
                    placeable.placeRelative(x = x, y = y)
                }
            }
        }
    }
}

@Composable
private fun MediaGridTile(
    item: MediaItem,
    index: Int,
    visibleCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    overlayContent: @Composable BoxScope.(index: Int, item: MediaItem) -> Unit,
) {
    val shape = RoundedCornerShape(
        size = if (visibleCount == 1) 18.dp else 14.dp
    )
    val imageModel = item.displayImageModel()
    Box(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (item.isVideo && item.thumbnailUrl.isNullOrBlank()) {
            MediaGridPlaceholder(label = item.name, showPlay = true)
        } else {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    MediaGridPlaceholder(label = item.name, showPlay = item.isVideo)
                },
                error = {
                    MediaGridPlaceholder(label = item.name, showPlay = item.isVideo)
                },
            )
        }
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        if (index == visibleCount - 1 && totalCount > visibleCount) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+${totalCount - visibleCount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        overlayContent(index, item)
    }
}

private data class MediaGridCell(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
)

private fun mediaGridLayoutSpec(
    count: Int,
): List<MediaGridCell> = when (count.coerceIn(1, 9)) {
    1 -> listOf(
        MediaGridCell(0, 0, 6, 6),
    )

    2 -> listOf(
        MediaGridCell(0, 0, 3, 6),
        MediaGridCell(3, 0, 3, 6),
    )

    3 -> listOf(
        MediaGridCell(0, 0, 4, 6),
        MediaGridCell(4, 0, 2, 3),
        MediaGridCell(4, 3, 2, 3),
    )

    4 -> listOf(
        MediaGridCell(0, 0, 3, 3),
        MediaGridCell(3, 0, 3, 3),
        MediaGridCell(0, 3, 3, 3),
        MediaGridCell(3, 3, 3, 3),
    )

    5 -> listOf(
        MediaGridCell(0, 0, 4, 4),
        MediaGridCell(4, 0, 2, 4),
        MediaGridCell(0, 4, 2, 2),
        MediaGridCell(2, 4, 2, 2),
        MediaGridCell(4, 4, 2, 2),
    )

    6 -> listOf(
        MediaGridCell(0, 0, 4, 4),
        MediaGridCell(4, 0, 2, 2),
        MediaGridCell(4, 2, 2, 2),
        MediaGridCell(0, 4, 2, 2),
        MediaGridCell(2, 4, 2, 2),
        MediaGridCell(4, 4, 2, 2),
    )

    7 -> listOf(
        MediaGridCell(0, 0, 2, 4),
        MediaGridCell(2, 0, 2, 4),
        MediaGridCell(4, 0, 2, 2),
        MediaGridCell(4, 2, 2, 2),
        MediaGridCell(0, 4, 2, 2),
        MediaGridCell(2, 4, 2, 2),
        MediaGridCell(4, 4, 2, 2),
    )

    8 -> listOf(
        MediaGridCell(0, 0, 2, 4),
        MediaGridCell(2, 0, 2, 2),
        MediaGridCell(4, 0, 2, 2),
        MediaGridCell(2, 2, 2, 2),
        MediaGridCell(4, 2, 2, 2),
        MediaGridCell(0, 4, 2, 2),
        MediaGridCell(2, 4, 2, 2),
        MediaGridCell(4, 4, 2, 2),
    )

    else -> List(9) { index ->
        MediaGridCell(
            x = (index % 3) * 2,
            y = (index / 3) * 2,
            w = 2,
            h = 2,
        )
    }
}

@Composable
private fun MediaGridPlaceholder(
    label: String,
    showPlay: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (showPlay) {
            Icon(
                imageVector = Icons.Outlined.PlayCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Center)
                    .aspectRatio(1f)
                    .size(28.dp),
            )
        }
    }
}
