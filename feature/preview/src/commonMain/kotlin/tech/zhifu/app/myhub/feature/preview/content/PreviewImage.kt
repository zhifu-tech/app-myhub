package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import tech.zhifu.app.myhub.feature.preview.PreviewPayload

internal const val PREVIEW_SHARE_LINK = "https://example.com"

@Composable
internal expect fun rememberPreviewImage(
    payload: PreviewPayload,
    width: Dp,
): suspend () -> String?
