package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.sync.SyncPullResponse
import tech.zhifu.app.myhub.sync.SyncPushRequest
import tech.zhifu.app.myhub.sync.SyncPushResponse

interface SyncRepository {
    suspend fun push(request: SyncPushRequest): SyncPushResponse
    suspend fun pull(
        userId: String,
        entityType: String,
        sinceToken: String?,
        limit: Int
    ): SyncPullResponse
}
