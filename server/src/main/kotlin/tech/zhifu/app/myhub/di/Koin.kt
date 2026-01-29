package tech.zhifu.app.myhub.di

import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.logger.LoggerConfig
import tech.zhifu.app.myhub.logger.di.loggerModule
import tech.zhifu.app.myhub.service.di.serviceModule

/**
 * 初始化 Koin 依赖注入
 */
fun initKoin() {
    startKoin {
        modules(
            loggerModule {
                LoggerConfig(appName = "Myhub")
            },
            databaseModule,  // 服务端数据库模块
            repositoryModule,  // 服务端仓库模块（包含 localDataSourceModule）
            serviceModule
        )
    }
}

