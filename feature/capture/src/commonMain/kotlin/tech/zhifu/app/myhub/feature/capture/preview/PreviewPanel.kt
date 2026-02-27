package tech.zhifu.app.myhub.feature.capture.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import tech.zhifu.app.myhub.feature.capture.CaptureState
import tech.zhifu.app.myhub.feature.capture.CaptureUiState

@Composable
internal fun PreviewPanel(
    modifier: Modifier = Modifier,
    uiState: CaptureUiState,
    reviewRichTextState: RichTextState
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .padding(vertical = 24.dp)
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (uiState.state) {
                CaptureState.ReadyIdle,
                CaptureState.ReadyFocused,
                CaptureState.AiFailed -> PreviewPanelContentReady()

                CaptureState.AiProcessing -> PreviewPanelContentProcessing()

                CaptureState.ReviewEditing,
                CaptureState.PostProcessing,
                CaptureState.PostSuccess,
                CaptureState.PostFailed -> uiState.review?.let { review ->
                    PreviewPanelContentReview(
                        review = review,
                        reviewRichTextState = reviewRichTextState
                    )
                } ?: PreviewPanelContentReady()
            }
        }
    }
}
