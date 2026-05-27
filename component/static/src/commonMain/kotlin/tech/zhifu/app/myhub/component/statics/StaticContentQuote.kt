package tech.zhifu.app.myhub.component.statics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.statics.resources.Res
import tech.zhifu.app.myhub.component.statics.resources.component_static_loading_quote
import tech.zhifu.app.myhub.component.statics.resources.drawables.QuoteIcon

@Composable
fun StaticQuote() {
    val quote = stringResource(Res.string.component_static_loading_quote)
    Column(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Icon(
                    imageVector = QuoteIcon,
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                    contentDescription = null,
                    modifier = Modifier.size(width = 17.dp, height = 12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
