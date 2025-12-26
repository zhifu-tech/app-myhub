package tech.zhifu.app.myhub.logger

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KMarkerFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker

internal actual fun logger(name: String): Logger {
    useAndroidLogger()
    return wrapKLogger(KotlinLogging.logger(name))
}

actual fun logger(func: () -> Unit): Logger {
    useAndroidLogger()
    return wrapKLogger(KotlinLogging.logger(func))
}

private var hasSetSystemProperty = false
private fun useAndroidLogger() {
    // 通过设置 System 属性，调用 native 本地的日志处理
    // 1. 减小包大小：省去不必要的包的导入
    // 2. 日志采用原生实现，可以复用平台的优化处理
    if (!hasSetSystemProperty) {
        hasSetSystemProperty = true
        System.setProperty("kotlin-logging-to-android-native", "true")
    }
}

private fun wrapKLogger(delegate: KLogger): Logger = object : Logger {
    override fun isTraceEnabled() = delegate.isTraceEnabled()
    override fun trace(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    ) = delegate.trace(throwable, marker?.asMarker(), message)

    override fun isDebugEnabled() = delegate.isDebugEnabled()
    override fun debug(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    ) = delegate.debug(throwable, marker?.asMarker(), message)

    override fun isInfoEnabled() = delegate.isInfoEnabled()
    override fun info(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    ) = delegate.info(throwable, marker?.asMarker(), message)

    override fun isWarnEnabled() = delegate.isWarnEnabled()
    override fun warn(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    ) = delegate.warn(throwable, marker?.asMarker(), message)

    override fun isErrorEnabled() = delegate.isErrorEnabled()
    override fun error(
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    ) = delegate.error(throwable, marker?.asMarker(), message)
}

private fun Any?.asMarker(): Marker? = when (this) {
    is Marker -> this
    is String -> KMarkerFactory.getMarker(this)
    else -> null
}

