package tech.zhifu.app.myhub.feature.ai.model

fun CaptureMediaAsset.isVideo(): Boolean =
    mediaType.startsWith("video/", ignoreCase = true)


fun CaptureMediaAsset.displayName(index: Int = 0): String =
    accessUrl.substringAfterLast('/').ifBlank {
        if (isVideo()) {
            "video-${index + 1}"
        } else {
            "image-${index + 1}"
        }
    }
