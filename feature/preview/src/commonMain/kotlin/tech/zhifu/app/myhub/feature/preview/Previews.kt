package tech.zhifu.app.myhub.feature.preview

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SharedTransitionScope.sharedWith(
    key: String,
    visible: Boolean
): Modifier = Modifier.sharedElementWithCallerManagedVisibility(
    sharedContentState = rememberSharedContentState(key = key),
    visible = visible,
    boundsTransform = { _, _ ->
        tween(
            durationMillis = PreviewState.SHARED_BOUNDS_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    },
)

@Composable
fun animationSpec(): TweenSpec<Float> = tween(
    durationMillis = PreviewState.SHARED_BOUNDS_DURATION_MS,
    easing = FastOutSlowInEasing
)
