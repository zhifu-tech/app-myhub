package tech.zhifu.app.myhub.api.user

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.model.dto.CreateUserRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateUserRequest
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.NotFoundException
import tech.zhifu.app.myhub.exception.UnauthorizedException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.UserService

/**
 * 用户 API 路由（领域：user）
 *
 * GET    /api/users/{id}         - 获取指定用户
 * POST   /api/users              - 创建用户
 * PUT    /api/users/{id}         - 完整更新用户
 * DELETE /api/users/{id}         - 删除用户
 * GET    /api/users/{id}/preferences  - 获取用户偏好设置
 * PUT    /api/users/{id}/preferences  - 更新用户偏好设置
 */
fun Route.usersApi() {
    authenticate("auth-bearer") {
        route("/api/users") {
            get("{id}") {
                val userService = call.application.get<UserService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("User ID is required")
                    val user = userService.getUser(id)
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to get user: ${e.message}", e) }
            }

            post {
                val userService = call.application.get<UserService>()
                try {
                    val request = call.receive<CreateUserRequest>()
                    val user = userService.createUser(request)
                    call.respond(HttpStatusCode.Created, user)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to create user: ${e.message}", e) }
            }

            put("{id}") {
                val userService = call.application.get<UserService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("User ID is required")
                    val request = call.receive<UpdateUserRequest>()
                    val user = userService.updateUser(id, request)
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to update user: ${e.message}", e) }
            }

            delete("{id}") {
                val userService = call.application.get<UserService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("User ID is required")
                    userService.deleteUser(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to delete user: ${e.message}", e) }
            }

            get("{id}/preferences") {
                val userService = call.application.get<UserService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("User ID is required")
                    val preferences = userService.getUserPreferences(id)
                    call.respond(HttpStatusCode.OK, preferences)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to get user preferences: ${e.message}", e) }
            }

            put("{id}/preferences") {
                val userService = call.application.get<UserService>()
                try {
                    val id = call.parameters["id"] ?: throw ValidationException("User ID is required")
                    val preferences = call.receive<UserPreferences>()
                    val updated = userService.upsertUserPreferences(id, preferences)
                    call.respond(HttpStatusCode.OK, updated)
                } catch (e: UnauthorizedException) { throw e }
                catch (e: NotFoundException) { throw e }
                catch (e: ValidationException) { throw e }
                catch (e: Exception) { throw ApiException(HttpStatusCode.InternalServerError, "Failed to update user preferences: ${e.message}", e) }
            }
        }
    }
}
