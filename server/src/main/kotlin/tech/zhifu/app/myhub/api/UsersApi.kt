package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.UserService

/**
 * 用户 API 路由
 */
fun Route.usersApi(userService: UserService) {
    route("/api/users") {
        // PUT /api/users/current - 获取当前用户
        put("fetchUser") {
            try {
                val userId = call.request.queryParameters["userId"]
                    ?: throw ValidationException("Missing userId parameter")
                val user: User = userService.fetchUser(userId)
                    ?: throw ApiException(HttpStatusCode.NotFound, "User not found")
                call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get current user: ${e.message}", e)
            }
        }
    }
}
