package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class ClipboardManager {
    actual fun copyToClipboard(text: String) {
        // 使用现代 Clipboard API
        val clipboard = js("navigator.clipboard")
        if (clipboard != null) {
            clipboard.writeText(text).catch { error ->
                console.error("Failed to copy to clipboard:", error)
                // 降级方案：使用传统的 execCommand
                fallbackCopyToClipboard(text)
            }
        } else {
            // 降级方案
            fallbackCopyToClipboard(text)
        }
    }

    private fun fallbackCopyToClipboard(text: String) {
        val textArea = js("document.createElement('textarea')")
        textArea.value = text
        textArea.style.position = "fixed"
        textArea.style.left = "-999999px"
        js("document.body.appendChild(textArea)")
        textArea.select()
        try {
            js("document.execCommand('copy')")
        } catch (e: Throwable) {
            console.error("Fallback copy failed:", e)
        }
        js("document.body.removeChild(textArea)")
    }
}

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    return remember { ClipboardManager() }
}

