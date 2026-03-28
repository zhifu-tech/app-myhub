package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SaveAlt
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
import tech.zhifu.app.myhub.core.saving.ImageSavingResult
import tech.zhifu.app.myhub.core.saving.rememberImageSaver
import tech.zhifu.app.myhub.core.saving.rememberImageSavingSupported
import tech.zhifu.app.myhub.feature.preview.resources.Res
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_saving_failure
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_saving_permission_denied
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_saving_success
import tech.zhifu.app.myhub.feature.preview.resources.feature_preview_saving_unsupported
import tech.zhifu.app.myhub.ui.LocalSnabackbarState
import tech.zhifu.app.myhub.ui.model.ContentCard

@Composable
internal fun PreviewActionSavingButton(
    card: ContentCard,
    shareContentWidth: Dp,
    snapshotController: PreviewSnapshotController? = null,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = LocalSnabackbarState.current,
) {
    val isSavingSupported = rememberImageSavingSupported()
    if (!isSavingSupported) {
        return
    }

    val capturePreviewImage = rememberPreviewSnapshot(
        card = card,
        width = shareContentWidth,
        snapshotController = snapshotController,
        exportMode = ExportMode.FullContent,
    )
    val imageSaver = rememberImageSaver()
    val coroutineScope = rememberCoroutineScope()
    val savingResultHandler = savingResultHandler(coroutineScope, snackbarHostState)
    val saveHandler = remember(capturePreviewImage, imageSaver, coroutineScope) {
        {
            coroutineScope.launch {
                val previewImage = capturePreviewImage()
                if (previewImage == null) {
                    savingResultHandler(ImageSavingResult.Failure("capture failed"))
                    return@launch
                }
                val result = imageSaver.save(previewImage)
                savingResultHandler(result)
            }
            Unit
        }
    }

    FilledTonalIconButton(
        onClick = saveHandler,
        shape = CircleShape,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.SaveAlt,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun savingResultHandler(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
): (ImageSavingResult) -> Unit {
    val savingSuccessText = stringResource(Res.string.feature_preview_saving_success)
    val permissionDeniedText = stringResource(Res.string.feature_preview_saving_permission_denied)
    val savingFailureText = stringResource(Res.string.feature_preview_saving_failure)
    val unsupportedText = stringResource(Res.string.feature_preview_saving_unsupported)

    return remember(
        coroutineScope,
        snackbarHostState,
        savingSuccessText,
        permissionDeniedText,
        savingFailureText,
        unsupportedText,
    ) {
        { result ->
            val message = when (result) {
                ImageSavingResult.Success -> savingSuccessText
                ImageSavingResult.PermissionDenied -> permissionDeniedText
                is ImageSavingResult.Failure -> savingFailureText
                ImageSavingResult.Unsupported -> unsupportedText
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = message)
            }
        }
    }
}
