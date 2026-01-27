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

