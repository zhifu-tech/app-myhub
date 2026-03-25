package tech.zhifu.app.myhub.core.saving

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImageSaver(): ImageSaver {
    return remember {
        ImageSaver { ImageSavingResult.Unsupported }
    }
}

@Composable
actual fun rememberImageSavingSupported(): Boolean = false
