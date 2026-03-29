package tech.zhifu.app.myhub.ui.design.util

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("LocalSharedTransitionScope is not provided. Wrap content in SharedTransitionLayout.")
}

val LocalSnackbarState = compositionLocalOf<SnackbarHostState> {
    error("LocalSnabackbarState is not provided.")
}
