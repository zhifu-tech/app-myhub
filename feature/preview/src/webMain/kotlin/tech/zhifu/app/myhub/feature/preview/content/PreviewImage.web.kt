package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import tech.zhifu.app.myhub.feature.preview.PreviewPayload

@Composable
internal actual fun rememberPreviewImage(
    payload: PreviewPayload,
    width: Dp,
): suspend () -> String? = remember(payload, width) { suspend { null } }
