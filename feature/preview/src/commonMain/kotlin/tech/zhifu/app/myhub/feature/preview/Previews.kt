package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.ui.LocalSceneAnimatedContentScope
import tech.zhifu.app.myhub.ui.LocalSharedTransitionScope

@Composable
fun SharedTransitionScope.sharedElement(
    key: String,
): Modifier = Modifier.sharedElement(
    sharedContentState = rememberSharedContentState(key = key),
    animatedVisibilityScope = LocalSceneAnimatedContentScope.current,
    boundsTransform = { _, _ ->
        tween(
            durationMillis = PreviewState.SHARED_BOUNDS_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    },
)

@Composable
fun Modifier.sharedElementWithCallerManagedVisibility(
    key: String,
    visible: Boolean
): Modifier = with(receiver = LocalSharedTransitionScope.current) {
    Modifier.sharedElementWithCallerManagedVisibility(
        sharedContentState = rememberSharedContentState(key = key),
        visible = visible,
        boundsTransform = { _, _ ->
            tween(
                durationMillis = PreviewState.SHARED_BOUNDS_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        },
    )
}

@Composable
fun Modifier.sharedBounds(
    key: String,
    sharedEnabled: Boolean = true,
    enter: EnterTransition = fadeIn(animationSpec = animationSpec()),
    exit: ExitTransition = fadeOut(animationSpec = animationSpec()),
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
    zIndexInOverlay: Float = -1f,
): Modifier = if (sharedEnabled) with(receiver = LocalSharedTransitionScope.current) {
    sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = LocalSceneAnimatedContentScope.current,
        enter = enter,
        exit = exit,
        boundsTransform = { _, _ ->
            tween(
                durationMillis = PreviewState.SHARED_BOUNDS_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        },
        resizeMode = resizeMode,
        zIndexInOverlay = zIndexInOverlay,
    )
} else Modifier

@Composable
fun animationSpec(): TweenSpec<Float> = tween(
    durationMillis = PreviewState.SHARED_BOUNDS_DURATION_MS,
    easing = FastOutSlowInEasing
)
