package tech.zhifu.app.myhub.component.statics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StaticContent(
    modifier: Modifier = Modifier,
    contentCard: @Composable (Modifier) -> Unit,
    contentTexts: @Composable (Boolean) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
//            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val isLandscape = maxWidth > maxHeight
            && windowSizeClass.minHeightDp == 0
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
            tonalElevation = 1.dp,
            shadowElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
            ),
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    contentCard(Modifier)
                    Spacer(modifier = Modifier.widthIn(min = 24.dp, max = 56.dp))
                    contentTexts(isLandscape)
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    contentCard(Modifier)
                    Spacer(modifier = Modifier.height(28.dp))
                    contentTexts(isLandscape)
                }
            }
        }
    }
}
