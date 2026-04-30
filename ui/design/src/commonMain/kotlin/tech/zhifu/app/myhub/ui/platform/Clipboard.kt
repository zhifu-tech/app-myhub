package tech.zhifu.app.myhub.ui.platform

import androidx.compose.ui.platform.Clipboard

expect suspend fun Clipboard.copyText(text: String)
