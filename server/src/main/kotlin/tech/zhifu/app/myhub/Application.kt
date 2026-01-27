package tech.zhifu.app.myhub

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import tech.zhifu.app.myhub.api.authApi
import tech.zhifu.app.myhub.api.cardTemplatesApi
import tech.zhifu.app.myhub.api.cardsApi
import tech.zhifu.app.myhub.api.collectionsApi
import tech.zhifu.app.myhub.api.syncApi
import tech.zhifu.app.myhub.api.tagsApi
import tech.zhifu.app.myhub.api.usersApi
import tech.zhifu.app.myhub.auth.TokenService
import tech.zhifu.app.myhub.auth.configureAuthentication
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.di.initKoin
import tech.zhifu.app.myhub.exception.configException
import tech.zhifu.app.myhub.service.CardService
import tech.zhifu.app.myhub.service.CardTemplateService
import tech.zhifu.app.myhub.service.CollectionService
import tech.zhifu.app.myhub.service.SyncService
import tech.zhifu.app.myhub.service.TagService
import tech.zhifu.app.myhub.service.UserService

const val SERVER_PORT = 8083

fun main() {
    // 初始化 Koin 依赖注入
    initKoin()

    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
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
        allowHeader("Authorization")
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Patch)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Options)
    }

    // 配置认证（JWT）
    configureAuthentication()

    // 配置错误处理
    configException()

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
        // 认证 API（不需要认证）
        authApi(
            tokenService = GlobalContext.get().get<TokenService>(),
            userService = GlobalContext.get().get<UserService>(),
            userRepository = GlobalContext.get().get<UserRepository>()
        )

        // 需要认证的 API
        syncApi(GlobalContext.get().get<SyncService>())
        usersApi(GlobalContext.get().get<UserService>())
        cardsApi(GlobalContext.get().get<CardService>())
        tagsApi(GlobalContext.get().get<TagService>())
        collectionsApi(GlobalContext.get().get<CollectionService>())
        cardTemplatesApi(GlobalContext.get().get<CardTemplateService>())
    }
}

