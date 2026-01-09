package tech.zhifu.app.myhub.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/**
 * 屏幕转场动画配置
 * 
 * 参考 Apple Music 的转场动画设计：
 * - 进入：缩放 + 淡入（从卡片位置放大到全屏）
 * - 退出：缩放 + 淡出（缩小并淡出）
 * - 使用平滑的缓动曲线
 * 
 * 注意：这些函数不是 @Composable，因为它们会在 transitionSpec lambda 中被调用
 * transitionSpec 不是 @Composable 上下文
 */
object ScreenTransition {
    /**
     * 卡片详情页进入动画
     * 模拟 Apple Music 的卡片展开效果
     * 从当前 card 缩放到内容区的平滑过渡
     */
    fun cardDetailEnterTransition() = fadeIn(
        animationSpec = tween(450, easing = FastOutSlowInEasing)
    ) + scaleIn(
        initialScale = 0.85f,  // 从更小的缩放开始，模拟从卡片位置展开
        animationSpec = tween(450, easing = FastOutSlowInEasing)
    )

    /**
     * 卡片详情页退出动画
     */
    fun cardDetailExitTransition() = fadeOut(
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )

    /**
     * Dashboard 进入动画（返回时）
     */
    fun dashboardEnterTransition() = fadeIn(
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    ) + slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )

    /**
     * Dashboard 退出动画（导航到详情页时）
     */
    fun dashboardExitTransition() = fadeOut(
        animationSpec = tween(450, easing = FastOutSlowInEasing)
    ) + slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(450, easing = FastOutSlowInEasing)
    )

    /**
     * 默认转场动画（用于其他屏幕）
     */
    fun defaultEnterTransition() = fadeIn(
        animationSpec = tween(300)
    ) + slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300)
    )

    fun defaultExitTransition() = fadeOut(
        animationSpec = tween(300)
    ) + slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(300)
    )
}
