package tech.zhifu.app.myhub

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import tech.zhifu.app.myhub.resources.Res
import tech.zhifu.app.myhub.resources.app_logo

fun main() = application {
    AppBootstrap.start()

    val windowState = remember {
        WindowState(size = DpSize(width = 840.dp, height = 844.dp))
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "MyHub",
        state = windowState,
        icon = painterResource(Res.drawable.app_logo)
    ) {
        App()
    }
}
