package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import tech.zhifu.app.myhub.service.SyncService
import tech.zhifu.app.myhub.sync.SyncPushRequest

fun Route.syncApi(syncService: SyncService) {
    route("/api/sync") {
        post("/push") {
            val request = call.receive<SyncPushRequest>()
            val response = syncService.push(request)
            call.respond(HttpStatusCode.OK, response)
        }
        get("/pull") {
            val userId = call.request.queryParameters["userId"]
            val entityType = call.request.queryParameters["entityType"]
            val since = call.request.queryParameters["since"]
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 200
            if (userId.isNullOrBlank() || entityType.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing userId/entityType"))
                return@get
            }
            val response = syncService.pull(
                userId = userId,
                entityType = entityType,
                sinceToken = since,
                limit = limit
            )
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
