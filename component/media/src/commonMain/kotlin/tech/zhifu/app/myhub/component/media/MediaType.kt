package tech.zhifu.app.myhub.component.media

object MediaType {
    val ImageExtensions: Set<String> = setOf("jpg", "jpeg", "png", "webp", "gif", "heic")
    val VideoExtensions: Set<String> = setOf("mp4", "mov", "m4v", "webm", "avi", "mkv", "wmv")
}

/**
 * 判断 mimeType 是否为图片类型
 */
fun String?.isImage(): Boolean =
    this?.startsWith("image/", ignoreCase = true) ?: false

/**
 * 判断 mimeType 是否为视频类型
 */
fun String?.isVideo(): Boolean =
    this?.startsWith("video/", ignoreCase = true) ?: false

/**
 * 从 mimeType 推断文件扩展名（非空返回）
 */
fun String.mimeTypeToExtension(): String =
    when (lowercase()) {
        "image/jpeg", "image/jpg" -> "jpg"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        "image/png" -> "png"
        "image/heic" -> "heic"
        "video/mp4" -> "mp4"
        "video/quicktime" -> "mov"
        "video/webm" -> "webm"
        "application/octet-stream" -> "bin"
        else -> "bin"
    }

/**
 * 从文件扩展名推断 mimeType
 */
fun String.extensionToMimeType(): String =
    when (lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "heic" -> "image/heic"
        "mp4" -> "video/mp4"
        "mov" -> "video/quicktime"
        "webm" -> "video/webm"
        "m4v" -> "video/mp4"
        "avi" -> "video/x-msvideo"
        "mkv" -> "video/x-matroska"
        "wmv" -> "video/x-ms-wmv"
        else -> "application/octet-stream"
    }
