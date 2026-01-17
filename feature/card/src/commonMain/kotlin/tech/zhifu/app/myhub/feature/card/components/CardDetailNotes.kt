package tech.zhifu.app.myhub.feature.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 个人笔记组件
 * 允许用户为卡片添加个人笔记和反思
 *
 * 使用 SectionCard 包装，提供统一的视觉样式
 *
 * 注意：onNotesChange 会触发防抖保存（500ms），由 ViewModel 处理
 */
@Composable
fun CardDetailNotes(
    notes: String,
    onNotesChange: (String) -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Personal Notes",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 标题已在 SectionCard 中显示，这里可以显示保存状态
            if (isSaving) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Saving...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (notes.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 12.dp),
            placeholder = {
                Text(
                    text = "Add your reflections on this card here...",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            maxLines = 5,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            ),
            textStyle = MaterialTheme.typography.bodySmall
        )
    }
}
