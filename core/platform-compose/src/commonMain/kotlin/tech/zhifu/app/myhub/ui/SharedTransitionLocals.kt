package tech.zhifu.app.myhub.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("LocalSharedTransitionScope is not provided. Wrap content in SharedTransitionLayout.")
}

val LocalSceneAnimatedContentScope = compositionLocalOf<AnimatedContentScope> {
    error("LocalSceneAnimatedContentScope is not provided. Wrap content in SceneAnimatedContent.")
}
