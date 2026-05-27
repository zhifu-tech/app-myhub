package tech.zhifu.app.myhub.di

import org.koin.core.module.Module
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.logger.LoggerConfig
import tech.zhifu.app.myhub.logger.di.loggerModule
import tech.zhifu.app.myhub.service.di.serviceModule

/**
 * 返回 Server 使用的 Koin 模块列表。
 * 供 Ktor Application 内 install(Koin) { modules(...) } 使用。
 */
fun koinModules(): List<Module> = listOf(
    loggerModule { LoggerConfig(appName = "Myhub") },
    databaseModule,
    repositoryModule,
    serviceModule
)
