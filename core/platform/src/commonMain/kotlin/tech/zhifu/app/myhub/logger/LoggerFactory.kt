package tech.zhifu.app.myhub.logger

val logger by lazy { logger() }

fun logger(vararg tags: String): Logger =
    logger(listOf("MyHub", *tags).joinToString(":"))

/**
 * 获取指定名称的 Logger 实例
 *
 * @param name Logger 名称，通常是类名
 * @return Logger 实例
 */
internal expect fun logger(name: String): Logger

/**
 * 通过函数获取 Logger 实例（用于全局 logger）
 */
expect fun logger(func: () -> Unit): Logger
