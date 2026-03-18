package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.sync.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.sync.SyncPullChange
import tech.zhifu.app.myhub.sync.SyncPullResponse
import tech.zhifu.app.myhub.sync.SyncPushRequest
import tech.zhifu.app.myhub.sync.SyncPushResponse

class SyncRepositoryImpl(
    private val localSyncDataSource: LocalSyncDataSource
) : SyncRepository {

    override suspend fun push(request: SyncPushRequest): SyncPushResponse {
        val accepted = mutableListOf<String>()
        val failed = mutableMapOf<String, String>()
        request.items.forEach { item ->
            runCatching {
                localSyncDataSource.insertOpLog(
                    id = item.id,
                    userId = request.userId,
                    entityType = item.entityType,
                    entityId = item.entityId,
                    operation = item.operation,
                    payload = item.payload,
                    createdAt = item.createdAt
                )
                accepted.add(item.id)
            }.onFailure { error ->
                failed[item.id] = error.message ?: "insert failed"
            }
        }
        return SyncPushResponse(acceptedIds = accepted, failed = failed)
    }

    override suspend fun pull(
        userId: String,
        entityType: String,
        sinceToken: String?,
        limit: Int
    ): SyncPullResponse {
        val after = sinceToken ?: ""
        val rows = localSyncDataSource.getOpLogsByUserIdAndEntityAfter(
            userId = userId,
            entityType = entityType,
            after = after,
            limit = limit.toLong()
        )
        val changes = rows.map { row ->
            SyncPullChange(
                entityType = row.entity_type,
                entityId = row.entity_id,
                operation = row.operation,
                payload = row.payload,
                updatedAt = row.created_at
            )
        }
        val nextToken = rows.lastOrNull()?.created_at
        return SyncPullResponse(
            changes = changes,
            nextToken = nextToken,
            fullSnapshot = false
        )
    }
}
