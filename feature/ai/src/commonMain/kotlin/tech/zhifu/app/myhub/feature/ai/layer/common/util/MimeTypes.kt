package tech.zhifu.app.myhub.feature.ai.layer.common.util

fun inferMimeType(
    uri: String
): String {
    val normalized = uri.lowercase()
    return when {
        normalized.endsWith(".jpg") || normalized.endsWith(".jpeg") -> "image/jpeg"
        normalized.endsWith(".png") -> "image/png"
        normalized.endsWith(".webp") -> "image/webp"
        normalized.endsWith(".gif") -> "image/gif"
        normalized.endsWith(".mp4") -> "video/mp4"
        normalized.endsWith(".mov") -> "video/quicktime"
        normalized.endsWith(".m4v") -> "video/x-m4v"
        else -> "application/octet-stream"
    }
}
