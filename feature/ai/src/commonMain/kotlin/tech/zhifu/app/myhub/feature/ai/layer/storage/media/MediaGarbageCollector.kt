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
            val localExists = mediaFileStore.fileExists(localUri = asset.localUri)
            if (!hasCard || !localExists) {
                mediaFileStore.deleteIfExists(localUri = asset.localUri)
                asset.thumbUri?.let {
                    mediaFileStore.deleteIfExists(localUri = it)
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
