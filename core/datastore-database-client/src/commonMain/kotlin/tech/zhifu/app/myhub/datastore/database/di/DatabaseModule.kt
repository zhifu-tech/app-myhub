package tech.zhifu.app.myhub.datastore.database.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.databaseDriverFactoryModule

/**
 * 数据库模块（客户端）
 * 提供数据库实例
 *
 * 注意：DatabaseDriverFactory 来自 core/datastore-database-client 模块
 */
val databaseModule = module {
    includes(databaseDriverFactoryModule())
    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}

