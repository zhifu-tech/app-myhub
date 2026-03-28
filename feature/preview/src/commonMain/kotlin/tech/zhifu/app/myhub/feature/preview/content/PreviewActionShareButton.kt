package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.core.sharing.rememberShare
import tech.zhifu.app.myhub.core.sharing.rememberShareSupported
import tech.zhifu.app.myhub.ui.model.ContentCard

@Composable
internal fun PreviewActionShareButton(
    card: ContentCard,
    shareContentWidth: Dp,
    snapshotController: PreviewSnapshotController? = null,
    modifier: Modifier = Modifier,
) {
    val shareSupported = rememberShareSupported()
    if (!shareSupported) {
        return
    }
    val share = rememberShare()
    val coroutineScope = rememberCoroutineScope()
    val capturePreviewImage = rememberPreviewSnapshot(
        card = card,
        width = shareContentWidth,
        snapshotController = snapshotController,
        exportMode = ExportMode.FullContent,
    )
    val onShare = remember(share, capturePreviewImage, coroutineScope) {
        {
            coroutineScope.launch {
                val previewImage = capturePreviewImage()
                val data = buildList {
                    add(PREVIEW_SHARE_LINK)
                    if (previewImage != null) {
                        add(previewImage)
                    }
                }
                share(data)
            }
            Unit
        }
    }
    FilledTonalIconButton(
        onClick = onShare,
        shape = CircleShape,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.IosShare,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
