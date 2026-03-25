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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.feature.preview.PreviewPayload
import tech.zhifu.app.myhub.feature.sharing.rememberShare

@Composable
internal fun PreviewActionShareButton(
    sharePayload: PreviewPayload,
    shareContentWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val share = rememberShare()
    val coroutineScope = rememberCoroutineScope()
    val capturePreviewImage = rememberPreviewShareImage(
        payload = sharePayload,
        width = shareContentWidth,
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
