package tech.zhifu.app.myhub.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween

/**
 * Motion tokens aligned to Material 3 defaults where possible.
 * Use these tokens in components to keep motion consistent and reusable.
 */
object AppMotionTokens {
    // Material-style easing curves
    val StandardEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val StandardDecelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
    // Tailwind "ease-out": cubic-bezier(0, 0, 0.2, 1)
    val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    // Base durations (ms)
    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationLong = 700
    const val DurationPulse = 2200

    data class MotionSpec(
        val durationMillis: Int,
        val easing: Easing,
    )

    // Reusable specs
    val Hover: MotionSpec = MotionSpec(DurationShort, StandardEasing)
    val FocusTransition: MotionSpec = MotionSpec(DurationMedium, StandardEasing)
    val FocusUnderlineExpand: MotionSpec = MotionSpec(DurationLong, StandardDecelerate)

    // Underline focus/blur aligned to design code.html
    val UnderlineExpand: MotionSpec = MotionSpec(700, EaseOut)
    val UnderlineCollapse: MotionSpec = MotionSpec(700, EaseOut)

    // Idle placeholder pulse (Ready state only)
    val PlaceholderPulse: MotionSpec = MotionSpec(DurationPulse, StandardEasing)
}

fun <T> motionTween(spec: AppMotionTokens.MotionSpec) = tween<T>(
    durationMillis = spec.durationMillis,
    easing = spec.easing,
)
