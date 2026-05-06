package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun databaseDriverFactoryModule(): Module = module {
    factory<DatabaseDriverFactory> {
        DatabaseDriverFactory {
            AndroidSqliteDriver(
                schema = MyHubDatabase.Schema.synchronous(),
                context = get(),
                name = "myhub.db"
            )
        }
    }
}
