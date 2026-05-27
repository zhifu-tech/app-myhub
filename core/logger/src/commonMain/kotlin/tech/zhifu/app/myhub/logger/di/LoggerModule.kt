package tech.zhifu.app.myhub.logger.di

import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.logger.LoggerConfig

/**
 * Logger 模块
 * 提供 Logger 配置
 */
fun loggerModule(config: () -> LoggerConfig): Module = module {
    // 注册 LoggerConfig
    single<LoggerConfig> {
        config()
    }
}

