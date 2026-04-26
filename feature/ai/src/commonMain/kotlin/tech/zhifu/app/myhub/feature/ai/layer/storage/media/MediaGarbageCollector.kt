package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository

class MediaGarbageCollector(
    private val captureLocalRepository: CaptureLocalRepository,
    private val mediaFileStore: MediaFileStore,
) {
    suspend fun collect(
        limit: Int = 500
    ): MediaGcResult {
        val assets = captureLocalRepository.listAllMediaAssets(limit = limit)
        var removedCount = 0
        var keptCount = 0

        assets.forEach { asset ->
            val hasCard = captureLocalRepository.hasCard(asset.cardId)
            val localExists = mediaFileStore.fileExists(
                storageHandle = asset.storageHandle,
                accessUrl = asset.accessUrl,
            )
            if (!hasCard || !localExists) {
                mediaFileStore.deleteIfExists(
                    storageHandle = asset.storageHandle,
                    accessUrl = asset.accessUrl,
                )
                if (!asset.thumbStorageHandle.isNullOrBlank() || !asset.thumbAccessUrl.isNullOrBlank()) {
                    mediaFileStore.deleteIfExists(
                        storageHandle = asset.thumbStorageHandle.orEmpty(),
                        accessUrl = asset.thumbAccessUrl.orEmpty(),
                    )
                }
                captureLocalRepository.deleteMediaAsset(asset.id)
                removedCount += 1
            } else {
                keptCount += 1
            }
        }
        return MediaGcResult(removedCount = removedCount, keptCount = keptCount)
    }
}

data class MediaGcResult(
    val removedCount: Int,
    val keptCount: Int,
)
