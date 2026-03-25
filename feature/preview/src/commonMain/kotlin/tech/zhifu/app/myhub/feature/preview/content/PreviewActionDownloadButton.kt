package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.preview.PreviewPayload
import tech.zhifu.app.myhub.feature.preview.resources.Res
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_download_failure
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_download_permission_denied
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_download_success
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_download_unsupported
import tech.zhifu.app.myhub.ui.LocalSnabackbarState

@Composable
internal fun PreviewActionDownloadButton(
    sharePayload: PreviewPayload,
    shareContentWidth: Dp,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = LocalSnabackbarState.current,
) {
    val isDownloadSupported = rememberPreviewDownloadSupported()
    if (!isDownloadSupported) {
        return
    }

    val capturePreviewImage = rememberPreviewImage(
        payload = sharePayload,
        width = shareContentWidth,
    )
    val downloader = rememberPreviewDownloader()
    val coroutineScope = rememberCoroutineScope()
    val downloadResultHandler = downloadResultHandler(coroutineScope, snackbarHostState)
    val downloadHandler = remember(capturePreviewImage, downloader, coroutineScope) {
        {
            coroutineScope.launch {
                val previewImage = capturePreviewImage()
                if (previewImage == null) {
                    downloadResultHandler(PreviewDownloadResult.Failure("capture failed"))
                    return@launch
                }
                val result = downloader.download(previewImage)
                downloadResultHandler(result)
            }
            Unit
        }
    }

    FilledTonalIconButton(
        onClick = downloadHandler,
        shape = CircleShape,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Download,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun downloadResultHandler(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
): (PreviewDownloadResult) -> Unit {
    val downloadSuccessText = stringResource(Res.string.feature_preview_download_success)
    val permissionDeniedText = stringResource(Res.string.feature_preview_download_permission_denied)
    val downloadFailureText = stringResource(Res.string.feature_preview_download_failure)
    val unsupportedText = stringResource(Res.string.feature_preview_download_unsupported)

    return remember(
        coroutineScope,
        snackbarHostState,
        downloadSuccessText,
        permissionDeniedText,
        downloadFailureText,
        unsupportedText,
    ) {
        { result ->
            val message = when (result) {
                PreviewDownloadResult.Success -> downloadSuccessText
                PreviewDownloadResult.PermissionDenied -> permissionDeniedText
                is PreviewDownloadResult.Failure -> downloadFailureText
                PreviewDownloadResult.Unsupported -> unsupportedText
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = message)
            }
        }
    }
}
