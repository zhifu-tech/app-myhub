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
import org.koin.ktor.plugin.Koin
import tech.zhifu.app.myhub.api.auth.authApi
import tech.zhifu.app.myhub.api.capture.captureAnalysisApi
import tech.zhifu.app.myhub.api.media.mediaUploadApi
import tech.zhifu.app.myhub.api.sync.syncApi
import tech.zhifu.app.myhub.auth.configureAuthentication
import tech.zhifu.app.myhub.di.koinModules
import tech.zhifu.app.myhub.exception.configException

const val SERVER_PORT = 8083

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    // Koin 依赖注入（Ktor 插件，Route 内按需 get<>()）
    install(Koin) {
        modules(koinModules())
    }

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

    configureAuthentication()
    configException()

    routing {
        get("/") {
            call.respondText("MyHub Server API - Version 1.0.0")
        }

        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok", "version" to "1.0.0"))
        }

        authApi()
        syncApi()
//        usersApi()
        mediaUploadApi()
        captureAnalysisApi()
    }
}
