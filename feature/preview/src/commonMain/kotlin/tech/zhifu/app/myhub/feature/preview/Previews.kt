package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import tech.zhifu.app.myhub.ui.LocalSharedTransitionScope

@Composable
fun Modifier.sharedBounds(
    key: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    overlayClipShape: Shape? = null,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
) = with(receiver = sharedTransitionScope) {
    val sharedContentState = rememberSharedContentState(key = key)
    if (overlayClipShape == null) {
        sharedBounds(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    } else {
        sharedBounds(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = animatedVisibilityScope,
            clipInOverlayDuringTransition = OverlayClip(clipShape = overlayClipShape),
        )
    }
}

@Composable
fun Modifier.sharedElement(
    key: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
): Modifier = with(receiver = sharedTransitionScope) {
    sharedElement(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = animatedVisibilityScope,
    )
}
