package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.service.UserService

/**
 * 用户 API 路由
 */
fun Route.usersApi(userService: UserService) {
    route("/api/users") {
        // GET /api/users/current - 获取当前用户
        get("current") {
            try {
                val user = userService.getCurrentUser()
                call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get current user: ${e.message}", e)
            }
        }

        // PUT /api/users/current - 更新当前用户
        put("current") {
            try {
                val request = call.receive<UpdateUserRequest>()
                val existing = userService.getCurrentUser()

                val preferences = request.preferences?.let { prefs ->
                    UserPreferences(
                        theme = prefs.theme ?: existing.preferences?.theme ?: "dark",
                        language = prefs.language ?: existing.preferences?.language ?: "en",
                        defaultCardType = prefs.defaultCardType?.let { CardType.valueOf(it.uppercase()) }
                            ?: existing.preferences?.defaultCardType,
                        autoSync = prefs.autoSync ?: existing.preferences?.autoSync ?: true,
                        syncInterval = prefs.syncInterval ?: existing.preferences?.syncInterval ?: 3600000L
                    )
                } ?: existing.preferences

                val updated = existing.copy(
                    username = request.username ?: existing.username,
                    email = request.email ?: existing.email,
                    displayName = request.displayName ?: existing.displayName,
                    avatarUrl = request.avatarUrl ?: existing.avatarUrl,
                    preferences = preferences
                )

                val user = userService.updateUser(updated)
                call.respond(HttpStatusCode.OK, user)
            } catch (e: tech.zhifu.app.myhub.exception.ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to update user: ${e.message}", e)
            }
        }
    }
}

/**
 * 更新用户请求
 */
@kotlinx.serialization.Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val preferences: UpdateUserPreferencesRequest? = null
)

/**
 * 更新用户偏好请求
 */
@kotlinx.serialization.Serializable
data class UpdateUserPreferencesRequest(
    val theme: String? = null,
    val language: String? = null,
    val defaultCardType: String? = null,
    val autoSync: Boolean? = null,
    val syncInterval: Long? = null
)

