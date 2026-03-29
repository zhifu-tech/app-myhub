package tech.zhifu.app.myhub

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    AppBootstrap.start()

    onWasmReady {
        val body = document.body ?: return@onWasmReady
        ComposeViewport(body) {
            App()
        }
    }
}
