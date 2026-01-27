package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import tech.zhifu.app.myhub.datastore.model.dto.ErrorDetail
import tech.zhifu.app.myhub.datastore.model.dto.ErrorResponse

internal fun Application.configException() {
    install(StatusPages) {
        exception<UnauthorizedException> { call, exception ->
            call.respond(
                status = HttpStatusCode.Companion.Unauthorized,
                message = ErrorResponse(
                    error = ErrorDetail(
                        code = "UNAUTHORIZED",
                        message = exception.message ?: "Authentication required",
                        path = call.request.path()
                    )
                )
            )
        }

        exception<NotFoundException> { call, exception ->
            call.respond(
                status = HttpStatusCode.Companion.NotFound,
                message = ErrorResponse(
                    error = ErrorDetail(
                        code = "NOT_FOUND",
                        message = exception.message ?: "Resource not found",
                        path = call.request.path()
                    )
                )
            )
        }

        exception<ValidationException> { call, exception ->
            call.respond(
                status = HttpStatusCode.Companion.BadRequest,
                message = ErrorResponse(
                    error = ErrorDetail(
                        code = "VALIDATION_ERROR",
                        message = exception.message ?: "Validation failed",
                        path = call.request.path()
                    )
                )
            )
        }

        exception<ForbiddenException> { call, exception ->
            call.respond(
                status = HttpStatusCode.Companion.Forbidden,
                message = ErrorResponse(
                    error = ErrorDetail(
                        code = "FORBIDDEN",
                        message = exception.message ?: "Access forbidden",
                        path = call.request.path()
                    )
                )
            )
        }

        exception<ApiException> { call, exception ->
            call.respond(
                status = exception.statusCode,
                message = ErrorResponse(
                    error = ErrorDetail(
                        code = "API_ERROR",
                        message = exception.message ?: "Unknown error",
                        path = call.request.path()
                    )
                )
            )
        }

        exception<IllegalArgumentException> { call, exception ->
            call.respond(
                status = HttpStatusCode.Companion.BadRequest,
                message = ErrorResponse(
                    error = ErrorDetail(
                        code = "BAD_REQUEST",
                        message = exception.message ?: "Invalid request",
                        path = call.request.path()
                    )
                )
            )
        }

        exception<Exception> { call, exception ->
            exception.printStackTrace() // 记录错误日志
            call.respond(
                status = HttpStatusCode.Companion.InternalServerError,
                message = ErrorResponse(
                    error = ErrorDetail(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "Internal server error: ${exception.message}",
                        path = call.request.path()
                    )
                )
            )
        }
    }
}
