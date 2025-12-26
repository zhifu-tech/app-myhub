package tech.zhifu.app.myhub.datastore.database

/**
 * 数据库类型
 */
enum class DatabaseType {
    SQLITE,
    POSTGRESQL
}

/**
 * 数据库配置
 */
data class DatabaseConfig(
    val type: DatabaseType,
    val host: String? = null,
    val port: Int? = null,
    val database: String? = null,
    val username: String? = null,
    val password: String? = null,
    val path: String? = null // SQLite 文件路径
) {
    companion object {
        /**
         * 从环境变量创建数据库配置
         */
        fun fromEnvironment(): DatabaseConfig {
            val dbType = System.getenv("DB_TYPE")?.uppercase() ?: "SQLITE"
            val type = try {
                DatabaseType.valueOf(dbType)
            } catch (e: Exception) {
                DatabaseType.SQLITE
            }

            return when (type) {
                DatabaseType.POSTGRESQL -> DatabaseConfig(
                    type = DatabaseType.POSTGRESQL,
                    host = System.getenv("DB_HOST") ?: "localhost",
                    port = System.getenv("DB_PORT")?.toIntOrNull() ?: 5432,
                    database = System.getenv("DB_NAME") ?: "myhub",
                    username = System.getenv("DB_USER") ?: "postgres",
                    password = System.getenv("DB_PASSWORD") ?: "postgres"
                )
                DatabaseType.SQLITE -> DatabaseConfig(
                    type = DatabaseType.SQLITE,
                    path = System.getenv("DB_PATH") ?: ".myhub/myhub.db"
                )
            }
        }
    }
}


