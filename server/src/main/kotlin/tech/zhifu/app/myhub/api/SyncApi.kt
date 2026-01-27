package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.service.SyncService
import tech.zhifu.app.myhub.sync.SyncPushRequest

/**
 * 同步 API 路由（RESTful 设计）
 *
 * POST /api/sync              - 推送同步数据（创建同步操作）
 * GET  /api/sync/changes      - 获取同步变更（查询同步资源）
 */
fun Route.syncApi(syncService: SyncService) {
    route("/api/sync") {
        // POST /api/sync - 推送同步数据（创建同步操作）
        // 符合 RESTful：POST 用于创建资源（同步操作）
        post {
            try {
                val request = call.receive<SyncPushRequest>()
                val response = syncService.push(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(
                    HttpStatusCode.InternalServerError,
                    "Failed to push sync data: ${e.message}",
                    e
                )
            }
        }

        // GET /api/sync/changes - 获取同步变更（查询同步资源）
        // 符合 RESTful：GET 用于查询资源，使用子资源 /changes 表示变更列表
        get("changes") {
            try {
                val userId = call.request.queryParameters["userId"]
                val entityType = call.request.queryParameters["entityType"]
                val since = call.request.queryParameters["since"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 200

                if (userId.isNullOrBlank() || entityType.isNullOrBlank()) {
                    throw ValidationException("Missing required parameters: userId and entityType")
                }

                // 验证 limit 参数
                require(limit > 0 && limit <= 1000) {
                    "Limit must be between 1 and 1000"
                }

                val response = syncService.pull(
                    userId = userId,
                    entityType = entityType,
                    sinceToken = since,
                    limit = limit
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: ValidationException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(
                    HttpStatusCode.InternalServerError,
                    "Failed to pull sync changes: ${e.message}",
                    e
                )
            }
        }
    }
}
