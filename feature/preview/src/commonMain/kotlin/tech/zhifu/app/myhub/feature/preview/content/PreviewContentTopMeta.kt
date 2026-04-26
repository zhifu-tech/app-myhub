package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.component.card.mixed.CardStatusAction
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.feature.preview.sharedElement

@Composable
internal fun PreviewContentTopMeta(
    card: ContentCard,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier,
) {
    val contentId = card.card.id
    val labelModifier = if (animatedVisibilityScope != null) {
        Modifier.sharedElement(
            key = "content-status-$contentId",
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }

    Box(modifier = modifier) {
        CardStatusAction(
            status = card.card.status,
            modifier = labelModifier.align(Alignment.CenterEnd),
            textAlpha = 0.6f,
        )
    }
}
