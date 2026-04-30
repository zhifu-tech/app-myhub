package tech.zhifu.app.myhub.ui.platform

import android.content.ClipData
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry

actual suspend fun Clipboard.copyText(text: String) {
    setClipEntry(ClipData.newPlainText("text", text).toClipEntry())
}
