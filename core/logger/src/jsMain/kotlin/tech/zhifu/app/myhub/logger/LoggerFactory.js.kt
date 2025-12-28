package tech.zhifu.app.myhub.logger

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KMarkerFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker

internal actual fun logger(name: String): Logger =
    wrapKLogger(KotlinLogging.logger(name))

actual fun logger(func: () -> Unit): Logger =
    wrapKLogger(KotlinLogging.logger(func))

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

