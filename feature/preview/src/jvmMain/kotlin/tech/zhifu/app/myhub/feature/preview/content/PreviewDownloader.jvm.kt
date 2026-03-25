package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberPreviewDownloader(): PreviewDownloader {
    return remember {
        PreviewDownloader { PreviewDownloadResult.Unsupported }
    }
}

@Composable
internal actual fun rememberPreviewDownloadSupported(): Boolean = false
