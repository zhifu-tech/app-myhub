package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.sync.SyncOutboxUploadItem
import tech.zhifu.app.myhub.sync.SyncPullResponse
import tech.zhifu.app.myhub.sync.SyncPushResponse

interface RemoteSyncDataSource {
    suspend fun pullChanges(
        userId: String,
        entityType: String,
        sinceToken: String?,
        limit: Int
    ): SyncPullResponse

    suspend fun pushOutbox(
        userId: String,
        items: List<SyncOutboxUploadItem>
    ): SyncPushResponse
}
