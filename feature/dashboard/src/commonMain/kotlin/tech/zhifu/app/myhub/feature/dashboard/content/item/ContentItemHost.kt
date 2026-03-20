package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.sharedWith
import tech.zhifu.app.myhub.ui.LocalSharedTransitionScope

@Composable
fun ContentItemHost(
    item: ContentItem,
    previewState: PreviewState,
    content: @Composable (Modifier) -> Unit,
) {
    val visible by remember(item.id) {
        derivedStateOf {
            previewState.payload?.contentId != item.id || previewState.visible.not()
        }
    }
    val sharedTransitionScope = LocalSharedTransitionScope.current
    content(
        sharedTransitionScope.sharedWith(
            key = "content-preview-${item.id}",
            visible = visible,
        )
    )
}
