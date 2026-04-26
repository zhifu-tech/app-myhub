package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.PreviewTokens
import tech.zhifu.app.myhub.feature.preview.sharedBounds
import tech.zhifu.app.myhub.feature.preview.sharedElement

@Composable
internal fun PreviewContent(
    card: ContentCard,
    previewState: PreviewState,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    snapshotController: PreviewSnapshotController? = null,
) {
    val contentModifier = if (animatedVisibilityScope != null) {
        modifier.sharedBounds(
            key = "content-preview-${card.card.id}",
            animatedVisibilityScope = animatedVisibilityScope,
            overlayClipShape = PreviewTokens.CardShape,
        )
    } else {
        modifier
    }
    val titleModifier = if (animatedVisibilityScope != null) {
        Modifier.sharedBounds(
            key = "content-title-${card.card.id}",
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }
    val coverModifier = if (animatedVisibilityScope != null) {
        Modifier.sharedElement(
            key = "content-image-${card.card.id}",
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }

    ElevatedCard(
        modifier = contentModifier
            .previewSnapshotSource(snapshotController)
            .clip(shape = PreviewTokens.CardShape),
        shape = PreviewTokens.CardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(all = 24.dp)
        ) {
            PreviewContentTopMeta(
                card = card,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 8.dp)
            )

            PreviewContentTitle(
                title = card.card.title,
                modifier = titleModifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp)
            )

            PreviewContentCover(
                card = card,
                previewState = previewState,
                modifier = coverModifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )

            PreviewContentMeta(
                card = card,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 24.dp)
            )

            PreviewContentNote(
                card = card,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 24.dp, bottom = 32.dp)
            )
        }
    }
}
