package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

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

/**
 * JVM 平台：使用反射访问 driver 并关闭
 */
actual fun destroyTestDatabase(database: MyHubDatabase) {
    try {
        val driverField = database.javaClass.getDeclaredField("driver")
        driverField.isAccessible = true
        val driver = driverField.get(database) as? SqlDriver
        driver?.close()
    } catch (e: Exception) {
        // 如果反射失败，忽略错误（测试已经结束）
        println("destroyTestDatabase error: $e")
    }
}

