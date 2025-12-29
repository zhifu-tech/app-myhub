package tech.zhifu.app.myhub.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.w3c.dom.events.Event

/**
 * JS 平台实现：实时监听浏览器窗口大小变化
 */
@Composable
actual fun getWindowSize(): DpSize {
    val density = LocalDensity.current

    // 使用 callbackFlow 监听窗口 resize 事件
    val windowSizeFlow = remember {
        callbackFlow {
            val updateSize = {
                val widthDp = (window.innerWidth / density.density).dp
                val heightDp = (window.innerHeight / density.density).dp
                trySend(DpSize(widthDp, heightDp))
                Unit // 确保返回 Unit
            }

            // 明确指定 Event 类型并确保返回 Unit
            val listener = { _: Event -> updateSize() }
            window.addEventListener("resize", listener)

            // 发送初始大小
            updateSize()

            awaitClose {
                window.removeEventListener("resize", listener)
            }
        }
    }

    val size by windowSizeFlow.collectAsState(
        initial = DpSize(
            (window.innerWidth / density.density).dp,
            (window.innerHeight / density.density).dp
        )
    )

    return size
}
