package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import io.ktor.http.decodeURLPart
import tech.zhifu.app.myhub.component.media.extensionToMimeType
import tech.zhifu.app.myhub.component.media.mimeTypeToExtension
import tech.zhifu.app.myhub.datastore.file.storage.createPlatformPrivateStorageHandle
import tech.zhifu.app.myhub.datastore.file.storage.extractPlatformPrivateStorageKeyFromStorageHandle
import tech.zhifu.app.myhub.datastore.file.storage.resolveStorageHandleToAccessUrl
import tech.zhifu.app.myhub.datastore.file.storage.storageHandleFromAccessUrl
import tech.zhifu.app.myhub.datastore.file.storage.writePlatformPrivateStorageBytes

internal class NativeMediaFileStore : MediaFileStore {
    override suspend fun persistDraftMedia(
        draftId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia = importMedia(
        source = source,
        relativeDir = "ai-capture/drafts/$draftId/media",
        mediaId = mediaId,
    )

    override suspend fun importToManagedStorage(
        cardId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia = importMedia(
        source = source,
        relativeDir = "cards/$cardId/media",
        mediaId = mediaId,
    )

    override suspend fun saveGeneratedImage(
        draftId: String,
        mediaId: String,
        bytes: ByteArray,
        mimeType: String,
    ): ImportedMedia {
        val extension = mimeType.mimeTypeToExtension()
        val relativeKey = "ai-capture/generated/$draftId/$mediaId.$extension"
        val accessUrl = writePlatformPrivateStorageBytes(
            key = relativeKey,
            bytes = bytes,
            mimeType = mimeType,
        ).orEmpty()
        return ImportedMedia(
            storageHandle = createPlatformPrivateStorageHandle(relativeKey),
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
        val sourcePath = normalizeAccessUrl(source.accessUrl)
        val sourceFile = PlatformFile(sourcePath)
        if (!sourceFile.exists()) return null
        val parent = sourceFile.parent() ?: return null
        parent / "${sourceFile.nameWithoutExtension}.thumb.jpg"
        val bytes = sourceFile.withScopedAccessSuspend {
            FileKit.compressImage(
                file = sourceFile,
                quality = quality,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }
        val thumbAccessUrl = writePlatformPrivateStorageBytes(
            key = buildThumbKey(source.storageHandle),
            bytes = bytes,
            mimeType = "image/jpeg",
        ).orEmpty()
        return ImportedMedia(
            storageHandle = storageHandleFromAccessUrl(thumbAccessUrl),
            accessUrl = thumbAccessUrl,
            sizeBytes = bytes.size.toLong(),
        )
    }

    override suspend fun fileExists(
        storageHandle: String,
        accessUrl: String,
    ): Boolean = runCatching {
        val raw = accessUrl.ifBlank { storageHandle }
        PlatformFile(normalizeAccessUrl(raw)).exists()
    }.getOrDefault(false)

    override suspend fun deleteIfExists(
        storageHandle: String,
        accessUrl: String,
    ) {
        runCatching {
            val raw = accessUrl.ifBlank { storageHandle }
            val file = PlatformFile(normalizeAccessUrl(raw))
            if (file.exists()) {
                file.delete(mustExist = false)
            }
        }
    }
}

private suspend fun importMedia(
    source: MediaImportSource,
    relativeDir: String,
    mediaId: String,
): ImportedMedia {
    val sourceFile = source.platformFile ?: PlatformFile(normalizeAccessUrl(source.accessUrl))

    val extension = sourceFile.extension.takeIf { it.isNotBlank() }
        ?: source.accessUrl.substringAfterLast('.', "")
            .takeIf { it.isNotBlank() && it.length <= 8 }
            ?.lowercase()
        ?: "bin"

    val relativeKey = "$relativeDir/$mediaId.$extension"
    val payload = sourceFile.withScopedAccessSuspend {
        sourceFile.readBytes()
    }
    val accessUrl = writePlatformPrivateStorageBytes(
        key = relativeKey,
        bytes = payload,
        mimeType = sourceFile.mimeType()?.toString().orEmpty().ifBlank { extension.extensionToMimeType() },
    ).orEmpty()
    return ImportedMedia(
        storageHandle = createPlatformPrivateStorageHandle(relativeKey),
        accessUrl = accessUrl,
        sizeBytes = payload.size.toLong(),
    )
}

private fun buildThumbKey(
    storageHandle: String,
): String {
    val sourceKey = extractPlatformPrivateStorageKeyFromStorageHandle(storageHandle)
        ?: error("Expected native private storage handle for thumbnail source")
    val extension = sourceKey.substringAfterLast('.', "")
    val stem = sourceKey.removeSuffix(if (extension.isBlank()) "" else ".$extension")
    return "$stem.thumb.jpg"
}

private suspend fun <T> PlatformFile.withScopedAccessSuspend(
    block: suspend () -> T
): T {
    val accessGranted = startAccessingSecurityScopedResource()
    try {
        return block()
    } finally {
        if (accessGranted) {
            stopAccessingSecurityScopedResource()
        }
    }
}

private suspend fun normalizeAccessUrl(accessUrl: String): String =
    resolveStorageHandleToAccessUrl(accessUrl)
        ?.removePrefix("file://")
        ?.decodeURLPart()
        ?.trim()
        ?: accessUrl.removePrefix("file://")
            .decodeURLPart()
            .trim()
