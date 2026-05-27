package tech.zhifu.app.myhub.api.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.datastore.model.dto.ErrorDetail
import tech.zhifu.app.myhub.datastore.model.dto.ErrorResponse
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.exception.UnauthorizedException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.service.UserService
import tech.zhifu.app.myhub.auth.TokenService
import kotlin.time.Clock

/**
 * 认证 API 路由（领域：auth）
 *
 * POST /api/auth/login - 登录并获取 token（支持自动注册）
 * POST /api/auth/refresh - 刷新 access token
 */
fun Route.authApi() {
    route("/api/auth") {
        post("login") {
            val tokenService = call.application.get<TokenService>()
            val userService = call.application.get<UserService>()
            val userRepository = call.application.get<UserRepository>()
            try {
                val request = call.receive<LoginRequest>()
                require(request.userId.isNotBlank()) { "User ID is required" }
                val user = userService.createUserIfNotExists(
                    userId = request.userId,
                    username = request.username,
                    displayName = request.displayName,
                    avatarUrl = request.avatarUrl,
                    avatarText = request.avatarText
                )
                val updatedUser = user.copy(
                    lastLoginAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )
                userRepository.upsertUser(updatedUser)
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

        post("refresh") {
            val tokenService = call.application.get<TokenService>()
            try {
                val request = call.receive<RefreshTokenRequest>()
                require(request.refreshToken.isNotBlank()) { "Refresh token is required" }
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

@Serializable
data class LoginRequest(
    val userId: String,
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val avatarText: String? = null
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
    val userId: String,
    val username: String
)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Long,
    val tokenType: String
)
