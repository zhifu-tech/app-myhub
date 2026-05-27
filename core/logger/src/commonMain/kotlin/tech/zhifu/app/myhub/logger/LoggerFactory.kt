package tech.zhifu.app.myhub.logger

import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.mp.KoinPlatform.getKoin

/**
 * 默认 Logger 实例 (使用应用默认名称)
 */
val logger: Logger by lazy { logger() }

/**
 * 工厂函数：通过 String 标签创建 Logger
 */
fun logger(vararg tags: String): Logger {
    val name = if (tags.isEmpty()) {
        loggerConfig.appName
    } else {
        (listOf(loggerConfig.appName) + tags).joinToString(":")
    }
    return runCatching {
        LoggerImpl(KotlinLogging.logger(name))
    }.getOrElse {
        ConsoleFallbackLogger(name)
    }
}

/**
 * 配置项的懒加载逻辑
 */
private val loggerConfig by lazy {
    val config = runCatching { getKoin().getOrNull<LoggerConfig>() }.getOrNull()
        ?: LoggerConfig("MyHub")

    config.apply { configPlatform() }
}

internal expect fun LoggerConfig.configPlatform()
