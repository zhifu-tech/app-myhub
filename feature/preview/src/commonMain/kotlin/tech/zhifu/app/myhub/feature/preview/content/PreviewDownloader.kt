package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable

internal sealed interface PreviewDownloadResult {
    data object Success : PreviewDownloadResult

    data object PermissionDenied : PreviewDownloadResult

    data object Unsupported : PreviewDownloadResult

    data class Failure(val message: String? = null) : PreviewDownloadResult
}

internal fun interface PreviewDownloader {
    suspend fun download(imagePath: String): PreviewDownloadResult
}

@Composable
internal expect fun rememberPreviewDownloader(): PreviewDownloader

@Composable
internal expect fun rememberPreviewDownloadSupported(): Boolean
