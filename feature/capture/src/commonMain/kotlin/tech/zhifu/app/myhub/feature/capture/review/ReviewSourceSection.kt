package tech.zhifu.app.myhub.feature.capture.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.feature.capture.resources.Res
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_source_form_extract
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_source_form_link
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_source_form_own
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_source_label

@Composable
internal fun ReviewSource(
    sourceForm: CardSource,
    onSourceFormChange: (CardSource) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.feature_capture_source_label).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = RoundedCornerShape(size = 9999.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReviewSourceFormPill(
                    icon = Icons.Default.Link,
                    label = stringResource(Res.string.feature_capture_source_form_link),
                    selected = sourceForm == CardSource.Link,
                    onClick = { onSourceFormChange(CardSource.Link) }
                )
                ReviewSourceFormPill(
                    icon = Icons.Default.ContentCut,
                    label = stringResource(Res.string.feature_capture_source_form_extract),
                    selected = sourceForm == CardSource.Extract,
                    onClick = { onSourceFormChange(CardSource.Extract) }
                )
                ReviewSourceFormPill(
                    icon = Icons.Default.EditNote,
                    label = stringResource(Res.string.feature_capture_source_form_own),
                    selected = sourceForm == CardSource.Own,
                    onClick = { onSourceFormChange(CardSource.Own) }
                )
            }
        }
    }
}

@Composable
internal fun ReviewSourceFormPill(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(size = 9999.dp),
        color = background,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.height(32.dp)
                .then(
                    if (selected) Modifier.padding(horizontal = 12.dp)
                    else Modifier.width(32.dp)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            if (selected) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label.uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = contentColor
                )
            }
        }
    }
}

