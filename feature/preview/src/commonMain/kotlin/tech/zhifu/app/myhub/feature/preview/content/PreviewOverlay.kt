package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.preview.PreviewState

@Composable
fun PreviewOverlay(
    state: PreviewState,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val overlayColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = state::hide,
            )
            .background(color = overlayColor)
    )
}
