package tech.zhifu.app.myhub.component.statics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.ui.design.util.isHeightCompact
import tech.zhifu.app.myhub.ui.design.util.rememberWindowSizeClass

@Composable
fun StaticContent(
    modifier: Modifier = Modifier,
    contentCard: @Composable (Modifier) -> Unit,
    contentTexts: @Composable (Boolean) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        val windowSizeClass = rememberWindowSizeClass()
        val isLandscape = maxWidth > maxHeight
            && windowSizeClass.isHeightCompact()
        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                contentCard(Modifier)
                Spacer(modifier = Modifier.widthIn(min = 32.dp, max = 72.dp))
                contentTexts(isLandscape)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                contentCard(Modifier)
                Spacer(modifier = Modifier.height(56.dp))
                contentTexts(isLandscape)
            }
        }
    }
}
