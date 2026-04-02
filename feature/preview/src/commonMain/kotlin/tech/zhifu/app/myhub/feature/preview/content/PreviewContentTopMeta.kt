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
import tech.zhifu.app.myhub.feature.preview.sharedElement
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.model.ContentCardIcon
import tech.zhifu.app.myhub.ui.model.toImageVector

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun PreviewContentTopMeta(
    card: ContentCard,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier,
) {
    val contentId = card.id
    val actionIcon = card.action.iconKey.toImageVector()
        ?: ContentCardIcon.EditNote.icon
    val actionColor = card.action.color
    val actionLabel = card.action.label
    val iconModifier = if (animatedVisibilityScope != null) {
        Modifier.sharedElement(
            key = "content-action-icon-$contentId",
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }
    val labelModifier = if (animatedVisibilityScope != null) {
        Modifier.sharedElement(
            key = "content-action-label-$contentId",
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }

    Box(modifier = modifier) {
        Icon(
            imageVector = actionIcon,
            contentDescription = null,
            tint = actionColor.copy(0.6f),
            modifier = iconModifier
                .size(20.dp),
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = actionColor.copy(0.6f),
            modifier = labelModifier
                .align(Alignment.CenterEnd),
        )
    }
}
