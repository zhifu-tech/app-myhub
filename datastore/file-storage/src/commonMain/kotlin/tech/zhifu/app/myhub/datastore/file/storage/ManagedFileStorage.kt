package tech.zhifu.app.myhub.datastore.file.storage

expect fun createPlatformPrivateStorageHandle(
    key: String,
): String

expect fun extractPlatformPrivateStorageKeyFromStorageHandle(
    storageHandle: String,
): String?

internal expect fun extractPlatformPrivateStorageKeyFromAccessUrl(
    accessUrl: String,
): String?

expect suspend fun resolvePlatformPrivateStorageHandleToAccessUrl(
    storageHandle: String,
): String?

expect suspend fun writePlatformPrivateStorageBytes(
    key: String,
    bytes: ByteArray,
    mimeType: String,
): String?

expect suspend fun copyPlatformPrivateStorage(
    sourceStorageHandle: String,
    targetKey: String,
): String?

expect suspend fun platformPrivateStorageHandleExists(
    storageHandle: String,
): Boolean

expect suspend fun deletePlatformPrivateStorageHandle(
    storageHandle: String,
)

