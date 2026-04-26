package tech.zhifu.app.myhub.component.media.util

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import tech.zhifu.app.myhub.component.media.MediaItem
import kotlin.time.Clock

fun PlatformFile.toMediaItem(): MediaItem {
    val fileName = name.takeIf { it.isNotBlank() } ?: "media"
    val extension = fileName.substringAfterLast('.', "").lowercase()
    val isVideo = extension in setOf(
        "mp4",
        "mov",
        "m4v",
        "webm",
        "avi",
        "mkv",
        "wmv"
    )
    return MediaItem(
        id = "${fileName}_${Clock.System.now()}",
        name = fileName,
        previewUrl = toString(),
        isVideo = isVideo,
        file = this,
    )
}

fun PlatformFile.toPlayableUrl(): String = toString().toPlayableUrl()

fun MediaItem.displayImageModel(): String =
    thumbnailUrl
        ?.takeIf { it.isNotBlank() }
        ?.toPlayableUrl()
        ?: previewUrl.toPlayableUrl()

fun MediaItem.displayMediaUrl(): String =
    previewUrl.toPlayableUrl().ifBlank { file?.toPlayableUrl().orEmpty() }

fun String.toPlayableUrl(): String {
    val rawPath = this.trim()
    if (rawPath.isBlank()) return ""
    return if (
        rawPath.startsWith("file://") ||
        rawPath.startsWith("content://") ||
        rawPath.startsWith("http://") ||
        rawPath.startsWith("https://") ||
        rawPath.startsWith("blob:") ||
        rawPath.startsWith("data:")
    ) {
        rawPath
    } else {
        "file://$rawPath"
    }
}

fun MediaItem.systemMimeType(): String {
    return file?.mimeType()?.toString()
        ?: previewUrl.substringAfterLast('.', "")
            .lowercase()
            .let { extension ->
                when (extension) {
                    "mp4", "mov", "m4v", "webm", "avi", "mkv", "wmv" -> "video/*"
                    "jpg", "jpeg", "png", "webp", "gif", "heic" -> "image/*"
                    else -> if (isVideo) "video/*" else "image/*"
                }
            }
}
