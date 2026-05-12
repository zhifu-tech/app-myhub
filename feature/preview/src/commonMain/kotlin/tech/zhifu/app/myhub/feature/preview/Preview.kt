package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.component.MediaGalleryDialog
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.datastore.model.domain.name
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionSavingButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionShareButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewContent
import tech.zhifu.app.myhub.feature.preview.content.PreviewOverlay
import tech.zhifu.app.myhub.feature.preview.content.PreviewPlaceholder
import tech.zhifu.app.myhub.feature.preview.content.rememberPreviewSnapshotController

@Composable
fun Preview(
    state: PreviewState,
    modifier: Modifier = Modifier,
) {
    val snapshotController = rememberPreviewSnapshotController()
    val mediaSession by state.mediaSession
    AnimatedContent(
        modifier = modifier.fillMaxSize(),
        targetState = state.card.value,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = PreviewTransitionTokens.OVERLAY_ENTER_DURATION_MS,
                    delayMillis = PreviewTransitionTokens.OVERLAY_ENTER_DELAY_MS,
                    easing = FastOutSlowInEasing,
                )
            ) togetherWith fadeOut(
                animationSpec = tween(
                    durationMillis = PreviewTransitionTokens.OVERLAY_EXIT_DURATION_MS,
                    easing = FastOutSlowInEasing,
                )
            )
        }
    ) { targetCard: ContentCard? ->
        if (targetCard == null) {
            if (state.pined) {
                PreviewPlaceholder()
            } else {
                Spacer(modifier = Modifier.fillMaxSize())
            }
        } else {
            if (!state.pined) {
                PreviewOverlay(state = state)
            }
            BoxWithConstraints(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val maxCardWidth = minOf(
                    a = 390.dp,
                    b = maxOf(
                        a = 360.dp,
                        b = minOf(maxWidth, maxHeight) * 0.92f
                    )
                )
                if (targetCard.card.status == CardStatus.DRAFT) {
                    PreviewContent(
                        card = targetCard,
                        previewState = state,
                        animatedVisibilityScope = this@AnimatedContent,
                        modifier = Modifier
                            .width(maxCardWidth)
                            .padding(vertical = 16.dp),
                        snapshotController = snapshotController,
                    )
                    return@BoxWithConstraints
                }

                val shareActionSize = 48.dp
                val adjustSize = 12.dp * 2
                val expectHorizontalMaxWidth = maxCardWidth + shareActionSize * 2 + adjustSize
                if (maxWidth < expectHorizontalMaxWidth) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = maxCardWidth)
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PreviewContent(
                            card = targetCard,
                            previewState = state,
                            animatedVisibilityScope = this@AnimatedContent,
                            modifier = Modifier.weight(1f, fill = false),
                            snapshotController = snapshotController,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PreviewActionSavingButton(
                                card = targetCard,
                                shareContentWidth = maxCardWidth,
                                snapshotController = snapshotController,
                            )
                            PreviewActionShareButton(
                                card = targetCard,
                                shareContentWidth = maxCardWidth,
                                snapshotController = snapshotController,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .widthIn(max = expectHorizontalMaxWidth)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Spacer(modifier.size(shareActionSize))
                        PreviewContent(
                            card = targetCard,
                            previewState = state,
                            animatedVisibilityScope = this@AnimatedContent,
                            modifier = Modifier
                                .widthIn(max = maxCardWidth),
                            snapshotController = snapshotController,
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PreviewActionSavingButton(
                                card = targetCard,
                                shareContentWidth = maxCardWidth,
                                snapshotController = snapshotController,
                            )
                            PreviewActionShareButton(
                                card = targetCard,
                                shareContentWidth = maxCardWidth,
                                snapshotController = snapshotController,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }
    }
    mediaSession?.let { session ->
        MediaGalleryDialog(
            items = session.items.map { item ->
                MediaItem(
                    id = item.media.id,
                    name = item.media.name(),
                    previewUrl = item.media.accessUrl,
                    mediaType = item.media.mediaType,
                    thumbnailUrl = item.media.thumbAccessUrl,
                )
            },
            initialIndex = session.initialIndex,
            onDismiss = state::hideMedia,
            titleForIndex = { index ->
                session.items.getOrNull(index)?.cardTitle
            },
        )
    }
}
