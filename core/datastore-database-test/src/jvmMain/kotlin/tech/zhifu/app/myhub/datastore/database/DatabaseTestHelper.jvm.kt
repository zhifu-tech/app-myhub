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
        // SQLDelight 生成的数据库类可能有不同的字段名，尝试多个可能的名称
        val possibleFieldNames = listOf("driver", "driver\$delegate", "\$driver")
        
        var driver: SqlDriver? = null
        for (fieldName in possibleFieldNames) {
            try {
                val driverField = database.javaClass.getDeclaredField(fieldName)
                driverField.isAccessible = true
                val fieldValue = driverField.get(database)
                if (fieldValue is SqlDriver) {
                    driver = fieldValue
                    break
                }
            } catch (e: NoSuchFieldException) {
                // 继续尝试下一个字段名
                continue
            }
        }
        
        // 如果直接字段访问失败，尝试通过所有字段查找
        if (driver == null) {
            val fields = database.javaClass.declaredFields
            for (field in fields) {
                if (SqlDriver::class.java.isAssignableFrom(field.type)) {
                    field.isAccessible = true
                    val fieldValue = field.get(database)
                    if (fieldValue is SqlDriver) {
                        driver = fieldValue
                        break
                    }
                }
            }
        }
        
        driver?.close()
    } catch (e: Exception) {
        // 如果反射失败，忽略错误（测试已经结束）
        // 对于内存数据库，连接关闭后会自动销毁
        println("destroyTestDatabase error: $e") // 注释掉以避免测试输出噪音
    }
}

