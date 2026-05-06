package tech.zhifu.app.myhub.datastore.database.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.database.DatabaseDriverFactory
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.databaseDriverFactoryModule

val databaseModule = module {
    includes(databaseDriverFactoryModule())
    single<MyHubDatabase> {
        val driverFactory = get<DatabaseDriverFactory>()
        MyHubDatabase(driverFactory.createDriver())
    }
}

