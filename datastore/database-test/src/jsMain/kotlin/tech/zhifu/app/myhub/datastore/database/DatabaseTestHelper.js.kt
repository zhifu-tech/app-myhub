package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker
/**
 * JavaScript 平台测试数据库创建实现
 *
 * 使用 WebWorkerDriver 创建内存数据库，适用于浏览器环境测试。
 * 注意：必须使用异步方法（awaitCreate, await）因为 WebWorkerDriver 是异步的。
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    val driver = WebWorkerDriver(
        Worker(js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)"""))
    )
    MyHubDatabase.Schema.awaitCreate(driver)
    // 启用外键约束以支持级联删除
    driver.execute(null, "PRAGMA foreign_keys = ON;", 0).await()
    return MyHubDatabase(driver)
}

/**
 * JS 平台：直接访问 driver 并关闭
 * 
 * 注意：SQLDelight 生成的数据库类在 JS 平台上可能没有 driver 属性。
 * 对于内存数据库，关闭 driver 会销毁整个数据库。
 * 如果无法访问 driver，则忽略（内存数据库会在测试结束后自动销毁）。
 */
actual fun destroyTestDatabase(database: MyHubDatabase) {
    // JS 平台暂时使用空实现，因为无法直接访问 driver
    // 内存数据库会在测试结束后自动销毁
    // 如果需要，可以通过 js() 函数访问 driver，但这不是必需的
}

