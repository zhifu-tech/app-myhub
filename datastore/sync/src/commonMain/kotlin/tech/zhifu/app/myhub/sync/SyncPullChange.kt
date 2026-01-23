package tech.zhifu.app.myhub.sync

data class SyncPullChange(
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: String,
    val updatedAt: String
)
