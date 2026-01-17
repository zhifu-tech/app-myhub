package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlin.random.Random

/**
 * iOS 平台测试数据库创建实现
 *
 * 使用 NativeSqliteDriver 创建内存数据库，适用于 iOS 模拟器和真机测试。
 * 为每个测试生成唯一的内存数据库名称（`:memory:test_${随机数}`），确保每个测试都是独立的。
 * 内存数据库不会持久化，连接关闭后自动销毁。
 *
 * 注意：
 * - SQLite 的内存数据库必须使用 `:memory:` 前缀
 * - 使用不同的名称（如 `:memory:test1`, `:memory:test2`）会创建独立的内存数据库
 * - 使用随机数确保每个测试都有唯一的数据库名称，提供额外的隔离保障
 * - NativeSqliteDriver 在传入 schema 参数时会自动创建表，不需要显式调用 create()。
 * - 这与 JdbcSqliteDriver 不同，JdbcSqliteDriver 需要显式调用 create()。
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    // 为每个测试生成唯一的内存数据库名称，确保测试隔离
    val driver = NativeSqliteDriver(
        schema = MyHubDatabase.Schema.synchronous(),
        // 使用 `:memory:test_${随机数}` 创建独立的内存数据库
        name = ":memory:test_${Random.nextLong()}"
    )
    driver.execute(
        identifier = null,
        sql = "PRAGMA foreign_keys = ON;",
        parameters = 0
    )
    return MyHubDatabase(driver)
}

/**
 * iOS 平台：尝试关闭 driver
 *
 * 注意：
 * - Kotlin/Native 不支持 Java 反射，无法像 JVM 平台那样访问 driver 属性
 * - 由于每个连接使用 `:memory:` 都会创建独立的内存数据库，即使 driver 没有被显式关闭，
 *   每个测试的连接也是独立的，不会相互影响
 * - 如果将来 SQLDelight 提供了更好的 API 来访问 driver，可以在这里实现关闭逻辑
 */
actual fun destroyTestDatabase(database: MyHubDatabase) {
    // iOS 平台：Kotlin/Native 不支持反射，无法直接访问 driver
    // 由于每个连接使用 `:memory:` 都会创建独立的内存数据库，测试之间天然隔离
    // 如果将来有更好的方式访问 driver，可以在这里实现关闭逻辑
}

