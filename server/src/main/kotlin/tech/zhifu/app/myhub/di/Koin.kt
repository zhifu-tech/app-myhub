package tech.zhifu.app.myhub.di

import org.koin.core.context.startKoin
import tech.zhifu.app.myhub.datastore.database.di.databaseModule
import tech.zhifu.app.myhub.datastore.database.manage.di.databaseManagerModule
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.service.di.serviceModule

/**
 * 初始化 Koin 依赖注入
 */
fun initKoin() {
    startKoin {
        modules(
            databaseModule,  // 服务端数据库模块
            databaseManagerModule,  // 数据库管理器模块（用于初始化数据）
            repositoryModule,  // 服务端仓库模块（包含 localDataSourceModule）
            serviceModule
        )
    }
}

