package tech.zhifu.app.myhub.network

/**
 * 网络异常
 * 当网络连接错误时抛出（如超时、无网络）。
 * API 非 2xx 由业务层统一使用 [tech.zhifu.app.myhub.exception.ApiException]。
 */
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
