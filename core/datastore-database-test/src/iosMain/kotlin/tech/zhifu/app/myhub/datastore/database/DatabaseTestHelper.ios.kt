package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS 平台测试数据库创建实现
 *
 * 使用 NativeSqliteDriver 创建内存数据库，适用于 iOS 模拟器和真机测试。
 * 使用 ":memory:" 作为数据库名称创建内存数据库，确保每个测试都是独立的。
 * 内存数据库不会持久化，测试结束后自动销毁。
 *
 * 注意：NativeSqliteDriver 在传入 schema 参数时会自动创建表，不需要显式调用 create()。
 * 这与 JdbcSqliteDriver 不同，JdbcSqliteDriver 需要显式调用 create()。
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = NativeSqliteDriver(
        schema = MyHubDatabase.Schema.synchronous(),
        name = ":memory:"
    ).apply {
        execute(null, "PRAGMA foreign_keys = ON;", 0)
    }
    return MyHubDatabase(driver)
}

