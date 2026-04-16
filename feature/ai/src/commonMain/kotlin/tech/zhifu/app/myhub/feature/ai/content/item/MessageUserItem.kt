package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun MessageUserItem(
    message: Message
) {
    MessageBubbleItem(
        placeLeft = false,
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
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
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
