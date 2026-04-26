package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_title

@Composable
fun AssistantMessageItem(
    message: Message,
    tone: AssistantTone,
    draft: CaptureDraft,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
    selectedTags: List<String>,
    onAction: (String) -> Unit,
) {
    val backgroundColor = when (tone) {
        AssistantTone.AI -> MaterialTheme.colorScheme.surfaceContainerHighest
        AssistantTone.SYSTEM -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
    }
    val contentColor = when (tone) {
        AssistantTone.AI -> MaterialTheme.colorScheme.onSurface
        AssistantTone.SYSTEM -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val text = message.textRes
        ?.let { res ->
            stringResource(
                resource = res,
                formatArgs = message.textArgs.toTypedArray(),
            )
        }
        ?: message.text

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        AssistantAvatar(tone = tone)
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 22.dp,
                bottomStart = 22.dp,
                bottomEnd = 22.dp,
            ),
            color = backgroundColor,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                message.editingField?.let { field ->
                    EditFieldPill(field = field)
                }
                text
                    .trim()
                    .takeIf { it.isNotBlank() }
                    ?.let { content ->
                        Text(
                            text = content,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                if (message.actionComponents.isNotEmpty()) {
                    InlineActionDeck(
                        components = message.actionComponents,
                        draft = draft,
                        mediaGenerationProgress = mediaGenerationProgress,
                        selectedTags = selectedTags,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditFieldPill(
    field: Field,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.84f),
    ) {
        Text(
            text = when (field) {
                Field.MEDIA -> stringResource(Res.string.feature_ai_action_edit_media)
                Field.TAGS -> stringResource(Res.string.feature_ai_action_edit_tags)
                Field.SUMMARY -> stringResource(Res.string.feature_ai_action_edit_summary)
                Field.LOCATION -> stringResource(Res.string.feature_ai_action_edit_location)
                else -> stringResource(Res.string.feature_ai_action_edit_title)
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AssistantAvatar(
    tone: AssistantTone,
) {
    val (label, brush, contentColor) = when (tone) {
        AssistantTone.AI -> Triple(
            "AI",
            Brush.linearGradient(
                colors = listOf(Color(0xFF9B8CFF), Color(0xFF4F46E5)),
            ),
            Color.White,
        )

        AssistantTone.SYSTEM -> Triple(
            "SYS",
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                ),
            ),
            MaterialTheme.colorScheme.onSecondary,
        )
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .size(width = 38.dp, height = 38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush = brush),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

enum class AssistantTone {
    AI,
    SYSTEM,
}
