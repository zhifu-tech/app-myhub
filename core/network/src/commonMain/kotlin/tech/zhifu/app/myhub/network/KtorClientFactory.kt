package tech.zhifu.app.myhub.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.network.auth.TokenRefreshProvider
import tech.zhifu.app.myhub.network.auth.TokenStorage

/**
 * 创建配置好的HttpClient（带认证版本）
 */
fun createHttpClient(
    tokenStorage: TokenStorage? = null,
    tokenRefreshProvider: TokenRefreshProvider? = null
): HttpClient = HttpClient {
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

    // 安装 Auth 插件（如果提供了 tokenStorage 和 tokenRefreshProvider）
    if (tokenStorage != null && tokenRefreshProvider != null) {
        install(Auth) {
            bearer {
                loadTokens {
                    // 从本地存储加载 token
                    tokenStorage.getBearerTokens()
                }

                refreshTokens {
                    // 401 错误时自动调用
                    val refreshToken = oldTokens?.refreshToken
                        ?: return@refreshTokens null

                    try {
                        // 刷新 token（刷新 API 本身不需要认证，由 sendWithoutRequest 控制）
                        val newAccessToken = tokenRefreshProvider.refreshToken(refreshToken)
                        val newTokens = BearerTokens(
                            accessToken = newAccessToken,
                            refreshToken = refreshToken // refresh token 不变（服务器端不返回新的 refreshToken）
                        )
                        // 保存新 token
                        tokenStorage.saveTokens(
                            accessToken = newAccessToken,
                            refreshToken = refreshToken
                        )
                        newTokens
                    } catch (e: Exception) {
                        // Refresh 失败：按 message 判断是否需清除 token（与 exception.ApiException 等兼容）
                        val isAuthFailure = e.message?.contains("Unauthorized", ignoreCase = true) == true ||
                            e.message?.contains("expired", ignoreCase = true) == true ||
                            e.message?.contains("invalid", ignoreCase = true) == true
                        if (isAuthFailure) tokenStorage.clearTokens()
                        if (e !is NetworkException) logger.error(e) { "Token refresh failed" }
                        null
                    }
                }

                // 方案 B：只对业务 API 请求发送认证，刷新 API 不发送认证
                // 这样可以避免循环依赖：刷新 token 的请求本身不需要认证
                sendWithoutRequest { request ->
                    val path = request.url.encodedPath
                    path.startsWith("/api/") && !path.startsWith("/api/auth/")
                }
            }
        }
    }
}

