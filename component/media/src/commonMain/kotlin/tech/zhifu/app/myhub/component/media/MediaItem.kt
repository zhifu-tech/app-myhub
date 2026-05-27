package tech.zhifu.app.myhub.component.media

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType

data class MediaItem(
    val id: String,
    val name: String,
    val previewUrl: String,
    val mediaType: String = "",
    val file: PlatformFile? = null,
    val thumbnailUrl: String? = null,
) {
    fun systemMimeType(): String = file?.mimeType()?.toString()
        ?: previewUrl.substringAfterLast('.', "")
            .lowercase()
            .let { extension ->
                when (extension) {
                    in MediaType.VideoExtensions -> "video/*"
                    in MediaType.ImageExtensions -> "image/*"
                    else -> if (mediaType.isVideo()) "video/*" else "image/*"
                }
            }
}
