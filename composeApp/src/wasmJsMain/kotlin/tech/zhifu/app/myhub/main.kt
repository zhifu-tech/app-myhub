package tech.zhifu.app.myhub

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import tech.zhifu.app.myhub.di.initKoin
import tech.zhifu.app.myhub.startup.StartupOrchestrator

fun main() {
    // 初始化 Koin 依赖注入
    val koinApplication = initKoin()
    koinApplication.koin.get<StartupOrchestrator>().start()

    onWasmReady {
        val body = document.body ?: return@onWasmReady
        ComposeViewport(body) {
            App()
        }
    }
}
