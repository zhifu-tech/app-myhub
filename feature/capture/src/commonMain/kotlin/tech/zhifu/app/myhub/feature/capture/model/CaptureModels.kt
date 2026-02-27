package tech.zhifu.app.myhub.feature.capture.model

import androidx.compose.ui.graphics.Color
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureReviewDataPayload
import tech.zhifu.app.myhub.feature.capture.ReviewContentType
import tech.zhifu.app.myhub.feature.capture.ReviewCtx
import tech.zhifu.app.myhub.feature.capture.StyleOption

internal fun CaptureReviewDataPayload.toReviewCtx(): ReviewCtx {
    return ReviewCtx(
        text = text,
        title = title,
        source = CardSource.fromWire(sourceForm),
        styleOptions = styleOptions.map {
            StyleOption(
                label = it.label,
                color = it.colorHex.toColor()
            )
        },
        selectedStyleIndex = selectedStyleIndex,
        tags = tags,
        tagQuery = tagQuery,
        code = code,
        codeLanguage = codeLanguage,
        imageOcrSummary = imageOcrSummary,
        imageOcrInfo = imageOcrInfo,
        videoMetadataSummary = videoMetadataSummary,
        videoMetadataInfo = videoMetadataInfo,
        primaryContentType = primaryContentType.toReviewContentType()
    )
}

private fun String.toReviewContentType(): ReviewContentType {
    return when (lowercase()) {
        "code" -> ReviewContentType.Code
        "image" -> ReviewContentType.Image
        "video" -> ReviewContentType.Video
        else -> ReviewContentType.Text
    }
}

private fun String.toColor(): Color {
    val normalized = removePrefix("#")
    val argbInt = runCatching {
        when (normalized.length) {
            6 -> ("FF$normalized").toLong(16).toInt()
            8 -> normalized.toLong(16).toInt()
            else -> 0xFFB4A3FF.toInt()
        }
    }.getOrDefault(0xFFB4A3FF.toInt())
    return Color(argbInt)
}
