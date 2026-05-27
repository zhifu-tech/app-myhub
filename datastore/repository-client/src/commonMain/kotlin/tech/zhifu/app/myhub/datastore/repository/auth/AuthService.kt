package tech.zhifu.app.myhub.datastore.repository.auth

import tech.zhifu.app.myhub.datastore.datasource.auth.RemoteAuthDataSource
import tech.zhifu.app.myhub.datastore.model.dto.LoginResponse
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.network.auth.TokenStorage

/**
 * 认证服务
 * 处理登录、登出和 Token 管理
 */
class AuthService(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository,
    private val logger: tech.zhifu.app.myhub.logger.Logger = logger("AuthService")
) {
    /**
     * 登录
     * @param userId 用户ID（本地生成的匿名用户ID）
     * @param username 可选：用户名
     * @param displayName 可选：显示名称
     * @param avatarUrl 可选：头像URL
     * @param avatarText 可选：头像文本
     * @return LoginResponse 包含 token 和用户信息
     */
    suspend fun login(
        userId: String,
        username: String? = null,
        displayName: String? = null,
        avatarUrl: String? = null,
        avatarText: String? = null
    ): LoginResponse {
        logger.info { "Attempting login for user: $userId" }
        
        return try {
            // 调用登录 API
            val response = remoteAuthDataSource.login(
                userId = userId,
                username = username,
                displayName = displayName,
                avatarUrl = avatarUrl,
                avatarText = avatarText
            )
            
            // 保存 token
            tokenStorage.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
            
            logger.info { "Login successful for user: ${response.userId}" }
            response
        } catch (e: Exception) {
            logger.error(e) { "Login failed for user: $userId" }
            throw e
        }
    }

    /**
     * 登出
     * 清除本地存储的 token
     */
    suspend fun logout() {
        logger.info { "Logging out user" }
        tokenStorage.clearTokens()
        logger.info { "Logout completed" }
    }

    /**
     * 检查是否已登录
     * @return true 如果存在有效的 token
     */
    suspend fun isLoggedIn(): Boolean {
        val accessToken = tokenStorage.getAccessToken()
        return accessToken != null
    }

    /**
     * 获取当前用户的 Access Token
     * @return Access Token，如果未登录则返回 null
     */
    suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }
}
