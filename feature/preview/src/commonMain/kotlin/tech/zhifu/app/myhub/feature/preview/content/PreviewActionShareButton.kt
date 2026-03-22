package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.animationSpec
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@Composable
internal fun PreviewActionShareButton(
    visible: Boolean,
    onShare: () -> Unit,
) {
    logger.debug { "PreviewActionShareButton called." }
    val actionAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animationSpec(),
        label = "previewActionAlpha"
    )

    Row(
        modifier = Modifier
            .graphicsLayer {
                val scale = 0.96f + 0.04f * actionAlpha
                alpha = 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(shape = RoundedCornerShape(9999.dp))
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(9999.dp),
            )
            .clickable(onClick = onShare)
            .padding(horizontal = 20.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Share Entry",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.surfaceVariant,
        )
        Icon(
            imageVector = Icons.Outlined.IosShare,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}
