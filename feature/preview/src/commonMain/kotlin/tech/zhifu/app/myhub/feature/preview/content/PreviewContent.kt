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
import tech.zhifu.app.myhub.feature.preview.PreviewPayload
import tech.zhifu.app.myhub.feature.preview.sharedBounds
import tech.zhifu.app.myhub.feature.preview.sharedElement

@Composable
internal fun PreviewContent(
    payload: PreviewPayload,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    snapshotController: PreviewSnapshotController? = null,
) {
    val contentModifier = if (animatedVisibilityScope != null) {
        modifier.sharedBounds(
            key = "content-preview-${payload.id}",
            animatedVisibilityScope = animatedVisibilityScope,
            overlayClipShape = MaterialTheme.shapes.large,
        )
    } else {
        modifier
    }
    val titleModifier = if (animatedVisibilityScope != null) {
        Modifier.sharedBounds(
            key = "content-title-${payload.id}",
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }
    val coverModifier = if (animatedVisibilityScope != null) {
        Modifier.sharedElement(
            key = "content-image-${payload.id}",
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        Modifier
    }

    ElevatedCard(
        modifier = contentModifier
            .previewSnapshotSource(snapshotController)
            .clip(shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
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
                payload = payload,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 8.dp)
            )

            PreviewContentTitle(
                title = payload.title.orEmpty(),
                modifier = titleModifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp)
            )

            PreviewContentCover(
                payload = payload,
                modifier = coverModifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )

            PreviewContentMeta(
                payload = payload,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 24.dp)
            )

            PreviewContentNote(
                payload = payload,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 24.dp, bottom = 32.dp)
            )
        }
    }
}
