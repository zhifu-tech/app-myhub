package tech.zhifu.app.myhub.datastore.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Js.create()
    }
}


