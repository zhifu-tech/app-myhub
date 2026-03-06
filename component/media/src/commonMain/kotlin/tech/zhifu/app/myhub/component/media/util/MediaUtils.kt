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
        file = this,
        name = fileName,
        isVideo = isVideo
    )
}

fun PlatformFile.toPlayableUrl(): String {
    val rawPath = toString().trim()
    if (rawPath.isBlank()) return ""
    return if (rawPath.startsWith("file://")) rawPath else "file://$rawPath"
}

fun MediaItem.systemMimeType(): String {
    return file.mimeType()?.toString() ?: if (isVideo) "video/*" else "audio/*"
}
