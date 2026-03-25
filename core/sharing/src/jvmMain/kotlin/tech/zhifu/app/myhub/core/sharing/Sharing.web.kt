package tech.zhifu.app.myhub.core.sharing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberShareSupported(): Boolean = false

@Composable
actual fun rememberShare(): Share = remember {
    object : Share {
        override fun invoke(data: List<String>, options: SharingOptions?) = Unit
    }
}
