package tech.zhifu.app.myhub.datastore.datasource.sync

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.database.Sync_conflict_log
import tech.zhifu.app.myhub.datastore.database.Sync_oplog
import tech.zhifu.app.myhub.datastore.database.Sync_outbox
import tech.zhifu.app.myhub.datastore.database.Sync_state

object SyncOutboxStatus {
    const val PENDING = "PENDING"
    const val SUCCESS = "SUCCESS"
    const val FAILED = "FAILED"
}

interface LocalSyncDataSource {
    suspend fun getOutboxById(id: String): Sync_outbox?
    suspend fun getOutboxByUserId(userId: String): List<Sync_outbox>
    suspend fun getPendingOutboxByUserId(
        userId: String,
        status: String = SyncOutboxStatus.PENDING
    ): List<Sync_outbox>

    suspend fun getReadyOutboxByUserId(
        userId: String,
        now: String,
        pendingStatus: String = SyncOutboxStatus.PENDING,
        failedStatus: String = SyncOutboxStatus.FAILED
    ): List<Sync_outbox>

    fun observePendingOutboxByUserId(
        userId: String,
        status: String = SyncOutboxStatus.PENDING
    ): Flow<List<Sync_outbox>>

    suspend fun insertOutbox(
        id: String,
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

    suspend fun updateOutboxStatus(
        id: String,
        status: String,
        nextRetryAt: String?,
        lastError: String?
    )

    suspend fun incrementOutboxRetry(
        id: String,
        nextRetryAt: String?,
        lastError: String?
    )

    suspend fun deleteOutboxById(id: String)
    suspend fun deleteOutboxByUserId(userId: String)
    suspend fun deleteOutboxByStatusBefore(
        userId: String,
        status: String,
        beforeAt: String
    )

    suspend fun getSyncState(userId: String, entityType: String): Sync_state?
    suspend fun getSyncStates(userId: String): List<Sync_state>
    suspend fun insertSyncState(
        id: String,
        userId: String,
        entityType: String,
        lastSyncAt: String?,
        lastSyncToken: String?
    )

    suspend fun deleteSyncStateById(id: String)
    suspend fun deleteSyncStatesByUserId(userId: String)

    suspend fun getOpLogsByUserId(userId: String): List<Sync_oplog>
    suspend fun getOpLogsByUserIdAndEntityAfter(
        userId: String,
        entityType: String,
        after: String,
        limit: Long
    ): List<Sync_oplog>

    suspend fun insertOpLog(
        id: String,
        userId: String,
        entityType: String,
        entityId: String,
        operation: String,
        payload: String,
        createdAt: String
    )

    suspend fun deleteOpLogById(id: String)
    suspend fun deleteOpLogsByUserId(userId: String)
    suspend fun deleteOpLogsBefore(userId: String, beforeAt: String)

    suspend fun getConflictLogsByUserId(userId: String): List<Sync_conflict_log>
    suspend fun insertConflictLog(
        id: String,
        userId: String,
        entityType: String,
        entityId: String,
        localPayload: String,
        remotePayload: String,
        resolvedStrategy: String,
        resolvedAt: String
    )

    suspend fun deleteConflictLogById(id: String)
    suspend fun deleteConflictLogsByUserId(userId: String)
}
