package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * JavaScript 平台测试数据库创建实现
 * 
 * 使用 WebWorkerDriver 创建内存数据库，适用于浏览器环境测试。
 * 注意：必须使用异步方法（awaitCreate, await）因为 WebWorkerDriver 是异步的。
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = DatabaseDriverFactory().createDriver()
    MyHubDatabase.Schema.awaitCreate(driver)
    // 启用外键约束以支持级联删除
    driver.execute(null, "PRAGMA foreign_keys = ON;", 0).await()
    return MyHubDatabase(driver)
}
