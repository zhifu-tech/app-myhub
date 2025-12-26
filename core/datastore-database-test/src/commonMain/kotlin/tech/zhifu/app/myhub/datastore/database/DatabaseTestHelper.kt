package tech.zhifu.app.myhub.datastore.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest

/**
 * 创建测试数据库
 *
 * 各平台需要提供具体实现，创建一个内存数据库实例用于单元测试。
 * 该数据库会在每个测试用例运行前创建，测试结束后自动清理。
 *
 * 注意：
 * - 数据库应该是内存数据库，不会持久化数据
 * - 必须启用外键约束（PRAGMA foreign_keys = ON）以支持级联删除等操作
 * - 每个测试用例都会获得一个全新的数据库实例
 */
expect suspend fun createTestDatabase(): MyHubDatabase

/**
 * 清空测试数据库
 *
 * 删除测试数据库中的所有数据，用于测试清理。
 * 按照外键依赖关系的顺序删除数据，避免违反约束。
 */
private suspend fun clearTestDatabase(database: MyHubDatabase) {
    database.transaction {
        database.cardQueries.deleteAll()
        database.tagQueries.deleteAll()
        database.userQueries.deleteAll()
        database.templateQueries.deleteAll()
        database.statisticsQueries.deleteAll()
    }
}

/**
 * 运行数据库测试
 *
 * 为每个测试用例提供独立的数据库实例，测试结束后自动清理。
 *
 * 注意：
 * 1.  JS 没有"阻塞线程"这回事； 2. @BeforeTest 在 JS 是"fire-and-forget"
 * 所以在 Kotlin/JS 测试环境中，@BeforeTest 里的 runTest {} 不会被测试框架等待完成
 *
 * 使用示例：
 * ```kotlin
 * @Test
 * fun `test insert card`() = runDatabaseTest { database ->
 *     val dataSource = LocalCardDataSourceImpl(database)
 *     // 测试代码...
 * }
 * ```
 *
 */
fun runDatabaseTest(
    block: suspend CoroutineScope.(MyHubDatabase) -> Unit
) = runTest {
    val db = createTestDatabase()

    block(db)

    clearTestDatabase(db)
}

