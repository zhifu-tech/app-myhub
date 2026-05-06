package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun databaseDriverFactoryModule(): Module = module {
    factory<DatabaseDriverFactory> {
        DatabaseDriverFactory {
            NativeSqliteDriver(
                schema = MyHubDatabase.Schema.synchronous(),
                name = "myhub.db"
            )
        }
    }
}
