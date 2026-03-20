package tech.zhifu.app.myhub.ui

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    staticCompositionLocalOf {
        error("LocalSharedTransitionScope is not provided. Wrap content in SharedTransitionLayout.")
    }
