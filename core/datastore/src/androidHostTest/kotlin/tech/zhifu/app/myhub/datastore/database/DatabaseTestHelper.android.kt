package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * Android 平台测试数据库创建实现
 * 
 * 使用 JdbcSqliteDriver 创建内存数据库（IN_MEMORY），适用于 Android 单元测试。
 * 内存数据库不会持久化，测试结束后自动销毁。
 * 
 * 注意：Android 平台测试使用 JVM 测试环境，因此使用 JdbcSqliteDriver 而非 AndroidSqliteDriver。
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        execute(null, "PRAGMA foreign_keys = ON;", 0)
    }
    MyHubDatabase.Schema.synchronous().create(driver)
    return MyHubDatabase(driver)
}
