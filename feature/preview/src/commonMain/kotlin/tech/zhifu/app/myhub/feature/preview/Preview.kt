package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionShareButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewContent

@Composable
fun PreviewOverlay(
    state: PreviewState,
    onShare: () -> Unit = {},
) {
    PreviewOverlay(
        state = state,
        onShare = onShare,
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
    onShare: () -> Unit = {},
    content: @Composable (PreviewPayload, Boolean) -> Unit,
    actionShare: @Composable (Boolean, onShare: () -> Unit) -> Unit,
) {
    PreviewOverlayBox(
        state = state,
        visible = visible,
        content = {
            Column(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp)
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val payload = state.payload ?: return@Column
                content(payload, visible)
                actionShare(visible, onShare)
            }
        }
    )
}

@Composable
private fun PreviewOverlayBox(
    state: PreviewState,
    visible: Boolean,
    content: @Composable @UiComposable BoxScope.() -> Unit,
) {
    val onHide: () -> Unit = remember(state) {
        {
            state.hide()
        }
    }
    val scrimAlpha = remember {
        Animatable(if (visible) 0.35f else 0f)
    }
    var keepInTree by remember { mutableStateOf(visible) }
    val scrimColor = MaterialTheme.colorScheme.scrim
    val scrimAnimationSpec = animationSpec()
    LaunchedEffect(visible) {
        if (visible) {
            keepInTree = true
        }
        scrimAlpha.animateTo(
            targetValue = if (visible) 0.35f else 0f,
            animationSpec = scrimAnimationSpec
        )
        if (!visible) {
            keepInTree = false
        }
    }
    if (!keepInTree) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = scrimColor.copy(alpha = scrimAlpha.value))
            }
            .then(other = if (visible) Modifier.clickable(onClick = onHide) else Modifier),
        contentAlignment = Alignment.Center,
        content = content
    )
}
