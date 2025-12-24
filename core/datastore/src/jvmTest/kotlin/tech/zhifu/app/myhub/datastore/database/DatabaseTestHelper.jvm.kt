package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * JVM (Desktop) 平台测试数据库创建实现
 * 
 * 使用 JdbcSqliteDriver 创建内存数据库（IN_MEMORY），适用于桌面应用测试。
 * 内存数据库不会持久化，测试结束后自动销毁。
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        execute(null, "PRAGMA foreign_keys = ON;", 0)
    }
    MyHubDatabase.Schema.synchronous().create(driver)
    return MyHubDatabase(driver)
}
