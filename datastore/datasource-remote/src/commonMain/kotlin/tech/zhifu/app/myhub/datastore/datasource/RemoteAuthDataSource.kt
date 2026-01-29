package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.dto.LoginResponse
import tech.zhifu.app.myhub.datastore.model.dto.RefreshTokenResponse

/**
 * 认证数据源接口
 * 处理登录和 Token 刷新
 */
interface RemoteAuthDataSource {
    /**
     * 登录并获取 Token
     * @param userId 用户ID（本地生成的匿名用户ID）
     * @param username 可选：用户名
     * @param displayName 可选：显示名称
     * @param avatarUrl 可选：头像URL
     * @param avatarText 可选：头像文本
     * @return LoginResponse 包含 accessToken 和 refreshToken
     */
    suspend fun login(
        userId: String,
        username: String? = null,
        displayName: String? = null,
        avatarUrl: String? = null,
        avatarText: String? = null
    ): LoginResponse

    /**
     * 刷新 Access Token
     * @param refreshToken Refresh Token
     * @return RefreshTokenResponse 包含新的 accessToken
     */
    suspend fun refreshToken(refreshToken: String): RefreshTokenResponse
}
