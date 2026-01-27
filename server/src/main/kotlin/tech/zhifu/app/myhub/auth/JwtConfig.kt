package tech.zhifu.app.myhub.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

/**
 * JWT 配置
 * 从环境变量读取配置，如果未设置则使用默认值
 */
object JwtConfig {
    // 从环境变量读取，如果未设置则使用默认值
    private val SECRET: String = System.getenv("JWT_SECRET")
        ?: "myhub-secret-key-change-in-production-please-set-jwt-secret-in-env"

    private val ISSUER: String = System.getenv("JWT_ISSUER") ?: "myhub-api"

    private val AUDIENCE: String = System.getenv("JWT_AUDIENCE") ?: "myhub-client"

    // Access Token 过期时间：15 分钟
    const val ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000L

    // Refresh Token 过期时间：7 天
    const val REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L

    /**
     * 创建 JWT Verifier
     */
    fun createVerifier(): JWTVerifier {
        return JWT
            .require(Algorithm.HMAC256(SECRET))
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .build()
    }

    /**
     * 获取 Secret Key
     */
    fun getSecret(): String = SECRET

    /**
     * 获取 Issuer
     */
    fun getIssuer(): String = ISSUER

    /**
     * 获取 Audience
     */
    fun getAudience(): String = AUDIENCE

    /**
     * 生成 Access Token
     */
    fun generateAccessToken(userId: String, username: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("type", "access")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + ACCESS_TOKEN_EXPIRATION_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }

    /**
     * 生成 Refresh Token
     */
    fun generateRefreshToken(userId: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + REFRESH_TOKEN_EXPIRATION_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }

    /**
     * 验证 token 并提取用户信息
     */
    fun validateToken(token: String): TokenClaims? {
        return try {
            val verifier = createVerifier()
            val decoded = verifier.verify(token)

            val userId = decoded.getClaim("userId").asString()
            val username = decoded.getClaim("username").asString()
            val type = decoded.getClaim("type").asString()
            val expiresAt = decoded.expiresAt

            if (userId == null || expiresAt == null || expiresAt.before(Date())) {
                return null
            }

            TokenClaims(
                userId = userId,
                username = username,
                type = type,
                expiresAt = expiresAt
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Token Claims（JWT Payload 中的声明）
 */
data class TokenClaims(
    val userId: String,
    val username: String?,
    val type: String?,
    val expiresAt: Date
)
