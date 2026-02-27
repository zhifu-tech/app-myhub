package tech.zhifu.app.myhub.feature.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.component.card.formatCreatedTime
import tech.zhifu.app.myhub.datastore.model.domain.Card

/**
 * 元数据展示组件
 * 显示卡片的创建时间、来源、模板等信息
 *
 * 使用 SectionCard 包装，提供统一的视觉样式
 */
@Composable
fun CardDetailMetadata(
    card: Card,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Metadata",
        modifier = modifier
    ) {
        Column {
            // 创建时间
            MetadataRow(
                label = "Created",
                value = card.formatCreatedTime()
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // 来源（允许折行显示）
            MetadataRow(
                label = "Source",
                value = "N/A",
                allowValueWrap = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // 卡片类型
            MetadataRow(
                label = "Type",
                value = card.type.wire
            )
        }
    }
}

/**
 * 元数据行组件
 */
@Composable
private fun MetadataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    allowValueWrap: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            maxLines = if (allowValueWrap) Int.MAX_VALUE else 1,
            softWrap = allowValueWrap
        )
    }
}
