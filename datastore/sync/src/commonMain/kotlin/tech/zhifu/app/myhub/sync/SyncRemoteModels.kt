package tech.zhifu.app.myhub.sync

data class SyncOutboxUploadItem(
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: String,
    val sequence: Long,
    val createdAt: String
)

data class SyncPushRequest(
    val userId: String,
    val items: List<SyncOutboxUploadItem>
)

data class SyncPushResponse(
    val acceptedIds: List<String> = emptyList(),
    val failed: Map<String, String> = emptyMap()
)

data class SyncPullResponse(
    val changes: List<SyncPullChange> = emptyList(),
    val nextToken: String? = null,
    val fullSnapshot: Boolean = false
)
