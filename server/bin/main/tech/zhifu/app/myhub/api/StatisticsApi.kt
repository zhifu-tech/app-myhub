package tech.zhifu.app.myhub.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.service.StatisticsService

/**
 * 统计信息 API 路由
 */
fun Route.statisticsApi(statisticsService: StatisticsService) {
    route("/api/statistics") {
        // GET /api/statistics - 获取统计信息
        get {
            try {
                val statistics = statisticsService.getStatistics()
                call.respond(HttpStatusCode.OK, statistics)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to get statistics: ${e.message}", e)
            }
        }

        // POST /api/statistics/refresh - 刷新统计信息
        post("refresh") {
            try {
                val statistics = statisticsService.refreshStatistics()
                call.respond(HttpStatusCode.OK, statistics)
            } catch (e: Exception) {
                throw ApiException(HttpStatusCode.InternalServerError, "Failed to refresh statistics: ${e.message}", e)
            }
        }
    }
}

