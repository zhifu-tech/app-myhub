package tech.zhifu.app.myhub.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEvent.Companion.EDGE_LEFT

private const val IOS_PAGE_PUSH_DURATION_MS = 350
private const val IOS_PAGE_POP_DURATION_MS = 300

private val IOS_PAGE_PUSH_EASING = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
private val IOS_PAGE_POP_EASING = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)


// ===============================
// 👉 Push（前进）完整版
// ===============================
@Composable
fun rememberPageForwardTransitionSpec():
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    remember {
        {
            ContentTransform(
                // 👉 新页面进入 → spring（主角有重量）
                targetContentEnter =
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = spring(
                            dampingRatio = 0.92f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(
                        initialAlpha = 0.85f,
                        animationSpec = tween(
                            durationMillis = IOS_PAGE_PUSH_DURATION_MS,
                            easing = IOS_PAGE_PUSH_EASING
                        )
                    ) + scaleIn(
                        initialScale = 0.98f, // 微弱放大
                        animationSpec = tween(
                            durationMillis = IOS_PAGE_PUSH_DURATION_MS,
                            easing = IOS_PAGE_PUSH_EASING
                        )
                    ),

                // 👉 旧页面（背景）
                initialContentExit = slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    targetOffset = { it / 3 },
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_PUSH_DURATION_MS,
                        easing = IOS_PAGE_PUSH_EASING
                    )
                ) + fadeOut(
                    targetAlpha = 0.9f,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_PUSH_DURATION_MS,
                        easing = IOS_PAGE_PUSH_EASING
                    )
                ) + scaleOut(
                    targetScale = 0.98f, // 被压缩感（iOS关键）
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_PUSH_DURATION_MS,
                        easing = IOS_PAGE_PUSH_EASING
                    )
                )
            )
        }
    }


// ===============================
// 👉 Pop（返回）完整版
// ===============================
@Composable
fun rememberPagePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    remember {
        {
            ContentTransform(
                // 👉 下层页面
                targetContentEnter = slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    initialOffset = { it / 3 },
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                ) + fadeIn(
                    initialAlpha = 0.9f,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                ) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                ),

                // 👉 当前页面（离开）
                initialContentExit = slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                ) + fadeOut(
                    targetAlpha = 0.85f,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                ) + scaleOut(
                    targetScale = 0.98f,
                    animationSpec = tween(
                        durationMillis = IOS_PAGE_POP_DURATION_MS,
                        easing = IOS_PAGE_POP_EASING
                    )
                )
            )
        }
    }


// ===============================
// 👉 Predictive Back（手势跟随）完整版
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

                // 👉 下层页面（跟手）
                targetContentEnter =
                    slideIntoContainer(
                        towards = direction,
                        initialOffset = { it / 3 },
                        animationSpec = tween(
                            IOS_PAGE_POP_DURATION_MS,
                            easing = LinearEasing
                        )
                    ) +
                        fadeIn(
                            initialAlpha = 0.9f,
                            animationSpec = tween(
                                IOS_PAGE_POP_DURATION_MS,
                                easing = LinearEasing
                            )
                        ) +
                        scaleIn(
                            initialScale = 0.98f,
                            animationSpec = tween(
                                IOS_PAGE_POP_DURATION_MS,
                                easing = LinearEasing
                            )
                        ),

                // 👉 当前页面（跟手退出）
                initialContentExit =
                    slideOutOfContainer(
                        towards = direction,
                        animationSpec = tween(
                            IOS_PAGE_POP_DURATION_MS,
                            easing = LinearEasing
                        )
                    ) +
                        fadeOut(
                            targetAlpha = 0.85f,
                            animationSpec = tween(
                                IOS_PAGE_POP_DURATION_MS,
                                easing = LinearEasing
                            )
                        ) +
                        scaleOut(
                            targetScale = 0.98f,
                            animationSpec = tween(
                                IOS_PAGE_POP_DURATION_MS,
                                easing = LinearEasing
                            )
                        )
            )
        }
    }
