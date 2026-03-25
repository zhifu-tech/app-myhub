package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionShareButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewContent

@Composable
fun Preview(
    state: PreviewState,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        modifier = modifier.fillMaxSize(),
        targetState = state.payload,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { payload ->
        if (payload == null) {
            Spacer(modifier = Modifier.fillMaxSize())
        } else {
            PreviewOverlay(state = state)
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
                            payload = payload,
                            animatedVisibilityScope = this@AnimatedContent,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        PreviewActionShareButton(
                            sharePayload = payload,
                            shareContentWidth = maxCardWidth,
                        )
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
                            payload = payload,
                            animatedVisibilityScope = this@AnimatedContent,
                            modifier = Modifier
                                .widthIn(max = maxCardWidth),
                        )
                        PreviewActionShareButton(
                            sharePayload = payload,
                            shareContentWidth = maxCardWidth,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewOverlay(
    state: PreviewState
) {
    val onClick = remember { { state.hide() } }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .background(color = MaterialTheme.colorScheme.scrim.copy(0.35f))
    )
}
