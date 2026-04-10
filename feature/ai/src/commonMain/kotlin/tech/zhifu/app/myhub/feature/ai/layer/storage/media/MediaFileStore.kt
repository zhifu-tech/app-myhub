package tech.zhifu.app.myhub.feature.ai.layer.storage.media

interface MediaFileStore {
    suspend fun importToManagedStorage(
        cardId: String,
        mediaId: String,
        sourceUri: String,
    ): ImportedMedia

    suspend fun createImageThumbnail(
        localUri: String,
        quality: Int = 80,
        maxWidth: Int = 720,
        maxHeight: Int = 720,
    ): String?

    suspend fun fileExists(
        localUri: String
    ): Boolean

    suspend fun deleteIfExists(
        localUri: String
    )
}

data class ImportedMedia(
    val localUri: String,
    val sizeBytes: Long,
)

expect fun createMediaFileStore(): MediaFileStore
