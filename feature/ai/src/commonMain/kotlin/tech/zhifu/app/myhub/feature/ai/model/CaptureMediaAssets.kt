package tech.zhifu.app.myhub.feature.ai.model

import tech.zhifu.app.myhub.component.media.isVideo

fun CaptureMediaAsset.displayName(index: Int = 0): String =
    accessUrl.substringAfterLast('/').ifBlank {
        if (mediaType.isVideo()) {
            "video-${index + 1}"
        } else {
            "image-${index + 1}"
        }
    }
