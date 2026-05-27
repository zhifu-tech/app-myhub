package tech.zhifu.app.myhub.sync

data class SyncPullChange(
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: String,
    val updatedAt: String
)

fun SyncPullChange.toSyncOperation() = SyncOperations.entries.find {
    operation == it.value
}

fun SyncPullChange.toSyncEntityType() = SyncEntityType.entries.firstOrNull {
    entityType == it.value
}
