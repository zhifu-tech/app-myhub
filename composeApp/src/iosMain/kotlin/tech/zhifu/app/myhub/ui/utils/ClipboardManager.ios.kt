package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard
import platform.UIKit.generalPasteboard

actual class ClipboardManager {
    actual fun copyToClipboard(text: String) {
        val pasteboard = UIPasteboard.generalPasteboard
        pasteboard.setString(text)
    }
}

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    return remember { ClipboardManager() }
}

