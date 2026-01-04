package tech.zhifu.app.myhub.datastore.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

/**
 * WASM 平台的 Ktor Client 工厂实现
 * 
 * WASM 平台使用与 JS 相同的 Js 引擎，因为 Ktor 的 JS 引擎在 WASM 环境下也能正常工作。
 * 这是通过 Kotlin/Wasm 的 JS interop 机制实现的。
 */
actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Js.create()
    }
}

