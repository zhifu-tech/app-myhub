package tech.zhifu.app.myhub.ui.platform

import androidx.compose.ui.platform.Clipboard
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual suspend fun Clipboard.copyText(
    text: String
) {
    try {
        val selection = StringSelection(text)
        val systemClipboard = Toolkit.getDefaultToolkit().systemClipboard
        systemClipboard.setContents(selection, selection)
    } catch (e: Exception) {
        // 在某些 Linux 环境或 headless 环境下可能会抛出 IllegalStateException
        e.printStackTrace()
    }
}
