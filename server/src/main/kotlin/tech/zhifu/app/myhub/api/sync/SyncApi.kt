package tech.zhifu.app.myhub.api.sync

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.get
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.SyncService
import tech.zhifu.app.myhub.sync.SyncPushRequest

/**
 * 同步 API 路由（领域：sync）
 *
 * POST /api/sync              - 推送同步数据（创建同步操作）
 * GET  /api/sync/changes      - 获取同步变更（查询同步资源）
 */
fun Route.syncApi() {
    route("/api/sync") {
        post {
            val syncService = call.application.get<SyncService>()
            try {
                val request = call.receive<SyncPushRequest>()
                val response = syncService.push(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ValidationException) { throw e }
            catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to push sync data: ${e.message}", e)
            }
        }

        get("changes") {
            val syncService = call.application.get<SyncService>()
            try {
                val userId = call.request.queryParameters["userId"]
                val entityType = call.request.queryParameters["entityType"]
                val since = call.request.queryParameters["since"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 200
                if (userId.isNullOrBlank() || entityType.isNullOrBlank()) {
                    throw ValidationException("Missing required parameters: userId and entityType")
                }
                require(limit > 0 && limit <= 1000) { "Limit must be between 1 and 1000" }
                val response = syncService.pull(userId = userId, entityType = entityType, sinceToken = since, limit = limit)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ValidationException) { throw e }
            catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to pull sync changes: ${e.message}", e)
            }
        }
    }
}
