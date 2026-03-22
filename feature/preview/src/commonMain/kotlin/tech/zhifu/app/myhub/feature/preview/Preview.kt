package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionShareButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewContent
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@Composable
fun PreviewOverlay(
    state: PreviewState,
) {
    logger.debug { "PreviewOverlay route called." }
    PreviewOverlay(
        state = state,
        content = { payload, visible ->
            PreviewContent(payload, visible)
        },
        actionShare = { visible, onShare ->
            PreviewActionShareButton(
                visible = visible,
                onShare = onShare
            )
        }
    )
}

@Composable
fun PreviewOverlay(
    state: PreviewState,
    visible: Boolean = state.visible,
    content: @Composable (PreviewPayload, Boolean) -> Unit,
    actionShare: @Composable (Boolean, onShare: () -> Unit) -> Unit,
) {
    logger.debug { "PreviewOverlay called." }
    val content: @Composable () -> Unit =
        remember(content, state, visible) {
            {
                logger.debug { "PreviewOverlay content content called." }
                state.payload ?: return@remember
                content(state.payload!!, visible)
            }
        }
    val onShare: () -> Unit = remember {
        {
            logger.debug { "PreviewOverlay onShare called." }
        }
    }
    val actionShare: @Composable () -> Unit =
        remember(actionShare, state, visible) {
            {
                state.payload ?: return@remember
                actionShare(visible, onShare)
            }
        }
    PreviewOverlayBox(
        state = state,
        showOverlay = visible,
        visible = visible,
        content = {
            logger.debug { "PreviewOverlayColumn called." }
            val targetWidth = minOf(maxWidth * 0.92f, 360.dp)
            Column(
                modifier = Modifier
                    .width(targetWidth)
                    .padding(vertical = 16.dp)
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                logger.debug { "PreviewOverlayBox content  column called." }
                content()
                actionShare()
            }
        }
    )
}

@Composable
private fun PreviewOverlayBox(
    state: PreviewState,
    showOverlay: Boolean,
    visible: Boolean,
    content: @Composable @UiComposable BoxWithConstraintsScope.() -> Unit,
) {
    logger.debug { "PreviewOverlayBox called." }
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.35f else 0f,
        animationSpec = animationSpec(),
        label = "previewScrimAlpha"
    )
    if (!showOverlay && scrimAlpha == 0f) return
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = scrimAlpha))
            .then(other = if (showOverlay) Modifier.clickable { state.hide() } else Modifier),
        contentAlignment = Alignment.Center,
        content = content
    )
}
