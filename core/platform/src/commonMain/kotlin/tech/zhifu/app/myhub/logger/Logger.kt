package tech.zhifu.app.myhub.logger

/**
 * MyHub 日志接口，定义所有日志级别的方法
 *
 * 支持的日志级别（按优先级从低到高）：
 * - TRACE - 最详细的日志信息，用于深度调试
 * - DEBUG - 调试信息，开发阶段的主要工具
 * - INFO - 信息性消息，记录重要事件和状态变化
 * - WARN - 警告信息，记录潜在问题
 * - ERROR - 错误信息，记录严重问题
 */
interface Logger {
    /**
     * TRACE - Most detailed and trivial log information.
     * Used for recording detailed program execution flow, variable value changes,
     * and very low-level debugging information.
     *
     * Note: This level of logging should ALWAYS be disabled in release builds
     * to avoid performance overhead and prevent sensitive information leakage.
     */
    fun isTraceEnabled(): Boolean
    fun trace(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    )

    /**
     * DEBUG - Debug information. Used for recording information that helps
     * understand program state and diagnose issues.
     *
     * Note: Should be disabled in release builds.
     */
    fun isDebugEnabled(): Boolean
    fun debug(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    )

    /**
     * INFO - Informational messages. Used to record important events, state changes,
     * or milestones during application runtime.
     */
    fun isInfoEnabled(): Boolean
    fun info(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    )

    /**
     * WARN - Warning information. Indicates that potential problems or abnormal situations
     * have occurred, but the application can still continue to run.
     */
    fun isWarnEnabled(): Boolean
    fun warn(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    )

    /**
     * ERROR - Error information. Indicates that relatively serious problems have occurred,
     * causing some functionality of the application to fail.
     */
    fun isErrorEnabled(): Boolean
    fun error(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    )
}

fun Logger.trace(message: () -> Any?) =
    trace(null, null, message)

fun Logger.trace(throwable: Throwable?, message: () -> Any?) =
    trace(null, throwable, message)

fun Logger.debug(message: () -> Any?) =
    debug(null, null, message)

fun Logger.debug(throwable: Throwable?, message: () -> Any?) =
    debug(null, throwable, message)

fun Logger.info(message: () -> Any?) =
    info(null, null, message)

fun Logger.info(throwable: Throwable?, message: () -> Any?) =
    info(null, throwable, message)

fun Logger.warn(message: () -> Any?) =
    warn(null, null, message)

fun Logger.warn(throwable: Throwable?, message: () -> Any?) =
    warn(null, throwable, message)

fun Logger.error(message: () -> Any?) =
    error(null, null, message)

fun Logger.error(throwable: Throwable?, message: () -> Any?) =
    error(null, throwable, message)
