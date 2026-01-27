package tech.zhifu.app.myhub.auth

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.exception.UnauthorizedException

/**
 * 配置认证插件（使用 JWT）
 */
fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-bearer") {
            realm = "MyHub API"
            verifier(
                com.auth0.jwt.JWT
                    .require(Algorithm.HMAC256(JwtConfig.getSecret()))
                    .withIssuer(JwtConfig.getIssuer())
                    .withAudience(JwtConfig.getAudience())
                    .build()
            )
            validate { credential ->
                // 验证 token 类型（只接受 access token）
                val type = credential.payload.getClaim("type").asString()
                if (type != "access") {
                    return@validate null
                }

                // 提取用户信息
                val userId = credential.payload.getClaim("userId").asString()
                val username = credential.payload.getClaim("username").asString()

                if (userId != null) {
                    // 返回 JWTPrincipal，包含 JWT payload
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

/**
 * 扩展函数：从请求中获取当前用户 ID（从 JWT token 中提取）
 */
suspend fun ApplicationCall.getCurrentUserId(): String {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Authentication required")

    val userId = principal.payload.getClaim("userId").asString()
        ?: throw UnauthorizedException("Invalid token: missing userId")

    return userId
}

/**
 * 扩展函数：从请求中获取当前用户（从数据库查询）
 */
suspend fun ApplicationCall.getCurrentUser(
    userRepository: tech.zhifu.app.myhub.datastore.repository.UserRepository
): User {
    val userId = getCurrentUserId()
    return userRepository.getUserById(userId)
        ?: throw UnauthorizedException("User not found")
}
