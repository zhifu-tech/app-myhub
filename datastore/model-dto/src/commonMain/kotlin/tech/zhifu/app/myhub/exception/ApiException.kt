package tech.zhifu.app.myhub.exception

import io.ktor.http.HttpStatusCode

/**
 * API 异常（统一用于服务端抛出与客户端解析非 2xx 响应）
 * - 服务端：带 statusCode，供 StatusPages 写回 HTTP 状态
 * - 客户端：带 response.status 与解析出的 message，便于上层按状态码/文案处理
 */
open class ApiException(
    val statusCode: HttpStatusCode,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /** 仅 message 的便捷构造（无 status 时默认 InternalServerError，如部分客户端封装） */
    constructor(message: String, cause: Throwable? = null) : this(HttpStatusCode.InternalServerError, message, cause)
}

