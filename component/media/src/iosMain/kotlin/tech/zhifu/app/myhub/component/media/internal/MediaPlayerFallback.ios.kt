package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.path
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import tech.zhifu.app.myhub.component.media.MediaItem

actual fun openInSystemPlayer(
    item: MediaItem,
    mimeType: String?
): Boolean {
    val path = item.previewUrl.ifBlank { item.file?.path.orEmpty() }
    if (path.isBlank()) return false
    val url = NSURL.URLWithString(path) ?: return false
    return UIApplication.sharedApplication.openURL(url)
}

actual fun isVlcAvailable(): Boolean = true
