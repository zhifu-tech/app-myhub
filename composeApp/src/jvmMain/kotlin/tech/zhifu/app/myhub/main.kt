package tech.zhifu.app.myhub

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import tech.zhifu.app.myhub.di.initKoin

fun main() = application {
    // 初始化 Koin 依赖注入
    initKoin()

    val windowState = remember { WindowState(size = DpSize(1280.dp, 800.dp)) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "书斋 - Study Room",
        state = windowState
    ) {
        // 传递当前窗口大小，以便 App 内部能正确计算 WindowSizeClass
        App(windowSize = windowState.size)
    }
}
