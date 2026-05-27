package tech.zhifu.app.myhub.datastore.repository.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.sync.SyncTrigger

enum class SyncStatus { IDLE, RUNNING, FAILED }

interface SyncRepository {
    val json: Json

    fun observeSyncStatus(userId: String): Flow<SyncStatus>
    suspend fun requestSync(userId: String, trigger: SyncTrigger)
    fun startAutoSync(userId: String)
    fun stopAutoSync(userId: String)

    suspend fun insertOutboxAndOpLog(
        outboxId: String,
        oplogId: String,
        userId: String,
        entityType: String,
        entityId: String,
        operation: String,
        payload: String,
        sequence: Long,
        createdAt: String,
        status: String,
        retryCount: Long,
        nextRetryAt: String?,
        lastError: String?
    )
}

