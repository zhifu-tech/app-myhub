package tech.zhifu.app.myhub.feature.ai.content.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.max

@Composable
internal fun rememberStickyInputHeight(): StickyInputHeightState {
    return remember { StickyInputHeightState() }
}

internal class StickyInputHeightState {
    var targetHeightPx by mutableStateOf(0)
    var maxHeightPx by mutableStateOf(0)

    fun update(newHeightPx: Int) {
        targetHeightPx = newHeightPx
        if (newHeightPx > maxHeightPx) {
            maxHeightPx = newHeightPx
        }
    }

    fun reset() {
        maxHeightPx = targetHeightPx
    }

    fun getDisplayHeightPx(): Int {
        return max(targetHeightPx, maxHeightPx)
    }
}
