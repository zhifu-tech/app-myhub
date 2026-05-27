package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.sync.SyncPullResponse
import tech.zhifu.app.myhub.sync.SyncPushRequest
import tech.zhifu.app.myhub.sync.SyncPushResponse

class SyncService(
    private val syncRepository: SyncRepository
) {
    suspend fun push(request: SyncPushRequest): SyncPushResponse {
        return syncRepository.push(request)
    }

    suspend fun pull(
        userId: String,
        entityType: String,
        sinceToken: String?,
        limit: Int
    ): SyncPullResponse {
        return syncRepository.pull(
            userId = userId,
            entityType = entityType,
            sinceToken = sinceToken,
            limit = limit
        )
    }
}
