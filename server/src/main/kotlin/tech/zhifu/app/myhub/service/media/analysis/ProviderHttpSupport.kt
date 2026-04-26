package tech.zhifu.app.myhub.service.media.analysis

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.math.max

internal object ProviderHttpSupport {
    fun postJson(
        url: String,
        body: String,
        timeoutMs: Long,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<String> {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(timeoutMs))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
        headers.forEach { (k, v) -> requestBuilder.header(k, v) }
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .build()
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
    }

    fun getBytes(
        url: String,
        timeoutMs: Long,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse<ByteArray> {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(timeoutMs))
            .GET()
        headers.forEach { (k, v) -> requestBuilder.header(k, v) }
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .build()
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray())
    }

    inline fun <T> withRetry(
        maxAttempts: Int,
        initialBackoffMs: Long,
        block: (attempt: Int) -> T
    ): T {
        var lastError: Throwable? = null
        var backoff = max(100L, initialBackoffMs)
        val attempts = max(1, maxAttempts + 1)
        repeat(attempts) { idx ->
            val attempt = idx + 1
            try {
                return block(attempt)
            } catch (e: Throwable) {
                lastError = e
                if (attempt < attempts) {
                    Thread.sleep(backoff)
                    backoff = (backoff * 2).coerceAtMost(4_000L)
                }
            }
        }
        throw IllegalStateException(
            "provider call failed after retry attempts=$attempts lastError=${lastError?.javaClass?.simpleName}: ${lastError?.message}",
            lastError
        )
    }
}
