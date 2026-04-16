package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun MessageSystemItem(
    message: Message
) {
    MessageBubbleItem(
        placeLeft = true,
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = message.textRes
                ?.let { res ->
                    stringResource(
                        resource = res,
                        formatArgs = *message.textArgs.toTypedArray()
                    )
                }
                ?: message.text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
