package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun PreviewContentTitle(
    title: String,
    modifier: Modifier,
) {
    Text(
        modifier = modifier,
        text = title,
        style = MaterialTheme.typography.headlineSmall
            .copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
