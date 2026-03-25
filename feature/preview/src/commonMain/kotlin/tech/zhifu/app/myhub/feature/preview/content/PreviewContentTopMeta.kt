package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.PreviewPayload
import tech.zhifu.app.myhub.feature.preview.sharedBounds

@Composable
internal fun PreviewContentTopMeta(
    payload: PreviewPayload,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier,
) {
    val contentId = payload.id
    val actionIcon = payload.actionIcon
    val actionColor = payload.actionColor
    val actionLabel = payload.actionLabel

    Box(modifier = modifier) {
        Icon(
            imageVector = actionIcon,
            contentDescription = null,
            tint = actionColor.copy(0.6f),
            modifier = Modifier
                .apply {
                    if (animatedVisibilityScope != null) {
                        sharedBounds(
                            key = "content-action-icon-$contentId",
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                }
                .size(20.dp),
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = actionColor.copy(0.6f),
            modifier = Modifier
                .apply {
                    if (animatedVisibilityScope != null) {
                        sharedBounds(
                            key = "content-action-label-$contentId",
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                }
                .align(Alignment.CenterEnd),
        )
    }
}

@Composable
internal fun PreviewContentTopMetaSnapshot(
    payload: PreviewPayload,
    modifier: Modifier,
) {
    val actionIcon = payload.actionIcon
    val actionColor = payload.actionColor
    val actionLabel = payload.actionLabel

    Box(modifier = modifier) {
        Icon(
            imageVector = actionIcon,
            contentDescription = null,
            tint = actionColor.copy(0.6f),
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = actionColor.copy(0.6f),
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}
