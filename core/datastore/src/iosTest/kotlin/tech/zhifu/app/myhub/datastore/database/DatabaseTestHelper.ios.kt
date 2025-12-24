package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * iOS 平台测试数据库创建实现
 * 
 * 使用 NativeSqliteDriver 创建内存数据库，适用于 iOS 模拟器和真机测试。
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = NativeSqliteDriver(
        schema = MyHubDatabase.Schema.synchronous(),
        name = "test.db"
    ).apply {
        execute(null, "PRAGMA foreign_keys = ON;", 0)
    }
    return MyHubDatabase(driver)
}
