package tech.zhifu.app.myhub.logger

import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.mp.KoinPlatform.getKoin

val logger by lazy { logger() }

/**
 * 获取指定名称的 Logger 实例
 */
fun logger(vararg tags: String): Logger {
    val name = listOf(config.appName, *tags).joinToString(":")
    return LoggerImpl(KotlinLogging.logger(name))
}

private val config by lazy {
    getKoin().get<LoggerConfig>().apply {
        configPlatform()
    }
}

internal expect fun LoggerConfig.configPlatform()
