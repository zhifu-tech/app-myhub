package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode

/**
 * 未授权异常
 */
class UnauthorizedException(
    message: String = "Unauthorized",
    cause: Throwable? = null
) : ApiException(
    HttpStatusCode.Unauthorized,
    message,
    cause
)
