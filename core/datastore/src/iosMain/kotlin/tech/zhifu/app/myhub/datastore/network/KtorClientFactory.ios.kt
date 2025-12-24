package tech.zhifu.app.myhub.datastore.network

import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.HttpClientEngine

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Darwin.create()
    }
}

