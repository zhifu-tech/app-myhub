package tech.zhifu.app.myhub

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import tech.zhifu.app.myhub.api.cardsApi
import tech.zhifu.app.myhub.api.statisticsApi
import tech.zhifu.app.myhub.api.tagsApi
import tech.zhifu.app.myhub.api.templatesApi
import tech.zhifu.app.myhub.api.usersApi
import tech.zhifu.app.myhub.datastore.database.manage.DatabaseManager
import tech.zhifu.app.myhub.exception.ApiException
import tech.zhifu.app.myhub.di.initKoin
import tech.zhifu.app.myhub.service.CardService
import tech.zhifu.app.myhub.service.StatisticsService
import tech.zhifu.app.myhub.service.TagService
import tech.zhifu.app.myhub.service.TemplateService
import tech.zhifu.app.myhub.service.UserService

const val SERVER_PORT = 8083

fun main() {
    // 初始化 Koin 依赖注入
    initKoin()

    // 初始化数据库数据（可通过环境变量控制）
    val shouldInitData = System.getenv("INIT_DATABASE")?.toBoolean() ?: true
    if (shouldInitData) {
        runBlocking {
            try {
                val databaseManager = GlobalContext.get().get<DatabaseManager>()
                val clearBeforeLoad = System.getenv("CLEAR_DATABASE_BEFORE_INIT")?.toBoolean() ?: false
                databaseManager.loadAllData(
                    resourcePath = "database/init",
                    clearBeforeLoad = clearBeforeLoad
                )
                println("✅ Database initialization completed successfully")
            } catch (e: Exception) {
                println("⚠️  Database initialization failed: ${e.message}")
                e.printStackTrace()
                // 继续启动服务器，即使初始化失败
            }
        }
    } else {
        println("ℹ️  Database initialization skipped (INIT_DATABASE=false)")
    }

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 配置 JSON 序列化
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
                prettyPrint = true
            }
        )
    }

    // 配置 CORS
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Options)
    }

    // 配置错误处理
    install(StatusPages) {
        exception<ApiException> { call, exception ->
            call.respond(exception.statusCode, ErrorResponse(exception.message ?: "Unknown error"))
        }
        exception<IllegalArgumentException> { call, exception ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(exception.message ?: "Invalid request"))
        }
        exception<Exception> { call, exception ->
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal server error: ${exception.message}"))
        }
    }

    // 从 Koin 获取 Service 实例
    val cardService = GlobalContext.get().get<CardService>()
    val tagService = GlobalContext.get().get<TagService>()
    val templateService = GlobalContext.get().get<TemplateService>()
    val userService = GlobalContext.get().get<UserService>()
    val statisticsService = GlobalContext.get().get<StatisticsService>()

    // 配置路由
    routing {
        // 根路径
        get("/") {
            call.respondText("MyHub Server API - Version 1.0.0")
        }

        // 健康检查
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok", "version" to "1.0.0"))
        }

        // API 路由
        cardsApi(cardService)
        tagsApi(tagService)
        templatesApi(templateService)
        usersApi(userService)
        statisticsApi(statisticsService)
    }
}

/**
 * 错误响应格式
 */
@kotlinx.serialization.Serializable
data class ErrorResponse(
    val error: String
)
