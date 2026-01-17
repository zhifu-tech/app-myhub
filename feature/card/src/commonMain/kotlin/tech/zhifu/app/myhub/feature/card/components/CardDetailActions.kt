package tech.zhifu.app.myhub.feature.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.datastore.model.Card

/**
 * 操作面板组件
 * 包含分享、编辑、删除、复制等操作按钮
 *
 * @param showDeleteConfirm 是否显示删除确认对话框（由 ViewModel 控制）
 */
@Composable
fun CardDetailActions(
    card: Card,
    isSharing: Boolean = false,
    showDeleteConfirm: Boolean = false,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,  // 显示确认对话框，而不是直接删除
    onConfirmDelete: () -> Unit,  // 确认删除
    onCancelDelete: () -> Unit,    // 取消删除
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Actions",
        modifier = modifier
    ) {
        // 主要操作按钮：分享/导出
        Button(
            onClick = onShare,
            enabled = !isSharing,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSharing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = "Share / Export",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // 次要操作按钮组
        // 使用 BoxWithConstraints 检测可用空间，动态调整布局
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            val preferredButtonWidth = 80.dp  // 推荐按钮宽度（显示图标+文字）
            val availableWidth = maxWidth
            val buttonCount = 3
            val spacing = 8.dp
            val totalSpacing = spacing * (buttonCount - 1)
            val availableWidthPerButton = (availableWidth - totalSpacing) / buttonCount

            // 判断是否显示文字：如果每个按钮可用宽度 >= 推荐宽度，则显示文字
            val showLabels = availableWidthPerButton >= preferredButtonWidth

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 编辑按钮
                ActionButton(
                    icon = Icons.Default.Edit,
                    label = "Edit",
                    onClick = onEdit,
                    showLabel = showLabels,
                    modifier = Modifier.weight(1f),
                    isDangerous = false
                )

                // 复制按钮
                ActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    onClick = onCopy,
                    showLabel = showLabels,
                    modifier = Modifier.weight(1f),
                    isDangerous = false
                )

                // 删除按钮（危险操作）
                ActionButton(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    onClick = onDelete,
                    showLabel = showLabels,
                    modifier = Modifier.weight(1f),
                    isDangerous = true
                )
            }
        }
    }
}

/**
 * 操作按钮组件
 * 根据可用空间自动调整显示方式：空间充足时显示图标+文字，空间不足时只显示图标
 */
@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    showLabel: Boolean,
    modifier: Modifier = Modifier,
    isDangerous: Boolean = false
) {
    val errorColor = MaterialTheme.colorScheme.error
    val contentColor = if (isDangerous) errorColor else MaterialTheme.colorScheme.onSurface

    if (showLabel) {
        // 空间充足：显示图标+文字（垂直布局）
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            colors = if (isDangerous) {
                ButtonDefaults.outlinedButtonColors(contentColor = errorColor)
            } else {
                ButtonDefaults.outlinedButtonColors()
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Text(
                    text = label,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    } else {
        // 空间不足：只显示图标（使用 IconButton）
        // 注意：contentDescription 已经包含了 label，屏幕阅读器可以读取
        IconButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label, // 用于无障碍访问和屏幕阅读器
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
        }
    }
}
