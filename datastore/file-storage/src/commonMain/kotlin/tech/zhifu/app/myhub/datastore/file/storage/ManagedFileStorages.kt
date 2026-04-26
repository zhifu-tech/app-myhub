package tech.zhifu.app.myhub.datastore.file.storage

fun isExternalStorageHandle(
    storageHandle: String,
): Boolean {
    val raw = storageHandle.trim()
    if (raw.isBlank()) return false
    return raw.startsWith("http://", ignoreCase = true) ||
        raw.startsWith("https://", ignoreCase = true) ||
        raw.startsWith("content://", ignoreCase = true) ||
        raw.startsWith("blob:", ignoreCase = true) ||
        raw.startsWith("data:", ignoreCase = true) ||
        raw.startsWith("file://", ignoreCase = true)
}

fun storageHandleFromAccessUrl(
    accessUrl: String,
): String {
    val raw = accessUrl.trim()
    if (raw.isBlank()) return raw
    if (isExternalStorageHandle(raw)) return raw
    val key = extractPlatformPrivateStorageKeyFromAccessUrl(raw) ?: return raw
    return createPlatformPrivateStorageHandle(key)
}

suspend fun resolveStorageHandleToAccessUrl(
    storageHandle: String,
): String? {
    val raw = storageHandle.trim()
    if (raw.isBlank()) return null
    if (isExternalStorageHandle(raw)) return raw
    return resolvePlatformPrivateStorageHandleToAccessUrl(raw)
}

suspend fun storageHandleExists(
    storageHandle: String,
): Boolean {
    val raw = storageHandle.trim()
    if (raw.isBlank()) return false
    if (isExternalStorageHandle(raw)) return true
    return platformPrivateStorageHandleExists(raw)
}

suspend fun deleteStorageHandle(
    storageHandle: String,
) {
    val raw = storageHandle.trim()
    if (raw.isBlank()) return
    if (isExternalStorageHandle(raw)) return
    deletePlatformPrivateStorageHandle(raw)
}
