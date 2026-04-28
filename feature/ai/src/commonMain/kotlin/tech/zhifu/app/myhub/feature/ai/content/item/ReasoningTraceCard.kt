package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_reasoning_collapsed_preview
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_reasoning_history_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_reasoning_live_desc
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_reasoning_live_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_thinking_collapse
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_thinking_expand

@Composable
fun ReasoningTraceCard(
    text: String,
    live: Boolean,
    modifier: Modifier = Modifier,
    persistentKey: String? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    var expanded by rememberSaveable(persistentKey) { mutableStateOf(live) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReasoningPulseDot(live = live)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (live) {
                            stringResource(Res.string.feature_ai_reasoning_live_title)
                        } else {
                            stringResource(Res.string.feature_ai_reasoning_history_title)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (live) {
                            stringResource(Res.string.feature_ai_reasoning_live_desc)
                        } else {
                            stringResource(Res.string.feature_ai_reasoning_collapsed_preview)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = if (expanded) {
                        stringResource(Res.string.feature_ai_thinking_collapse)
                    } else {
                        stringResource(Res.string.feature_ai_thinking_expand)
                    },
                    modifier = Modifier.clickable { expanded = !expanded },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (secondaryActionLabel != null && onSecondaryAction != null) {
                Text(
                    text = secondaryActionLabel,
                    modifier = Modifier.clickable(onClick = onSecondaryAction),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (expanded) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                ) {
                    Text(
                        text = text.trim(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReasoningPulseDot(
    live: Boolean,
) {
    val colors = remember(live) {
        if (live) {
            listOf(Color(0xFF22C55E), Color(0xFF0EA5E9))
        } else {
            listOf(Color(0xFFF59E0B), Color(0xFFF97316))
        }
    }
    Box(
        modifier = Modifier
            .size(14.dp)
            .background(
                brush = Brush.linearGradient(colors),
                shape = RoundedCornerShape(999.dp),
            ),
    )
}
