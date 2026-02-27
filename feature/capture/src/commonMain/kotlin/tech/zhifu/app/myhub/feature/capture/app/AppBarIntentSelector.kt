package tech.zhifu.app.myhub.feature.capture.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
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
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.feature.capture.resources.Res
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_intent_do
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_intent_material
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_intent_review

@Composable
internal fun IntentSelector(
    intent: CardType,
    onIntentChange: (CardType) -> Unit,
    enabled: Boolean
) {
    Surface(
        shape = RoundedCornerShape(size = 9999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IntentButton(
                icon = Icons.Default.Visibility,
                label = stringResource(Res.string.feature_capture_intent_review),
                selected = intent == CardType.Review,
                enabled = enabled,
                onClick = { onIntentChange(CardType.Review) }
            )
            IntentButton(
                icon = Icons.Default.TaskAlt,
                label = stringResource(Res.string.feature_capture_intent_do),
                selected = intent == CardType.Do,
                enabled = enabled,
                onClick = { onIntentChange(CardType.Do) }
            )
            IntentButton(
                icon = Icons.Default.Extension,
                label = stringResource(Res.string.feature_capture_intent_material),
                selected = intent == CardType.Material,
                enabled = enabled,
                onClick = { onIntentChange(CardType.Material) }
            )
        }
    }
}

@Composable
private fun IntentButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        shape = RoundedCornerShape(size = 9999.dp),
        color = background,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.height(32.dp)
                .then(
                    if (selected) Modifier.padding(horizontal = 16.dp)
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
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label.uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = contentColor
                )
            }
        }
    }
}
