package tech.zhifu.app.myhub.datastore.network

import io.ktor.client.engine.android.Android
import io.ktor.client.engine.HttpClientEngine

actual class KtorClientFactory {
    actual fun createEngine(): HttpClientEngine {
        return Android.create()
    }
}

