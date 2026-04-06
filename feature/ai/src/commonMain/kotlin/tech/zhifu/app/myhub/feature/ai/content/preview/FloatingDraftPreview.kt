package tech.zhifu.app.myhub.feature.ai.content.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FloatingDraftPreview(
    title: String,
    summary: String,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(0.52f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "预览",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
            )
            Text(
                text = title.ifBlank { "未命名草稿" },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF0F172A),
                maxLines = 1,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                maxLines = 1,
            )
        }
    }
}
