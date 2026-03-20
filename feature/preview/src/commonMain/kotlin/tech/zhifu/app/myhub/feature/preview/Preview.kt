package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.content.PreviewActionShareButton
import tech.zhifu.app.myhub.feature.preview.content.PreviewContent
import tech.zhifu.app.myhub.ui.LocalSharedTransitionScope

@Composable
fun Preview(
    state: PreviewState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
) {
    val visible = state.visible
    val payload = state.payload ?: return
    val showOverlay = visible || sharedTransitionScope.isTransitionActive
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.35f else 0f,
        animationSpec = animationSpec(),
        label = "previewScrimAlpha"
    )
    if (!showOverlay && scrimAlpha == 0f) return
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val targetWidth = minOf(maxWidth * 0.92f, 360.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = scrimAlpha))
                .then(
                    other = if (showOverlay) {
                        Modifier.clickable { state.hide() }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = modifier
                    .width(targetWidth)
                    .padding(vertical = 16.dp)
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreviewContent(
                    sharedTransitionScope = sharedTransitionScope,
                    payload = payload,
                    visible = visible,
                )
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = animationSpec()
                    ) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = animationSpec()
                    ),
                    exit = fadeOut(
                        animationSpec = animationSpec()
                    ) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = animationSpec()
                    ),
                ) {
                    PreviewActionShareButton(
                        onShare = { /*fixme*/ },
                    )
                }
            }
        }
    }
}
