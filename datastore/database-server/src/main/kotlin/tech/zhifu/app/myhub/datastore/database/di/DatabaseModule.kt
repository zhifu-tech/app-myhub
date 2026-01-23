package tech.zhifu.app.myhub.datastore.database.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.DatabaseConfig
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * 数据库模块（服务端）
 * 提供数据库实例
 */
val databaseModule = module {
    // 数据库配置
    factory<DatabaseConfig> {
        DatabaseConfig.fromEnvironment()
    }

    // 数据库驱动工厂
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(get<DatabaseConfig>())
    }

    // 数据库实例
    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}


