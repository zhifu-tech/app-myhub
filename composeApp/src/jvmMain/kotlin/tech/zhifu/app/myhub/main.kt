package tech.zhifu.app.myhub

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    AppBootstrap.start()

    val windowState = remember {
//        WindowState(size = DpSize(width = 390.dp, height = 844.dp))
        WindowState(size = DpSize(width = 840.dp, height = 844.dp))
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "MyHub",
        state = windowState,
//        icon = painterResource(Res.drawable)
    ) {
        // 传递当前窗口大小，以便 App 内部能正确计算 WindowSizeClass
        App()
    }
}
