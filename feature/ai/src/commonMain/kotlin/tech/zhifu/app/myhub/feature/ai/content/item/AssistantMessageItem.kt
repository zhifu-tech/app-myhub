package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.item.action.ActionComponents
import tech.zhifu.app.myhub.feature.ai.content.item.action.EditFieldPill
import tech.zhifu.app.myhub.feature.ai.content.text
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_copied
import tech.zhifu.app.myhub.ui.design.util.LocalSnackbarState
import tech.zhifu.app.myhub.ui.platform.copyText

@Composable
fun AssistantMessageItem(
    viewModel: AIViewModel,
    message: Message,
) {
    AssistantMessageItem(
        message = message,
        actionComponents = {
            ActionComponents(
                viewModel = viewModel,
                message = message,
            )
        }
    )
}

@Composable
fun AssistantMessageItem(
    message: Message,
    actionComponents: @Composable () -> Unit,
) {
    val (backgroundColor, contentColor) = when (message.role) {
        Message.Role.AI -> {
            MaterialTheme.colorScheme.surfaceContainerHighest to
                MaterialTheme.colorScheme.onSurface
        }

        Message.Role.SYSTEM -> {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f) to
                MaterialTheme.colorScheme.onSecondaryContainer
        }

        else -> return
    }

    val messageText = message.text()
    val clipboard = LocalClipboard.current
    val snackbarState = LocalSnackbarState.current
    val coroutineScope = rememberCoroutineScope()
    val copiedMessage = stringResource(Res.string.feature_ai_msg_copied)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        AssistantAvatar(role = message.role)
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
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            if (messageText.isBlank()) return@combinedClickable
                            coroutineScope.launch {
                                clipboard.copyText(messageText)
                                snackbarState.showSnackbar(
                                    message = copiedMessage,
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        }
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                message.editingField?.let { field ->
                    EditFieldPill(field = field)
                }
                messageText
                    .takeIf { it.isNotBlank() }
                    ?.let { content ->
                        Text(
                            text = content,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                actionComponents()
            }
        }
    }
}

@Composable
private fun AssistantAvatar(
    role: Message.Role,
) {
    val (label, brush, contentColor) = when (role) {
        Message.Role.AI -> Triple(
            "AI",
            Brush.linearGradient(
                colors = listOf(Color(0xFF9B8CFF), Color(0xFF4F46E5)),
            ),
            Color.White,
        )

        Message.Role.SYSTEM -> Triple(
            "SYS",
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                ),
            ),
            MaterialTheme.colorScheme.onSecondary,
        )

        else -> return
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
