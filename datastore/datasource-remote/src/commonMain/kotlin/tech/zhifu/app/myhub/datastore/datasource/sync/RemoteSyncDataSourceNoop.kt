package tech.zhifu.app.myhub.datastore.datasource.sync

import tech.zhifu.app.myhub.sync.SyncOutboxUploadItem
import tech.zhifu.app.myhub.sync.SyncPullResponse
import tech.zhifu.app.myhub.sync.SyncPushResponse

class RemoteSyncDataSourceNoop : RemoteSyncDataSource {
    override suspend fun pullChanges(
        userId: String,
        entityType: String,
        sinceToken: String?,
        limit: Int
    ): SyncPullResponse = SyncPullResponse()

    override suspend fun pushOutbox(
        userId: String,
        items: List<SyncOutboxUploadItem>
    ): SyncPushResponse = SyncPushResponse()
}
