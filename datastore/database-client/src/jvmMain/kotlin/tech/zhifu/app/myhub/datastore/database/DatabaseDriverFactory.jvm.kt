package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import org.koin.core.module.Module
import org.koin.dsl.module

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val appDataDir = FileKit.filesDir / "app-data"
        appDataDir.createDirectories()
        val databasePath = (appDataDir / "myhub.db").path
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:$databasePath")

        // 检查数据库文件是否已存在
        val databaseExists = (appDataDir / "myhub.db").exists()

        if (!databaseExists) {
            // 数据库文件不存在，创建新数据库和表（版本 2）
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
