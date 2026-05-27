package tech.zhifu.app.myhub.ui.platform

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.copyText(text: String) {
    setClipEntry(ClipEntry.withPlainText(text))
}
