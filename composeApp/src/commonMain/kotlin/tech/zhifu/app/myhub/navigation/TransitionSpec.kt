package tech.zhifu.app.myhub.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEvent.Companion.EDGE_LEFT

private const val IOS_PAGE_PUSH_DURATION_MS = 350
private const val IOS_PAGE_POP_DURATION_MS = 300

// iOS push exit 曲线
private val IOS_PAGE_PUSH_EASING = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

// iOS pop 入/出曲线
private val IOS_PAGE_POP_EASING = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

// ===============================
// 👉 Push（前进）
// ===============================
@Composable
fun rememberPageForwardTransitionSpec():
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    remember {
        {
            ContentTransform(
                // 新页面进入 → spring（主角有重量）
                targetContentEnter = slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = spring(
                        dampingRatio = 0.92f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                // 旧页面退出 → tween + easeOut（轻滑）
                initialContentExit = slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    targetOffset = { it / 3 }, // iOS 更明显视差
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_PUSH_DURATION_MS,
                        easing = IOS_PAGE_PUSH_EASING
                    )
                )
            )
        }
    }

// ===============================
// 👉 Pop（返回）
// ===============================
@Composable
fun rememberPagePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    remember {
        {
            ContentTransform(
                // 下层页面进入 → tween + easeInOut
                targetContentEnter = slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    initialOffset = { it / 3 },
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                ),
                // 当前页面退出 → tween + easeInOut
                initialContentExit = slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                )
            )
        }
    }

// ===============================
// 👉 Predictive Back（手势跟随）
// ===============================
@Composable
fun rememberPagePredictivePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<NavKey>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform =
    remember {
        { edge ->
            val direction = if (edge == EDGE_LEFT) {
                AnimatedContentTransitionScope.SlideDirection.Right
            } else {
                AnimatedContentTransitionScope.SlideDirection.Left
            }
            ContentTransform(
                // 手势拖动阶段 → LinearEasing 完全跟手
                targetContentEnter = slideIntoContainer(
                    towards = direction,
                    initialOffset = { it / 3 },
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = LinearEasing
                    )
                ),
                initialContentExit = slideOutOfContainer(
                    towards = direction,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = LinearEasing
                    ),
                ),
            )
        }
    }

