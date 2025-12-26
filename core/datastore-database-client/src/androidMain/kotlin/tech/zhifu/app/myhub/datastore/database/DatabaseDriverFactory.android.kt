package tech.zhifu.app.myhub.datastore.database

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 平台的数据库驱动实现
 * 使用构造函数注入 Context，以保持代码的可测试性和清晰的依赖关系
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MyHubDatabase.Schema.synchronous(),
            context = context,
            name = "myhub.db"
        )
    }
}

actual fun databaseDriverFactoryModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(get())
    }
}
