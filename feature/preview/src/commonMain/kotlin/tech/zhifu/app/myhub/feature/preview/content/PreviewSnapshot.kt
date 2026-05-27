package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard

internal const val PREVIEW_SHARE_LINK = "https://example.com"

internal enum class ExportMode {
    Visible,
    FullContent,
}

internal class PreviewSnapshotController {
    var captureVisibleSnapshot: (suspend () -> String?)? = null
}

@Composable
internal fun rememberPreviewSnapshotController(): PreviewSnapshotController {
    return remember { PreviewSnapshotController() }
}

@Composable
internal expect fun rememberPreviewSnapshot(
    card: ContentCard,
    width: Dp,
    snapshotController: PreviewSnapshotController? = null,
    exportMode: ExportMode = ExportMode.FullContent,
): suspend () -> String?

@Composable
internal expect fun Modifier.previewSnapshotSource(
    controller: PreviewSnapshotController?,
): Modifier
