package tech.zhifu.app.myhub.datastore.database.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.DatabaseConfig
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

val databaseModule = module {
    factory<DatabaseConfig> {
        DatabaseConfig.fromEnvironment()
    }
    factoryOf(::DatabaseDriverFactory)
    // 数据库实例
    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}


