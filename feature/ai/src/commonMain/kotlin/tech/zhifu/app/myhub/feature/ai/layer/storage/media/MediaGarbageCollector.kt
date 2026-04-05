package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository

class MediaGarbageCollector(
    private val captureLocalRepository: CaptureLocalRepository,
) {
    suspend fun collect(
        limit: Int = 500
    ): MediaGcResult {
        val assets = captureLocalRepository.listAllMediaAssets(limit = limit)
        var removedCount = 0
        var keptCount = 0

        assets.forEach { asset ->
            val hasCard = captureLocalRepository.hasCard(asset.cardId)
            val localExists = fileExists(path = asset.localUri)
            if (!hasCard || !localExists) {
                deleteFileIfExists(path = asset.localUri)
                asset.thumbUri?.let {
                    deleteFileIfExists(path = it)
                }
                captureLocalRepository.deleteMediaAsset(asset.id)
                removedCount += 1
            } else {
                keptCount += 1
            }
        }
        return MediaGcResult(removedCount = removedCount, keptCount = keptCount)
    }

    private fun fileExists(
        path: String
    ): Boolean = path.isNotBlank() // fixme  需要增加文件是否存在的判断

    private fun deleteFileIfExists(path: String) {
        if (path.isBlank()) return
        // fixme 需要增加文件删除的逻辑
        // No-op in commonMain. File deletion is platform-specific with filekit 0.13.
    }
}

data class MediaGcResult(
    val removedCount: Int,
    val keptCount: Int,
)
