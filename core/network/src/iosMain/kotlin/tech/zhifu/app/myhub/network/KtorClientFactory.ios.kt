package tech.zhifu.app.myhub.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Darwin.create()
    }
}

