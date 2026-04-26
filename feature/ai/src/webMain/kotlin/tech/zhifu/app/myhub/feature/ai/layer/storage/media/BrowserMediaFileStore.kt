package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import tech.zhifu.app.myhub.datastore.file.storage.copyPlatformPrivateStorage
import tech.zhifu.app.myhub.datastore.file.storage.createPlatformPrivateStorageHandle
import tech.zhifu.app.myhub.datastore.file.storage.deleteStorageHandle
import tech.zhifu.app.myhub.datastore.file.storage.extractPlatformPrivateStorageKeyFromStorageHandle
import tech.zhifu.app.myhub.datastore.file.storage.isExternalStorageHandle
import tech.zhifu.app.myhub.datastore.file.storage.resolveStorageHandleToAccessUrl
import tech.zhifu.app.myhub.datastore.file.storage.storageHandleExists
import tech.zhifu.app.myhub.datastore.file.storage.writePlatformPrivateStorageBytes
import tech.zhifu.app.myhub.feature.ai.layer.common.util.inferMimeType
import kotlin.io.encoding.Base64

internal class BrowserMediaFileStore : MediaFileStore {
    override suspend fun persistDraftMedia(
        draftId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia = storeInPrivateMediaStore(
        key = "ai-capture/drafts/$draftId/media/${mediaId.resolveExtension(source)}",
        source = source,
    )

    override suspend fun importToManagedStorage(
        cardId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia = storeInPrivateMediaStore(
        key = "cards/$cardId/media/${mediaId.resolveExtension(source)}",
        source = source,
    )

    override suspend fun saveGeneratedImage(
        draftId: String,
        mediaId: String,
        bytes: ByteArray,
        mimeType: String,
    ): ImportedMedia {
        val extension = extensionForMimeType(mimeType)
        val key = "ai-capture/generated/$draftId/$mediaId.$extension"
        val accessUrl = writePlatformPrivateStorageBytes(
            key = key,
            bytes = bytes,
            mimeType = mimeType,
        ).orEmpty()
        return ImportedMedia(
            storageHandle = createPlatformPrivateStorageHandle(key),
            accessUrl = accessUrl,
            sizeBytes = bytes.size.toLong(),
        )
    }

    override suspend fun createImageThumbnail(
        source: ImportedMedia,
        quality: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): ImportedMedia? {
        val sourceKey = extractPlatformPrivateStorageKeyFromStorageHandle(source.storageHandle) ?: return null
        val thumbKey = buildThumbKey(sourceKey)
        val accessUrl = copyPlatformPrivateStorage(
            sourceStorageHandle = source.storageHandle,
            targetKey = thumbKey,
        ) ?: return null
        return ImportedMedia(
            storageHandle = createPlatformPrivateStorageHandle(thumbKey),
            accessUrl = accessUrl,
            sizeBytes = source.sizeBytes,
        )
    }

    override suspend fun fileExists(
        storageHandle: String,
        accessUrl: String,
    ): Boolean = when {
        storageHandle.isBlank() && accessUrl.isBlank() -> false
        storageHandle.isNotBlank() -> {
            storageHandleExists(storageHandle)
        }

        else -> accessUrl.isNotBlank()
    }

    override suspend fun deleteIfExists(
        storageHandle: String,
        accessUrl: String,
    ) {
        if (storageHandle.isNotBlank()) {
            deleteStorageHandle(storageHandle)
        }
    }
}


private suspend fun storeInPrivateMediaStore(
    key: String,
    source: MediaImportSource,
): ImportedMedia {
    val normalizedKey = key.trim().trim('/')
    val existingPrivateCopy = source.storageHandle
        .takeUnless(String::isBlank)
        ?.let { copyPlatformPrivateStorage(it, normalizedKey) }
    if (existingPrivateCopy != null) {
        return ImportedMedia(
            storageHandle = createPlatformPrivateStorageHandle(normalizedKey),
            accessUrl = existingPrivateCopy,
            sizeBytes = source.sizeBytes ?: 0L,
        )
    }

    val payload = source.toPersistablePayload()
    if (payload != null) {
        val accessUrl = writePlatformPrivateStorageBytes(
            key = normalizedKey,
            bytes = payload.bytes,
            mimeType = payload.mimeType,
        ).orEmpty()
        return ImportedMedia(
            storageHandle = createPlatformPrivateStorageHandle(normalizedKey),
            accessUrl = accessUrl,
            sizeBytes = payload.bytes.size.toLong(),
        )
    }

    val fallbackHandle = source.storageHandle.ifBlank { source.accessUrl }
    return ImportedMedia(
        storageHandle = fallbackHandle,
        accessUrl = resolveStorageHandleToAccessUrl(fallbackHandle).orEmpty(),
        sizeBytes = source.sizeBytes ?: 0L,
    )
}

private suspend fun MediaImportSource.toPersistablePayload(
): PersistablePayload? {
    platformFile?.let { file ->
        val bytes = runCatching { file.readBytes() }.getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        val mimeType = file.mimeType()?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: inferMimeType(accessUrl.ifBlank { file.toString() })
        return PersistablePayload(bytes = bytes, mimeType = mimeType)
    }
    val raw = accessUrl.trim()
    if (raw.startsWith("data:", ignoreCase = true)) {
        return decodeDataUrl(raw)
    }
    if (isExternalStorageHandle(raw)) {
        return null
    }
    return null
}

private fun String.resolveExtension(
    source: MediaImportSource,
): String {
    val fromAccessUrl = source.accessUrl.substringAfterLast('.', "")
        .takeIf { it.isNotBlank() && it.length <= 8 }
        ?.lowercase()
    if (fromAccessUrl != null) return "$this.$fromAccessUrl"
    val mimeType = source.platformFile?.mimeType()?.toString().orEmpty()
    val extension = extensionForMimeType(mimeType)
    return "$this.$extension"
}

private fun decodeDataUrl(
    dataUrl: String,
): PersistablePayload? {
    val commaIndex = dataUrl.indexOf(',')
    if (commaIndex <= 0) return null
    val metadata = dataUrl.substring(5, commaIndex)
    val payload = dataUrl.substring(commaIndex + 1)
    if (!metadata.contains(";base64", ignoreCase = true)) return null
    val mimeType = metadata.substringBefore(';').ifBlank { "application/octet-stream" }
    val bytes = runCatching { Base64.decode(payload) }.getOrNull() ?: return null
    return PersistablePayload(bytes = bytes, mimeType = mimeType)
}

private fun extensionForMimeType(
    mimeType: String,
): String = when (mimeType.lowercase()) {
    "image/jpeg",
    "image/jpg" -> "jpg"

    "image/webp" -> "webp"
    "image/gif" -> "gif"
    "video/mp4" -> "mp4"
    "video/quicktime" -> "mov"
    "video/webm" -> "webm"
    else -> "bin"
}

private data class PersistablePayload(
    val bytes: ByteArray,
    val mimeType: String,
)

private fun buildThumbKey(
    sourceKey: String,
): String {
    val extension = sourceKey.substringAfterLast('.', "")
    val stem = sourceKey.removeSuffix(if (extension.isBlank()) "" else ".$extension")
    return if (extension.isBlank()) {
        "$stem.thumb"
    } else {
        "$stem.thumb.$extension"
    }
}
