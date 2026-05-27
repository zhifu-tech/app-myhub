package tech.zhifu.app.myhub.auth

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.exception.UnauthorizedException
import java.util.Date

/**
 * Token 服务
 * 负责 JWT token 的生成、验证和管理
 */
class TokenService(
    private val userRepository: UserRepository
) {
    /**
     * 验证 access token 并返回用户信息
     *
     * @param token Access Token
     * @return 用户信息，如果 token 无效或已过期则返回 null
     */
    suspend fun validateAccessToken(token: String): User? {
        val claims = JwtConfig.validateToken(token) ?: return null

        // 验证 token 类型
        if (claims.type != "access") {
            return null
        }

        // 检查是否过期
        if (claims.expiresAt.before(Date())) {
            return null
        }

        // 从数据库获取用户
        return userRepository.getUserById(claims.userId)
    }

    /**
     * 验证 refresh token 并返回用户信息
     *
     * @param token Refresh Token
     * @return 用户信息，如果 token 无效或已过期则返回 null
     */
    suspend fun validateRefreshToken(token: String): User? {
        val claims = JwtConfig.validateToken(token) ?: return null

        // 验证 token 类型
        if (claims.type != "refresh") {
            return null
        }

        // 检查是否过期
        if (claims.expiresAt.before(Date())) {
            return null
        }

        // 从数据库获取用户
        return userRepository.getUserById(claims.userId)
    }

    /**
     * 生成 Access Token 和 Refresh Token
     *
     * @param user 用户信息
     * @return TokenPair（包含 access token 和 refresh token）
     */
    suspend fun generateTokens(user: User): TokenPair {
        val accessToken = JwtConfig.generateAccessToken(
            userId = user.id,
            username = user.username
        )

        val refreshToken = JwtConfig.generateRefreshToken(
            userId = user.id
        )

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = JwtConfig.ACCESS_TOKEN_EXPIRATION_MS / 1000 // 秒
        )
    }

    /**
     * 使用 refresh token 刷新 access token
     *
     * @param refreshToken Refresh Token
     * @return 新的 TokenPair
     */
    suspend fun refreshAccessToken(refreshToken: String): TokenPair {
        val user = validateRefreshToken(refreshToken)
            ?: throw UnauthorizedException("Invalid or expired refresh token")

        return generateTokens(user)
    }
}

/**
 * Token 对（Access Token + Refresh Token）
 */
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long // 秒
)
