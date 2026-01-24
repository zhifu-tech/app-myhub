package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.Sync_conflict_log
import tech.zhifu.app.myhub.datastore.database.Sync_oplog
import tech.zhifu.app.myhub.datastore.database.Sync_outbox
import tech.zhifu.app.myhub.datastore.database.Sync_state
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource

class LocalSyncDataSourceImpl(
    private val database: MyHubDatabase,
) : LocalSyncDataSource {

    override suspend fun getOutboxById(id: String): Sync_outbox? {
        return database.sync_outboxQueries
            .selectOutboxById(id)
            .awaitAsOneOrNull()
    }

    override suspend fun getOutboxByUserId(userId: String): List<Sync_outbox> {
        return database.sync_outboxQueries
            .selectOutboxByUserId(userId)
            .awaitAsList()
    }

    override suspend fun getPendingOutboxByUserId(
        userId: String,
        status: String
    ): List<Sync_outbox> {
        return database.sync_outboxQueries
            .selectPendingOutboxByUserId(userId, status)
            .awaitAsList()
    }

    override suspend fun getReadyOutboxByUserId(
        userId: String,
        now: String,
        pendingStatus: String,
        failedStatus: String
    ): List<Sync_outbox> {
        return database.sync_outboxQueries
            .selectReadyOutboxByUserId(userId, pendingStatus, failedStatus, now)
            .awaitAsList()
    }

    override fun observePendingOutboxByUserId(
        userId: String,
        status: String
    ): Flow<List<Sync_outbox>> {
        return database.sync_outboxQueries
            .selectPendingOutboxByUserId(userId, status)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override suspend fun insertOutbox(
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
    ) {
        database.sync_outboxQueries.insertOutbox(
            id = id,
            user_id = userId,
            entity_type = entityType,
            entity_id = entityId,
            operation = operation,
            payload = payload,
            sequence = sequence,
            created_at = createdAt,
            status = status,
            retry_count = retryCount,
            next_retry_at = nextRetryAt,
            last_error = lastError
        )
    }

    override suspend fun insertOutboxAndOpLog(
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
    ) {
        database.transaction {
            database.sync_outboxQueries.insertOutbox(
                id = outboxId,
                user_id = userId,
                entity_type = entityType,
                entity_id = entityId,
                operation = operation,
                payload = payload,
                sequence = sequence,
                created_at = createdAt,
                status = status,
                retry_count = retryCount,
                next_retry_at = nextRetryAt,
                last_error = lastError
            )
            database.sync_oplogQueries.insertOpLog(
                id = oplogId,
                user_id = userId,
                entity_type = entityType,
                entity_id = entityId,
                operation = operation,
                payload = payload,
                created_at = createdAt
            )
        }
    }

    override suspend fun updateOutboxStatus(
        id: String,
        status: String,
        nextRetryAt: String?,
        lastError: String?
    ) {
        database.sync_outboxQueries.updateOutboxStatus(
            status = status,
            next_retry_at = nextRetryAt,
            last_error = lastError,
            id = id
        )
    }

    override suspend fun incrementOutboxRetry(
        id: String,
        nextRetryAt: String?,
        lastError: String?
    ) {
        database.sync_outboxQueries.incrementOutboxRetry(
            next_retry_at = nextRetryAt,
            last_error = lastError,
            id = id
        )
    }

    override suspend fun deleteOutboxById(id: String) {
        database.sync_outboxQueries.deleteOutboxById(id)
    }

    override suspend fun deleteOutboxByUserId(userId: String) {
        database.sync_outboxQueries.deleteOutboxByUserId(userId)
    }

    override suspend fun deleteOutboxByStatusBefore(
        userId: String,
        status: String,
        beforeAt: String
    ) {
        database.sync_outboxQueries.deleteOutboxByStatusBefore(
            user_id = userId,
            status = status,
            created_at = beforeAt
        )
    }

    override suspend fun getSyncState(userId: String, entityType: String): Sync_state? {
        return database.sync_stateQueries
            .selectSyncStateByUserIdAndEntity(userId, entityType)
            .awaitAsOneOrNull()
    }

    override suspend fun getSyncStates(userId: String): List<Sync_state> {
        return database.sync_stateQueries
            .selectSyncStatesByUserId(userId)
            .awaitAsList()
    }

    override suspend fun insertSyncState(
        id: String,
        userId: String,
        entityType: String,
        lastSyncAt: String?,
        lastSyncToken: String?
    ) {
        database.sync_stateQueries.insertSyncState(
            id = id,
            user_id = userId,
            entity_type = entityType,
            last_sync_at = lastSyncAt,
            last_sync_token = lastSyncToken
        )
    }

    override suspend fun deleteSyncStateById(id: String) {
        database.sync_stateQueries.deleteSyncStateById(id)
    }

    override suspend fun deleteSyncStatesByUserId(userId: String) {
        database.sync_stateQueries.deleteSyncStatesByUserId(userId)
    }

    override suspend fun getOpLogsByUserId(userId: String): List<Sync_oplog> {
        return database.sync_oplogQueries
            .selectOpLogsByUserId(userId)
            .awaitAsList()
    }

    override suspend fun getOpLogsByUserIdAndEntityAfter(
        userId: String,
        entityType: String,
        after: String,
        limit: Long
    ): List<Sync_oplog> {
        return database.sync_oplogQueries
            .selectOpLogsByUserIdAndEntityAfter(userId, entityType, after, limit)
            .awaitAsList()
    }

    override suspend fun insertOpLog(
        id: String,
        userId: String,
        entityType: String,
        entityId: String,
        operation: String,
        payload: String,
        createdAt: String
    ) {
        database.sync_oplogQueries.insertOpLog(
            id = id,
            user_id = userId,
            entity_type = entityType,
            entity_id = entityId,
            operation = operation,
            payload = payload,
            created_at = createdAt
        )
    }

    override suspend fun deleteOpLogById(id: String) {
        database.sync_oplogQueries.deleteOpLogById(id)
    }

    override suspend fun deleteOpLogsByUserId(userId: String) {
        database.sync_oplogQueries.deleteOpLogsByUserId(userId)
    }

    override suspend fun deleteOpLogsBefore(userId: String, beforeAt: String) {
        database.sync_oplogQueries.deleteOpLogsBefore(
            user_id = userId,
            created_at = beforeAt
        )
    }

    override suspend fun getConflictLogsByUserId(userId: String): List<Sync_conflict_log> {
        return database.sync_conflict_logQueries
            .selectConflictLogsByUserId(userId)
            .awaitAsList()
    }

    override suspend fun insertConflictLog(
        id: String,
        userId: String,
        entityType: String,
        entityId: String,
        localPayload: String,
        remotePayload: String,
        resolvedStrategy: String,
        resolvedAt: String
    ) {
        database.sync_conflict_logQueries.insertConflictLog(
            id = id,
            user_id = userId,
            entity_type = entityType,
            entity_id = entityId,
            local_payload = localPayload,
            remote_payload = remotePayload,
            resolved_strategy = resolvedStrategy,
            resolved_at = resolvedAt
        )
    }

    override suspend fun deleteConflictLogById(id: String) {
        database.sync_conflict_logQueries.deleteConflictLogById(id)
    }

    override suspend fun deleteConflictLogsByUserId(userId: String) {
        database.sync_conflict_logQueries.deleteConflictLogsByUserId(userId)
    }
}
