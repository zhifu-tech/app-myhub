package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import java.io.File

/**
 * 数据库驱动工厂（服务端）
 * 支持 SQLite 和 PostgreSQL
 */
class DatabaseDriverFactory(private val config: DatabaseConfig) {
    
    fun createDriver(): SqlDriver {
        return when (config.type) {
            DatabaseType.SQLITE -> createSqliteDriver()
            DatabaseType.POSTGRESQL -> createPostgresDriver()
        }
    }
    
    private fun createSqliteDriver(): SqlDriver {
        val path = config.path ?: ".myhub/myhub.db"
        val databaseFile = File(path)
        
        // 确保目录存在
        if (databaseFile.parentFile != null && !databaseFile.parentFile.exists()) {
            databaseFile.parentFile.mkdirs()
        }
        
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databaseFile.absolutePath}")
        
        // 检查数据库文件是否已存在
        val databaseExists = databaseFile.exists()
        
        if (!databaseExists) {
            // 数据库文件不存在，创建新数据库和表
            MyHubDatabase.Schema.synchronous().create(driver)
        }
        
        // 启用外键约束
        driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
        
        return driver
    }
    
    private fun createPostgresDriver(): SqlDriver {
        val host = config.host ?: "localhost"
        val port = config.port ?: 5432
        val database = config.database ?: "myhub"
        val username = config.username ?: "postgres"
        val password = config.password ?: "postgres"
        
        val url = "jdbc:postgresql://$host:$port/$database"
        
        // 加载 PostgreSQL 驱动
        Class.forName("org.postgresql.Driver")
        
        // SQLDelight 的 JdbcSqliteDriver 实际上可以用于任何 JDBC 数据库
        // 我们只需要提供正确的 JDBC URL
        val driver = JdbcSqliteDriver(url)
        
        // 创建数据库 schema（如果不存在）
        try {
            MyHubDatabase.Schema.synchronous().create(driver)
        } catch (e: Exception) {
            // 如果表已存在，忽略错误
            // PostgreSQL 可能需要手动创建表，这里只是尝试
        }
        
        return driver
    }
}


