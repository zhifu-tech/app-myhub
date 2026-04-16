package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_thinking_collapse
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_thinking_collapsed_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_thinking_expand
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_thinking_title

@Composable
fun MessageThinkingItem(
    message: Message
) {
    var thinkingCollapsed by rememberSaveable(message.id) {
        mutableStateOf(true)
    }
    MessageBubbleItem(
        placeLeft = true,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(Res.string.feature_ai_thinking_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (thinkingCollapsed) {
                Text(
                    text = stringResource(Res.string.feature_ai_thinking_collapsed_hint),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(
                    onClick = { thinkingCollapsed = false },
                ) {
                    Text(stringResource(Res.string.feature_ai_thinking_expand))
                }
            } else {
                Text(
                    text = message.textRes?.let { res ->
                        stringResource(resource = res, *message.textArgs.toTypedArray())
                    } ?: message.text,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(
                    onClick = { thinkingCollapsed = true },
                ) {
                    Text(stringResource(Res.string.feature_ai_thinking_collapse))
                }
            }
        }
    }
}
