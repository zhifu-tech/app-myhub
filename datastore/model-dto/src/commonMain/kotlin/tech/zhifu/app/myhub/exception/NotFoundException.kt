package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode

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
