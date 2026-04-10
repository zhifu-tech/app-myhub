package tech.zhifu.app.myhub.feature.ai.layer.storage.media

private class NoopMediaFileStore : MediaFileStore {
    override suspend fun importToManagedStorage(
        cardId: String,
        mediaId: String,
        sourceUri: String,
    ): ImportedMedia = ImportedMedia(
        localUri = sourceUri,
        sizeBytes = 0L,
    )

    override suspend fun createImageThumbnail(
        localUri: String,
        quality: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): String? = null

    override suspend fun fileExists(
        localUri: String
    ): Boolean = localUri.isNotBlank()

    override suspend fun deleteIfExists(
        localUri: String
    ) = Unit
}

actual fun createMediaFileStore(): MediaFileStore = NoopMediaFileStore()
