package tech.zhifu.app.myhub.feature.capture.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.feature.capture.CaptureError
import tech.zhifu.app.myhub.feature.capture.CaptureState

@Composable
internal fun InputSection(
    state: CaptureState,
    error: CaptureError?,
    inputText: String,
    mediaItems: List<MediaItem>,
    canEditInput: Boolean,
    horizontalPadding: Dp,
    onInputChange: (String) -> Unit,
    onInputFocusChanged: (Boolean) -> Unit,
    onRemoveMedia: (String) -> Unit,
    onAddMedia: () -> Unit,
    onPreviewMedia: (MediaItem) -> Unit,
    onRetry: () -> Unit,
    onEditInput: () -> Unit,
    onClearError: () -> Unit
) {
    val shouldDim = state == CaptureState.AiProcessing
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .then(
                other = if (shouldDim) {
                    Modifier.graphicsLayer { alpha = 0.1f }
                } else {
                    Modifier
                }
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputContent(
            inputText = inputText,
            onInputChange = onInputChange,
            onInputFocusChanged = onInputFocusChanged,
            mediaItems = mediaItems,
            onRemoveMedia = onRemoveMedia,
            onAddMedia = onAddMedia,
            onPreviewMedia = onPreviewMedia,
            horizontalPadding = horizontalPadding,
            isProcessing = !canEditInput
        )
    }
    when (state) {
        CaptureState.AiProcessing -> OverlayAiProcessing()
        CaptureState.AiFailed -> {
            OverlayAiFailed(
                message = (error as? CaptureError.AiFailed)?.message.orEmpty(),
                onRetry = onRetry,
                onEditInput = onEditInput
            )
            if (error is CaptureError.UploadFailed) {
                CaptureInlineErrorBanner(
                    message = error.message,
                    onDismiss = onClearError
                )
            }
        }

        else -> Unit
    }
}

