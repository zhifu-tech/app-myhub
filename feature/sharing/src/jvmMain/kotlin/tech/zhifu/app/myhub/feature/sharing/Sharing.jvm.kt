package tech.zhifu.app.myhub.feature.sharing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public actual fun rememberShare(): Share = remember {
    object : Share {
        override fun invoke(data: List<String>, options: SharingOptions?) = Unit
    }
}

@Composable
public actual fun rememberShareSupported(): Boolean = false
