package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode

/**
 * API 异常
 */
open class ApiException(
    val statusCode: HttpStatusCode,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * 资源未找到异常
 */
class NotFoundException(
    resource: String,
    id: String,
    cause: Throwable? = null
) : ApiException(
    HttpStatusCode.NotFound,
    "$resource with id '$id' not found",
    cause
)

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

/**
 * 冲突异常（如资源已存在）
 */
class ConflictException(
    message: String,
    cause: Throwable? = null
) : ApiException(
    HttpStatusCode.Conflict,
    message,
    cause
)

