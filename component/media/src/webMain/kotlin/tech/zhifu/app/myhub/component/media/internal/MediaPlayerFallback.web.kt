package tech.zhifu.app.myhub.component.media.internal

import kotlinx.browser.window
import tech.zhifu.app.myhub.component.media.MediaItem

actual fun openInSystemPlayer(
    item: MediaItem,
    mimeType: String?
): Boolean {
    val path = item.previewUrl.trim().ifBlank { item.file?.toString()?.trim().orEmpty() }
    if (path.isBlank()) return false
    window.open(path, "_blank")
    return true
}

actual fun isVlcAvailable(): Boolean = true
