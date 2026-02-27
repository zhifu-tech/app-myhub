package tech.zhifu.app.myhub.component.media.api.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.component.MediaPreviewDialog
import tech.zhifu.app.myhub.component.media.component.MediaThumbnail
import tech.zhifu.app.myhub.theme.AppTheme

private object PreviewMediaPreviewer : MediaPreviewer {
    override fun openInSystemPlayer(item: MediaItem): Boolean = false

    override fun isSystemPlayerPreferred(): Boolean = false
}

private fun sampleImageItem(): MediaItem = MediaItem(
    id = "preview-image",
    file = PlatformFile("feature/capture/docs/design/capture-ready-input-idle/screen.png"),
    name = "Preview Image",
    isVideo = false
)

private fun sampleVideoItem(): MediaItem = MediaItem(
    id = "preview-video",
    file = PlatformFile("feature/capture/docs/design/capture-ready-input-idle/screen.png"),
    name = "Preview Video",
    isVideo = true
)

@Preview
@Composable
private fun MediaThumbnailPreview() {
    AppTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.padding(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MediaThumbnail(
                    item = sampleImageItem(),
                    onRemove = {},
                    onPreview = {},
                    enabled = true
                )
                MediaThumbnail(
                    item = sampleVideoItem(),
                    onRemove = {},
                    onPreview = {},
                    enabled = true
                )
                MediaThumbnail(
                    item = sampleImageItem(),
                    onRemove = {},
                    onPreview = {},
                    enabled = false
                )
            }
        }
    }
}

@Preview
@Composable
private fun MediaPreviewDialogPreview() {
    AppTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "MediaPreviewDialog (image)",
                style = MaterialTheme.typography.titleMedium
            )
            Surface(
                modifier = Modifier.weight(1f),
                tonalElevation = 2.dp
            ) {
                MediaPreviewDialog(
                    item = sampleImageItem(),
                    mediaPreviewer = PreviewMediaPreviewer,
                    onDismiss = {}
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Action")
                }
                Spacer(Modifier.size(1.dp))
            }
        }
    }
}
