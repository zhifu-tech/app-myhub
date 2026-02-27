package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openInSystemPlayer(
    file: PlatformFile,
    mimeType: String?
): Boolean {
    val path = file.path
    if (path.isBlank()) return false
    val url = NSURL.URLWithString(path) ?: return false
    return UIApplication.sharedApplication.openURL(url)
}

actual fun isVlcAvailable(): Boolean = true
