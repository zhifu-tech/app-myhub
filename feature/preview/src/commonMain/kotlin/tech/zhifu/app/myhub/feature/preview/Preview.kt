package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionSavingButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionShareButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewContent
import tech.zhifu.app.myhub.feature.preview.content.rememberPreviewSnapshotController
import tech.zhifu.app.myhub.ui.model.ContentCard

@Composable
fun Preview(
    card: ContentCard?,
    modifier: Modifier = Modifier,
    actionHide: () -> Unit = {},
) {
    val snapshotController = rememberPreviewSnapshotController()
    AnimatedContent(
        modifier = modifier.fillMaxSize(),
        targetState = card,
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
    ) { card ->
        if (card == null) {
            Spacer(modifier = Modifier.fillMaxSize())
        } else {
            PreviewOverlay(actionHide = actionHide)
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
                            card = card,
                            animatedVisibilityScope = this@AnimatedContent,
                            modifier = Modifier.weight(1f, fill = false),
                            snapshotController = snapshotController,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PreviewActionSavingButton(
                                card = card,
                                shareContentWidth = maxCardWidth,
                                snapshotController = snapshotController,
                            )
                            PreviewActionShareButton(
                                card = card,
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
                            card = card,
                            animatedVisibilityScope = this@AnimatedContent,
                            modifier = Modifier
                                .widthIn(max = maxCardWidth),
                            snapshotController = snapshotController,
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PreviewActionSavingButton(
                                card = card,
                                shareContentWidth = maxCardWidth,
                                snapshotController = snapshotController,
                            )
                            PreviewActionShareButton(
                                card = card,
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
}

@Composable
private fun PreviewOverlay(
    actionHide: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val overlayColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = actionHide,
            )
            .background(color = overlayColor)
    )
}
