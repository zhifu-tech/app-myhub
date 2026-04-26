package tech.zhifu.app.myhub.component.media.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.util.displayImageModel

@Composable
fun MediaThumbnail(
    item: MediaItem,
    onRemove: () -> Unit,
    onPreview: () -> Unit,
    enabled: Boolean
) {
    val thumbShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier.size(80.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(8.dp, thumbShape)
                .clip(thumbShape)
                .background(Color(0xFF2B2930))
                .border(
                    width = 1.dp,
                    color = Color(0xFF49454F),
                    shape = thumbShape
                )
                .clickable(enabled = enabled, onClick = onPreview)
        ) {
            if (item.isVideo) {
                ThumbnailPlaceholder(
                    icon = Icons.Default.Movie,
                    label = item.name
                )
            } else {
                SubcomposeAsyncImage(
                    model = item.displayImageModel(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        ThumbnailPlaceholder(
                            icon = Icons.Default.Image,
                            label = item.name
                        )
                    },
                    error = {
                        ThumbnailPlaceholder(
                            icon = Icons.Default.Image,
                            label = item.name
                        )
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .clip(CircleShape)
                .shadow(8.dp, CircleShape)
                .background(Color(0xFF36343B))
                .border(1.dp, Color(0xFF49454F), CircleShape)
                .clickable(enabled = enabled, onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ThumbnailPlaceholder(
    icon: ImageVector,
    label: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 2
        )
    }
}
