package tech.zhifu.app.myhub.logger

import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

internal actual fun LoggerConfig.configPlatform() {
    KotlinLoggingConfiguration.direct.logLevel = if (enableDebugLogs) {
        Level.DEBUG
    } else {
        Level.INFO
    }
}
