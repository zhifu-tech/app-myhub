package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ClipboardManager as ComposeClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

actual class ClipboardManager(
    private val composeClipboardManager: ComposeClipboardManager
) {
    actual fun copyToClipboard(text: String) {
        composeClipboardManager.setText(AnnotatedString(text))
    }
}

// 辅助函数，用于在 Compose 中创建 ClipboardManager
@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    val composeClipboardManager = LocalClipboardManager.current
    return remember { ClipboardManager(composeClipboardManager) }
}

