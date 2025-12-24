package tech.zhifu.app.myhub.datastore.network

import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.HttpClientEngine

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return CIO.create()
    }
}

