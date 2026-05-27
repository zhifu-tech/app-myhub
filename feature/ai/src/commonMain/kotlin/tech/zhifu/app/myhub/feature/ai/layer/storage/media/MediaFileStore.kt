package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import io.github.vinceglb.filekit.PlatformFile

interface MediaFileStore {
    suspend fun persistDraftMedia(
        draftId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia

    suspend fun importToManagedStorage(
        cardId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia

    suspend fun saveGeneratedImage(
        draftId: String,
        mediaId: String,
        bytes: ByteArray,
        mimeType: String = "image/png",
    ): ImportedMedia

    suspend fun createImageThumbnail(
        source: ImportedMedia,
        quality: Int = 80,
        maxWidth: Int = 720,
        maxHeight: Int = 720,
    ): ImportedMedia?

    suspend fun fileExists(
        storageHandle: String,
        accessUrl: String = "",
    ): Boolean

    suspend fun deleteIfExists(
        storageHandle: String,
        accessUrl: String = "",
    )
}

data class MediaImportSource(
    val storageHandle: String = "",
    val accessUrl: String = "",
    val platformFile: PlatformFile? = null,
    val sizeBytes: Long? = null,
)

data class ImportedMedia(
    val storageHandle: String,
    val accessUrl: String,
    val sizeBytes: Long,
)

expect fun createMediaFileStore(): MediaFileStore
