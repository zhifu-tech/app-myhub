package tech.zhifu.app.myhub.datastore.network

/**
 * API异常
 * 当API返回错误状态码时抛出
 */
class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 网络异常
 * 当网络连接错误时抛出
 */
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)


