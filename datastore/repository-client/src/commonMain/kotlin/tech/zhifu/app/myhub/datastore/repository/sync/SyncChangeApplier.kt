package tech.zhifu.app.myhub.datastore.repository.sync

import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

interface SyncChangeApplier {
    suspend fun applyChanges(
        userid: String,
        entity: SyncEntityType,
        operations: SyncOperations,
        change: SyncPullChange
    )
}
