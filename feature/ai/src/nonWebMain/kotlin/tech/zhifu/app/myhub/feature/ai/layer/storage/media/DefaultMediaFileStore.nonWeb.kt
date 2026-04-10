package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.atomicMove
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import io.github.vinceglb.filekit.write

private class DefaultMediaFileStore : MediaFileStore {
    override suspend fun importToManagedStorage(
        cardId: String,
        mediaId: String,
        sourceUri: String,
    ): ImportedMedia {
        val source = PlatformFile(sourceUri)
        val mediaDir = (FileKit.filesDir / "app-data" / "cards" / cardId / "media")
        mediaDir.createDirectories()

        val extension = source.extension.takeIf { it.isNotBlank() }
            ?: sourceUri.substringAfterLast('.', "")
                .takeIf { it.isNotBlank() && it.length <= 8 }
                ?.lowercase()
            ?: "bin"

        val destination = mediaDir / "$mediaId.$extension"
        val temporary = mediaDir / "$mediaId.tmp.$extension"

        source.withScopedAccessSuspend {
            source copyTo temporary
        }
        temporary.atomicMove(destination)
        return ImportedMedia(
            localUri = destination.absolutePath(),
            sizeBytes = destination.size(),
        )
    }

    override suspend fun createImageThumbnail(
        localUri: String,
        quality: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): String? {
        val source = PlatformFile(localUri)
        if (!source.exists()) return null
        val parent = source.parent() ?: return null
        val thumb = parent / "${source.nameWithoutExtension}.thumb.jpg"
        val bytes = source.withScopedAccessSuspend {
            FileKit.compressImage(
                file = source,
                quality = quality,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }
        thumb write bytes
        return thumb.path
    }

    override suspend fun fileExists(
        localUri: String
    ): Boolean = runCatching {
        PlatformFile(localUri).exists()
    }.getOrDefault(false)

    override suspend fun deleteIfExists(
        localUri: String
    ) {
        runCatching {
            val file = PlatformFile(localUri)
            if (file.exists()) {
                file.delete(mustExist = false)
            }
        }
    }
}

actual fun createMediaFileStore(): MediaFileStore = DefaultMediaFileStore()

suspend fun <T> PlatformFile.withScopedAccessSuspend(
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
