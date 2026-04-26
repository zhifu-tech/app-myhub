package tech.zhifu.app.myhub.datastore.file.storage

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual fun createPlatformPrivateStorageHandle(
    key: String,
): String = encodeWebStorageHandle(
    WebStorageHandle.BrowserStoredAsset(key = normalizeWebStorageKey(key))
)

actual fun extractPlatformPrivateStorageKeyFromStorageHandle(
    storageHandle: String,
): String? = decodeWebStorageHandle(storageHandle)
    ?.let { handle ->
        when (handle) {
            is WebStorageHandle.BrowserStoredAsset -> handle.key
        }
    }

actual fun extractPlatformPrivateStorageKeyFromAccessUrl(
    accessUrl: String,
): String? = null

actual suspend fun resolvePlatformPrivateStorageHandleToAccessUrl(
    storageHandle: String,
): String? {
    val key = extractPlatformPrivateStorageKeyFromStorageHandle(storageHandle) ?: return null
    return resolveWebPrivateMediaAccessUrl(key)
}

@OptIn(ExperimentalEncodingApi::class)
actual suspend fun writePlatformPrivateStorageBytes(
    key: String,
    bytes: ByteArray,
    mimeType: String,
): String? = writeWebPrivateMediaBytes(
    key = normalizeWebStorageKey(key),
    base64 = Base64.encode(bytes),
    mimeType = mimeType,
)

actual suspend fun copyPlatformPrivateStorage(
    sourceStorageHandle: String,
    targetKey: String,
): String? {
    val sourceKey = extractPlatformPrivateStorageKeyFromStorageHandle(sourceStorageHandle) ?: return null
    return copyWebPrivateMediaToKey(
        sourceKey = sourceKey,
        targetKey = normalizeWebStorageKey(targetKey),
    )
}

actual suspend fun platformPrivateStorageHandleExists(
    storageHandle: String,
): Boolean {
    val key = extractPlatformPrivateStorageKeyFromStorageHandle(storageHandle) ?: return false
    return webPrivateMediaExists(key)
}

actual suspend fun deletePlatformPrivateStorageHandle(
    storageHandle: String,
) {
    val key = extractPlatformPrivateStorageKeyFromStorageHandle(storageHandle) ?: return
    deleteWebPrivateMediaByKey(key)
}

internal expect suspend fun resolveWebPrivateMediaAccessUrl(
    key: String,
): String?

internal expect suspend fun writeWebPrivateMediaBytes(
    key: String,
    base64: String,
    mimeType: String,
): String?

internal expect suspend fun copyWebPrivateMediaToKey(
    sourceKey: String,
    targetKey: String,
): String?

internal expect suspend fun webPrivateMediaExists(
    key: String,
): Boolean

internal expect suspend fun deleteWebPrivateMediaByKey(
    key: String,
)
