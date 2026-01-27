package tech.zhifu.app.myhub.network.auth

import io.ktor.client.plugins.auth.providers.BearerTokens

/**
 * Token 存储接口
 * 用于存储和管理 JWT Access Token 和 Refresh Token
 */
interface TokenStorage {
    /**
     * 获取 Access Token
     */
    suspend fun getAccessToken(): String?

    /**
     * 获取 Refresh Token
     */
    suspend fun getRefreshToken(): String?

    /**
     * 保存 Token 对
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /**
     * 清除所有 Token
     */
    suspend fun clearTokens()

    /**
     * 获取 BearerTokens（同步方法，用于 Ktor Auth 插件）
     */
    fun getBearerTokens(): BearerTokens?
}
