package tech.zhifu.app.myhub.feature.preview.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.preview.PreviewTransitionTokens

@Composable
fun PreviewAnimatedVisibility(
    modifier: Modifier,
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_ENTER_DURATION_MS,
                easing = FastOutSlowInEasing,
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_ENTER_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
            initialScale = PreviewTransitionTokens.HOST_SCALE_FROM,
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_EXIT_DURATION_MS,
                easing = FastOutSlowInEasing,
            )
        ) + scaleOut(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_EXIT_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
            targetScale = PreviewTransitionTokens.HOST_SCALE_TO,
        ),
        modifier = modifier,
        content = content,
    )
}
