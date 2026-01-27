package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.auth.TokenService
import tech.zhifu.app.myhub.datastore.model.dto.ErrorDetail
import tech.zhifu.app.myhub.datastore.model.dto.ErrorResponse
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.exception.UnauthorizedException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.service.UserService
import kotlin.time.Clock

/**
 * 认证 API 路由
 *
 * POST /api/auth/login - 登录并获取 token（支持自动注册）
 * POST /api/auth/refresh - 刷新 access token
 */
fun Route.authApi(
    tokenService: TokenService,
    userService: UserService,
    userRepository: UserRepository
) {
    route("/api/auth") {
        // POST /api/auth/login - 登录（支持自动注册）
        post("login") {
            try {
                val request = call.receive<LoginRequest>()

                // 验证请求
                require(request.userId.isNotBlank()) {
                    "User ID is required"
                }

                // 获取或创建用户（自动注册）
                // 如果客户端提供了用户信息，使用客户端数据（以客户端为准）
                val user = userService.createUserIfNotExists(
                    userId = request.userId,
                    username = request.username,
                    displayName = request.displayName,
                    avatarUrl = request.avatarUrl,
                    avatarText = request.avatarText
                )

                // 更新最后登录时间（在事务中确保原子性）
                val updatedUser = user.copy(
                    lastLoginAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )
                userRepository.upsertUser(updatedUser)

                // 生成 token pair（access token + refresh token）
                val tokenPair = tokenService.generateTokens(updatedUser)

                call.respond(
                    HttpStatusCode.OK,
                    LoginResponse(
                        accessToken = tokenPair.accessToken,
                        refreshToken = tokenPair.refreshToken,
                        expiresIn = tokenPair.expiresIn,
                        tokenType = "Bearer",
                        userId = updatedUser.id,
                        username = updatedUser.username ?: ""
                    )
                )
            } catch (e: ValidationException) {
                // 验证错误，返回 400
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = ErrorDetail(
                            code = "VALIDATION_ERROR",
                            message = e.message ?: "Validation failed",
                            path = call.request.path()
                        )
                    )
                )
            } catch (e: IllegalArgumentException) {
                // 参数错误，返回 400
                logger.warn(e) { "Invalid login request: ${e.message}" }
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = ErrorDetail(
                            code = "INVALID_REQUEST",
                            message = e.message ?: "Invalid request",
                            path = call.request.path()
                        )
                    )
                )
            } catch (e: IllegalStateException) {
                // 状态错误（如并发创建后的状态不一致），返回 409
                logger.warn(e) { "User creation state error: ${e.message}" }
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(
                        error = ErrorDetail(
                            code = "CONFLICT",
                            message = "User creation conflict",
                            path = call.request.path()
                        )
                    )
                )
            } catch (e: Exception) {
                // 其他错误，记录详细日志，返回通用错误（避免信息泄露）
                logger.error(e) { "Login failed: ${e.message}" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = ErrorDetail(
                            code = "INTERNAL_ERROR",
                            message = "Login failed",
                            path = call.request.path()
                        )
                    )
                )
            }
        }

        // POST /api/auth/refresh - 刷新 access token
        post("refresh") {
            try {
                val request = call.receive<RefreshTokenRequest>()

                // 验证请求
                require(request.refreshToken.isNotBlank()) {
                    "Refresh token is required"
                }

                // 使用 refresh token 刷新 access token
                val tokenPair = tokenService.refreshAccessToken(request.refreshToken)

                call.respond(
                    HttpStatusCode.OK,
                    RefreshTokenResponse(
                        accessToken = tokenPair.accessToken,
                        expiresIn = tokenPair.expiresIn,
                        tokenType = "Bearer"
                    )
                )
            } catch (e: UnauthorizedException) {
                throw e
            } catch (e: ValidationException) {
                throw e
            } catch (e: Exception) {
                throw UnauthorizedException("Token refresh failed: ${e.message}")
            }
        }
    }
}

/**
 * 登录请求
 * 支持客户端提供用户信息（本地匿名用户已有）
 */
@Serializable
data class LoginRequest(
    val userId: String,
    val username: String? = null, // 可选：客户端提供的用户名
    val displayName: String? = null, // 可选：客户端提供的显示名称
    val avatarUrl: String? = null, // 可选：客户端提供的头像URL
    val avatarText: String? = null // 可选：客户端提供的头像文本
)

/**
 * 登录响应
 */
@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long, // 秒
    val tokenType: String,
    val userId: String,
    val username: String
)

/**
 * 刷新 Token 请求
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * 刷新 Token 响应
 */
@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Long, // 秒
    val tokenType: String
)
