package tech.zhifu.app.myhub.datastore.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import tech.zhifu.app.myhub.getPlatform

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
 * 销毁测试数据库
 *
 * 直接关闭数据库驱动，对于内存数据库来说，这会销毁整个数据库实例。
 * 这比手动删除所有数据更简单、更高效。
 *
 * 各平台需要提供具体实现，因为访问 driver 的方式可能不同。
 */
expect fun destroyTestDatabase(database: MyHubDatabase)

/**
 * 检查当前平台是否为 WASM
 *
 * WASM 平台由于 JS interop 限制，无法可靠地访问 SQLDelight Driver 实例。
 * 因此数据库测试在 WASM 平台会被跳过。
 */
private fun isWasmPlatform(): Boolean {
    return getPlatform().name.contains("Wasm", ignoreCase = true)
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
    // WASM 平台跳过数据库测试
    if (isWasmPlatform()) {
        println("Skipping database test on WASM platform")
        return@runTest
    }

    val db = createTestDatabase()

    try {
        block(db)
    } finally {
        // 测试结束后销毁数据库（关闭驱动）
        destroyTestDatabase(db)
    }
}

