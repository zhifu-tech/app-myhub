package tech.zhifu.app.myhub.feature.dashboard.content.statics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.drawables.QuoteIcon
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_loading_quote

@Composable
fun StaticQuote() {
    val quote = stringResource(Res.string.feature_dashboard_loading_quote)
    Column(
        modifier = Modifier.widthIn(max = 360.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Icon(
            imageVector = QuoteIcon,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            contentDescription = null,
            modifier = Modifier.size(width = 17.dp, height = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(start = 24.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Start,
            )
        }
    }
}
