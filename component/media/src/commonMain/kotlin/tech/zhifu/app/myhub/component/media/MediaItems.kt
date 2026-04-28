package tech.zhifu.app.myhub.component.media

import io.github.vinceglb.filekit.mimeType

fun MediaItem.displayImageModel(): String =
    thumbnailUrl
        ?.takeIf { it.isNotBlank() }
        ?.toPlayableUrl()
        ?: previewUrl.toPlayableUrl()

fun MediaItem.displayMediaUrl(): String =
    previewUrl.toPlayableUrl().ifBlank {
        file?.toString()?.toPlayableUrl().orEmpty()
    }

fun MediaItem.systemMimeType(): String =
    file?.mimeType()?.toString()
        ?: previewUrl.substringAfterLast('.', "")
            .lowercase()
            .let { extension ->
                when (extension) {
                    "mp4", "mov", "m4v", "webm", "avi", "mkv", "wmv" -> "video/*"
                    "jpg", "jpeg", "png", "webp", "gif", "heic" -> "image/*"
                    else -> if (isVideo) "video/*" else "image/*"
                }
            }

private fun String.toPlayableUrl(): String {
    val rawPath = this.trim()
    return when {
        rawPath.isBlank() -> ""
        rawPath.startsWith("file://") ||
            rawPath.startsWith("content://") ||
            rawPath.startsWith("http://") ||
            rawPath.startsWith("https://") ||
            rawPath.startsWith("blob:") ||
            rawPath.startsWith("data:")
            -> rawPath

        else -> "file://$rawPath"
    }
}
