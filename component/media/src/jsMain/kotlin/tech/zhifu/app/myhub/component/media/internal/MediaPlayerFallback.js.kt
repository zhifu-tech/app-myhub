package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.path
import kotlinx.browser.window
import tech.zhifu.app.myhub.component.media.MediaItem

actual fun openInSystemPlayer(
    item: MediaItem,
    mimeType: String?
): Boolean {
    val path = item.previewUrl.ifBlank { item.file?.path.orEmpty() }
    if (path.isBlank()) return false
    window.open(path, "_blank")
    return true
}

actual fun isVlcAvailable(): Boolean = true
