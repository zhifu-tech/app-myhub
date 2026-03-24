package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.ui.LocalSharedTransitionScope

@Composable
fun Modifier.sharedBounds(
    key: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
) = with(receiver = sharedTransitionScope) {
    sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = animatedVisibilityScope,
        clipInOverlayDuringTransition = OverlayClip(
            clipShape = MaterialTheme.shapes.large
        )
    )
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
