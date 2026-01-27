package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode

/**
 * 请求验证异常
 */
class ValidationException(
    message: String,
    cause: Throwable? = null
) : ApiException(
    HttpStatusCode.BadRequest,
    message,
    cause
)
