package tech.zhifu.app.myhub.network.auth

/**
 * Token 刷新提供者接口
 * 用于在 core/network 模块中抽象 Token 刷新功能，避免依赖数据层
 */
interface TokenRefreshProvider {
    /**
     * 刷新 Access Token
     * @param refreshToken Refresh Token
     * @return 新的 Access Token
     */
    suspend fun refreshToken(refreshToken: String): String
}
