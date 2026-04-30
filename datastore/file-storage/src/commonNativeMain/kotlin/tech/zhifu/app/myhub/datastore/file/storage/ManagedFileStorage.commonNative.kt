package tech.zhifu.app.myhub.datastore.file.storage

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write

actual fun createPlatformPrivateStorageHandle(
    key: String,
): String = NativeStorageHandle.ManagedAsset(
    key = key.normalize()
).encodeToString()

actual fun extractPlatformPrivateStorageKeyFromStorageHandle(
    storageHandle: String,
): String? = storageHandle.decodeToNativeStorageHandle()
    ?.let { handle ->
        when (handle) {
            is NativeStorageHandle.ManagedAsset -> handle.key
        }
    }

actual fun extractPlatformPrivateStorageKeyFromAccessUrl(
    accessUrl: String,
): String? = nativePrivateKeyFromAccessUrl(accessUrl)

actual suspend fun resolvePlatformPrivateStorageHandleToAccessUrl(
    storageHandle: String,
): String? = extractPlatformPrivateStorageKeyFromStorageHandle(
    storageHandle
)
    ?.let { key -> (platformManagedAppDataDir() / key).absolutePath() }

actual suspend fun writePlatformPrivateStorageBytes(
    key: String,
    bytes: ByteArray,
    mimeType: String,
): String? {
    val normalizedKey = key.normalize()
    if (normalizedKey.isBlank()) return null
    val file = platformManagedAppDataDir() / normalizedKey
    file.parent()?.createDirectories()
    file.write(bytes)
    return file.absolutePath()
}

actual suspend fun copyPlatformPrivateStorage(
    sourceStorageHandle: String,
    targetKey: String,
): String? {
    val sourceKey = extractPlatformPrivateStorageKeyFromStorageHandle(
        sourceStorageHandle
    ) ?: return null
    val normalizedTargetKey =
        targetKey.normalize()
    if (normalizedTargetKey.isBlank()) return null
    val source = platformManagedAppDataDir() / sourceKey
    if (!source.exists()) return null
    val target = platformManagedAppDataDir() / normalizedTargetKey
    target.parent()?.createDirectories()
    target.write(source.readBytes())
    return target.absolutePath()
}

actual suspend fun platformPrivateStorageHandleExists(
    storageHandle: String,
): Boolean = extractPlatformPrivateStorageKeyFromStorageHandle(
    storageHandle
)
    ?.let { key -> (platformManagedAppDataDir() / key).exists() }
    ?: false

actual suspend fun deletePlatformPrivateStorageHandle(
    storageHandle: String,
) {
    val key = extractPlatformPrivateStorageKeyFromStorageHandle(
        storageHandle
    ) ?: return
    val file = platformManagedAppDataDir() / key
    if (file.exists()) {
        file.delete(mustExist = false)
    }
}

internal expect fun platformManagedAppDataDir(): PlatformFile
