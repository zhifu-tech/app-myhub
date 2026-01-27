package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode

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
