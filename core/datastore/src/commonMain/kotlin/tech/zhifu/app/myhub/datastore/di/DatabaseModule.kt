package tech.zhifu.app.myhub.datastore.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * 数据库模块
 * 提供数据库实例
 */
val databaseModule = module {
    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}

