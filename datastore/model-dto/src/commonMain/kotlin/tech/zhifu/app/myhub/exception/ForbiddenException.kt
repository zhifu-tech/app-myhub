package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode

/**
 * 权限不足异常
 */
class ForbiddenException(
    message: String,
    cause: Throwable? = null
) : ApiException(
    HttpStatusCode.Forbidden,
    message,
    cause
)
