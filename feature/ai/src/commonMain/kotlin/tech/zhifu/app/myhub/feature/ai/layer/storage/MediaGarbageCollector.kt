package tech.zhifu.app.myhub.feature.ai.layer.storage

import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository

class MediaGarbageCollector(
    private val captureLocalRepository: CaptureLocalRepository,
) {
    suspend fun collect(limit: Int = 500): MediaGcResult {
        val assets = captureLocalRepository.listAllMediaAssets(limit = limit)
        var removedCount = 0
        var keptCount = 0

        assets.forEach { asset ->
            val hasCard = captureLocalRepository.hasCard(asset.cardId)
            val localExists = fileExists(asset.localUri)
            if (!hasCard || !localExists) {
                deleteFileIfExists(asset.localUri)
                asset.thumbUri?.let { deleteFileIfExists(it) }
                captureLocalRepository.deleteMediaAsset(asset.id)
                removedCount += 1
            } else {
                keptCount += 1
            }
        }
        return MediaGcResult(removedCount = removedCount, keptCount = keptCount)
    }

    private fun fileExists(path: String): Boolean {
        return path.isNotBlank()
    }

    private fun deleteFileIfExists(path: String) {
        if (path.isBlank()) return
        // No-op in commonMain. File deletion is platform-specific with filekit 0.13.
    }
}

data class MediaGcResult(
    val removedCount: Int,
    val keptCount: Int,
)
