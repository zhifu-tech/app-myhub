package tech.zhifu.app.myhub.sync

enum class SyncOperations(val value: String) {
    Unknown("UNKNOWN"),
    Insert("INSERT"),
    Delete("DELETE"),
}

fun String.toSyncOperation() = when (this) {
    SyncOperations.Insert.value -> SyncOperations.Insert
    SyncOperations.Delete.value -> SyncOperations.Delete
    else -> SyncOperations.Unknown
}

fun SyncPullChange.toSyncEntityType() = when (entityType) {
    SyncEntityType.User.value -> SyncEntityType.User
    SyncEntityType.UserPreferences.value -> SyncEntityType.UserPreferences
    SyncEntityType.Card.value -> SyncEntityType.Card
    SyncEntityType.Tag.value -> SyncEntityType.Tag
    SyncEntityType.Collection.value -> SyncEntityType.Collection
    SyncEntityType.Template.value -> SyncEntityType.Template
    else -> SyncEntityType.Unknown
}
