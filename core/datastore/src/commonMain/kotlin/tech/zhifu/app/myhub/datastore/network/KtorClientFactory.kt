package tech.zhifu.app.myhub.datastore.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor Client工厂
 * 各平台需要提供具体的引擎实现
 */
expect class KtorClientFactory() {
    fun createEngine(): HttpClientEngine
}

/**
 * 创建配置好的HttpClient
 */
fun createHttpClient(factory: KtorClientFactory): HttpClient {
    return HttpClient(factory.createEngine()) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }

        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }

        // 日志
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}
