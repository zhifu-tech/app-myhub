package tech.zhifu.app.myhub.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * 滑动返回手势阈值
 */
private const val SWIPE_BACK_THRESHOLD_DP = 100f // 滑动超过 100dp 时触发返回
private const val EDGE_THRESHOLD_DP = 20f // 左边缘 20dp 范围内开始滑动才有效
private const val VERTICAL_THRESHOLD_DP = 50f // 垂直方向偏移超过 50dp 时取消手势

/**
 * 滑动返回手势 Modifier
 * 
 * 实现从屏幕左边缘向右滑动返回上一页的功能
 * 这是 app 内统一的返回交互方式
 * 
 * @param onSwipeBack 滑动返回回调
 * @param enabled 是否启用滑动返回（默认 true，桌面端可禁用）
 */
@Composable
fun Modifier.swipeBackGesture(
    onSwipeBack: () -> Unit,
    enabled: Boolean = true
): Modifier {
    val density = LocalDensity.current
    
    if (!enabled) return this

    val swipeThreshold = remember(density) { with(density) { SWIPE_BACK_THRESHOLD_DP.dp.toPx() } }
    val edgeThreshold = remember(density) { with(density) { EDGE_THRESHOLD_DP.dp.toPx() } }
    val verticalThreshold = remember(density) { with(density) { VERTICAL_THRESHOLD_DP.dp.toPx() } }

    return this.pointerInput(swipeThreshold, edgeThreshold, verticalThreshold) {
        var dragStart: Offset? = null
        var isDragging = false
        var lastPosition: Offset? = null

        detectDragGestures(
            onDragStart = { offset ->
                // 检查是否从屏幕左边缘开始
                if (offset.x <= edgeThreshold) {
                    dragStart = offset
                    lastPosition = offset
                    isDragging = true
                }
            },
            onDragEnd = {
                if (isDragging && dragStart != null && lastPosition != null) {
                    val dragDistance = lastPosition!!.x - dragStart!!.x
                    val verticalDistance = abs(lastPosition!!.y - dragStart!!.y)
                    
                    // 如果水平滑动距离超过阈值，且垂直偏移在允许范围内，触发返回
                    if (dragDistance > swipeThreshold && verticalDistance < verticalThreshold) {
                        onSwipeBack()
                    }
                }
                dragStart = null
                lastPosition = null
                isDragging = false
            },
            onDragCancel = {
                dragStart = null
                lastPosition = null
                isDragging = false
            }
        ) { change, dragAmount ->
            if (isDragging && dragStart != null) {
                lastPosition = change.position
                val verticalDistance = abs(change.position.y - dragStart!!.y)
                
                // 如果垂直偏移过大，取消手势
                if (verticalDistance > verticalThreshold) {
                    dragStart = null
                    lastPosition = null
                    isDragging = false
                }
            }
        }
    }
}
