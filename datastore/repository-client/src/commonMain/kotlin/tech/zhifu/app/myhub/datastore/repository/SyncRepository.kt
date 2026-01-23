package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange
import tech.zhifu.app.myhub.sync.SyncPullResponse
import tech.zhifu.app.myhub.sync.SyncPushRequest
import tech.zhifu.app.myhub.sync.SyncPushResponse
import tech.zhifu.app.myhub.sync.SyncTrigger

enum class SyncStatus { IDLE, RUNNING, FAILED }

interface SyncRepository {
    fun observeSyncStatus(userId: String): Flow<SyncStatus>
    suspend fun requestSync(userId: String, trigger: SyncTrigger)
    fun startAutoSync(userId: String)
    fun stopAutoSync(userId: String)
    suspend fun push(request: SyncPushRequest): SyncPushResponse
    suspend fun pull(
        userId: String,
        entityType: String,
        sinceToken: String?,
        limit: Int
    ): SyncPullResponse
}

interface SyncChangeApplier {
    suspend fun applyChanges(
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    )
}

