package tech.zhifu.app.myhub.feature.ai.content.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.input.ChatInputBar
import tech.zhifu.app.myhub.feature.ai.content.preview.PreviewThumbnail

@Composable
fun BottomBar(
    viewModel: AIViewModel,
) {
    BottomBarContent(
        previewThumbnail = {
            PreviewThumbnail(viewModel = viewModel)
        },
        chatInputBar = { modifier ->
            ChatInputBar(viewModel = viewModel, modifier = modifier)
        },
    )
}

@Composable
fun BottomBarContent(
    previewThumbnail: @Composable BoxScope.() -> Unit = {},
    chatInputBar: @Composable (Modifier) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(16.dp), // 保持与键盘间距
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            ChatInputBarWrapper(chatInputBar)
        }
        previewThumbnail()
    }
}

@Composable
private fun RowScope.ChatInputBarWrapper(inputBox: @Composable ((Modifier) -> Unit)) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val modifier = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
            Modifier.widthIn(min = 120.dp, max = 480.dp)
        }

        else -> {
            Modifier.weight(1f)
        }
    }
    inputBox(modifier)
}
