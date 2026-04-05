package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import tech.zhifu.app.myhub.ui.design.util.LocalSharedTransitionScope

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

object PreviewTransitionTokens {
    const val HOST_ENTER_DURATION_MS: Int = 500
    const val HOST_EXIT_DURATION_MS: Int = 300
    const val OVERLAY_ENTER_DURATION_MS: Int = 500
    const val OVERLAY_EXIT_DURATION_MS: Int = 300
    const val OVERLAY_ENTER_DELAY_MS: Int = 20
    const val HOST_SCALE_FROM: Float = 0.985f
    const val HOST_SCALE_TO: Float = 0.985f
}
