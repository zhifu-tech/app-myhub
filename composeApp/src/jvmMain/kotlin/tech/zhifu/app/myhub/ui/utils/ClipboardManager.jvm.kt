package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual class ClipboardManager {
    actual fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(text)
        clipboard.setContents(selection, null)
    }
}

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    return remember { ClipboardManager() }
}

