package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import tech.zhifu.app.myhub.feature.preview.PreviewPayload

@Composable
internal actual fun rememberPreviewSnapshot(
    payload: PreviewPayload,
    width: Dp,
    snapshotController: PreviewSnapshotController?,
    exportMode: ExportMode,
): suspend () -> String? = remember(payload, width, snapshotController, exportMode) {
    suspend {
        if (exportMode == ExportMode.Visible) {
            snapshotController?.captureVisibleSnapshot?.invoke()
        } else {
            null
        }
    }
}

@Composable
internal actual fun Modifier.previewSnapshotSource(
    controller: PreviewSnapshotController?,
): Modifier = this
