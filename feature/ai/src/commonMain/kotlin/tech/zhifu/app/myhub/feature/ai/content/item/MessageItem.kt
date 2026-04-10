package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun MessageBubbleItem(message: Message) {
    val isUser = message.role == Message.Role.USER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .background(
                    color = when (message.role) {
                        Message.Role.AI -> MaterialTheme.colorScheme.surfaceContainerHighest
                        Message.Role.USER -> MaterialTheme.colorScheme.primaryContainer
                        Message.Role.SYSTEM -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = when (message.role) {
                        Message.Role.USER -> RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 6.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp,
                        )

                        else -> RoundedCornerShape(
                            topStart = 6.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp,
                        )
                    },
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.textRes?.let { res ->
                    stringResource(resource = res, *message.textArgs.toTypedArray())
                } ?: message.text,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
