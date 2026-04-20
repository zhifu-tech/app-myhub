package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun MessageThinkingItem(
    message: Message
) {
    ReasoningTraceCard(
        text = message.textRes?.let { res ->
            org.jetbrains.compose.resources.stringResource(
                resource = res,
                formatArgs = message.textArgs.toTypedArray(),
            )
        } ?: message.text,
        live = false,
        modifier = Modifier.padding(horizontal = 12.dp),
        persistentKey = "reasoning-${message.id}",
    )
}
