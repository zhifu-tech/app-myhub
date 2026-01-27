package tech.zhifu.app.myhub.datastore.repository.store

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 内存实现的 Bookkeeper 数据存储
 *
 * 用于追踪失败的同步操作时间戳。
 * 这是一个简单的内存实现，适合快速验证 Store5 功能。
 *
 * 注意：应用重启后数据会丢失，生产环境建议使用 DatabaseBookkeeperStorage
 */
class InMemoryBookkeeperStorage : BookkeeperStorage {
    private val mutex = Mutex()
    private val failedSyncs = mutableMapOf<String, Long>()

    override suspend fun getLastFailedSync(key: String): Long? = mutex.withLock {
        failedSyncs[key]
    }

    override suspend fun setLastFailedSync(key: String, timestamp: Long): Boolean = mutex.withLock {
        failedSyncs[key] = timestamp
        true
    }

    override suspend fun clearFailedSync(key: String): Boolean = mutex.withLock {
        failedSyncs.remove(key) != null
    }

    override suspend fun clearAllFailedSyncs(): Boolean = mutex.withLock {
        failedSyncs.clear()
        true
    }
}
