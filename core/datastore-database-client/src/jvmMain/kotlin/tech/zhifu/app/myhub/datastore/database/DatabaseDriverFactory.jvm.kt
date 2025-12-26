package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".myhub/myhub.db")
        if (!databasePath.parentFile.exists()) {
            databasePath.parentFile.mkdirs()
        }
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")

        // 检查数据库文件是否已存在
        val databaseExists = databasePath.exists()

        if (!databaseExists) {
            // 数据库文件不存在，创建新数据库和表
            MyHubDatabase.Schema.synchronous().create(driver)
        }
        // 如果数据库文件已存在，说明表已经创建，不需要再次创建
        // SQLDelight 会在需要时自动处理迁移

        return driver
    }
}

actual fun databaseDriverFactoryModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }
}
