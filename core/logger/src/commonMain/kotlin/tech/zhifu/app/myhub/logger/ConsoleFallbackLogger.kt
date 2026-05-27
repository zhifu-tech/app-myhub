package tech.zhifu.app.myhub.logger

class ConsoleFallbackLogger(
    private val name: String
) : Logger {
    override fun isTraceEnabled(): Boolean = true
    override fun trace(marker: Any?, throwable: Throwable?, message: () -> Any?) {
        log("TRACE", marker, throwable, message)
    }

    override fun isDebugEnabled(): Boolean = true
    override fun debug(marker: Any?, throwable: Throwable?, message: () -> Any?) {
        log("DEBUG", marker, throwable, message)
    }

    override fun isInfoEnabled(): Boolean = true
    override fun info(marker: Any?, throwable: Throwable?, message: () -> Any?) {
        log("INFO", marker, throwable, message)
    }

    override fun isWarnEnabled(): Boolean = true
    override fun warn(marker: Any?, throwable: Throwable?, message: () -> Any?) {
        log("WARN", marker, throwable, message)
    }

    override fun isErrorEnabled(): Boolean = true
    override fun error(marker: Any?, throwable: Throwable?, message: () -> Any?) {
        log("ERROR", marker, throwable, message)
    }

    private fun log(
        level: String,
        marker: Any?,
        throwable: Throwable?,
        message: () -> Any?
    ) {
        val markerText = marker?.toString()?.takeIf { it.isNotBlank() }?.let { "[$it] " } ?: ""
        println("$level $name - $markerText${message()?.toString().orEmpty()}")
        throwable?.printStackTrace()
    }
}
