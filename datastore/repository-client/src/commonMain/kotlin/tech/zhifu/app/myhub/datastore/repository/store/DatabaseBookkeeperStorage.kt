package tech.zhifu.app.myhub.datastore.repository.store

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

/**
 * 数据库实现的 Bookkeeper 数据存储
 *
 * 使用 SQLDelight 持久化存储失败的同步操作时间戳。
 * 应用重启后数据仍然保留，适合生产环境使用。
 *
 * 优势：
 * - 持久化：应用重启后不会丢失待重试的同步记录
 * - 可靠：使用 SQLite 事务保证数据一致性
 * - 可查询：可以在调试时查看数据库内容
 */
class DatabaseBookkeeperStorage(
    private val database: MyHubDatabase
) : BookkeeperStorage {

    override suspend fun getLastFailedSync(key: String): Long? {
        return database.bookkeeperQueries
            .selectByKey(key)
            .awaitAsOneOrNull()
    }

    override suspend fun setLastFailedSync(key: String, timestamp: Long): Boolean {
        return try {
            database.bookkeeperQueries.insertOrReplace(key, timestamp)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearFailedSync(key: String): Boolean {
        return try {
            database.bookkeeperQueries.deleteByKey(key)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearAllFailedSyncs(): Boolean {
        return try {
            database.bookkeeperQueries.deleteAll()
            true
        } catch (e: Exception) {
            false
        }
    }
}
