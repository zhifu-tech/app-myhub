package tech.zhifu.app.myhub.component.media.internal

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.browser.window

actual fun openInSystemPlayer(
    file: PlatformFile,
    mimeType: String?
): Boolean {
    val path = file.path
    if (path.isBlank()) return false
    window.open(path, "_blank")
    return true
}

actual fun isVlcAvailable(): Boolean = true
