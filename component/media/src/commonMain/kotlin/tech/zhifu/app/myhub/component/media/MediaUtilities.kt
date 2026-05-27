package tech.zhifu.app.myhub.component.media

fun MediaItem.displayImageModel(): String =
    thumbnailUrl
        ?.takeIf { it.isNotBlank() }
        ?.toPlayableUrl()
        ?: previewUrl.toPlayableUrl()

fun MediaItem.displayMediaUrl(): String =
    previewUrl.toPlayableUrl().ifBlank {
        file?.toString()?.toPlayableUrl().orEmpty()
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
