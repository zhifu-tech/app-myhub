package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * 剪贴板管理器接口
 * 用于跨平台复制文本到剪贴板
 */
expect class ClipboardManager {
    /**
     * 复制文本到剪贴板
     */
    fun copyToClipboard(text: String)
}

/**
 * 在 Compose 中记住 ClipboardManager 实例
 * Android 平台需要特殊处理，其他平台使用默认实现
 */
@Composable
expect fun rememberClipboardManager(): ClipboardManager
