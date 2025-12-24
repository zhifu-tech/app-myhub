package tech.zhifu.app.myhub

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import tech.zhifu.app.myhub.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // 初始化 Koin 依赖注入
    initKoin()

    onWasmReady {
        val body = document.body ?: return@onWasmReady
        ComposeViewport(body) {
            App()
        }
    }
}
