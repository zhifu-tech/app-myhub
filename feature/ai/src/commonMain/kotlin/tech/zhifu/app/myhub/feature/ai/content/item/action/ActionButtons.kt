package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionEvent
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionItemSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionStyle
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.text

@Composable
fun ActionButtonsColumn(
    actions: List<ActionItemSchema>,
    onAction: (ActionEvent) -> Unit,
) {
    actions.forEach { action ->
        ActionButton(
            action = action,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ActionButtonsFlow(
    actions: List<ActionItemSchema>,
    onAction: (ActionEvent) -> Unit,
) {
    if (actions.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { action ->
            ActionButton(
                action = action,
                onAction = onAction,
            )
        }
    }
}

@Composable
fun ActionButton(
    action: ActionItemSchema,
    onAction: (ActionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = action.label.text()
    when (action.style) {
        ActionStyle.Primary -> Button(
            onClick = { onAction(action.event) },
            enabled = action.enabled,
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(label)
        }

        ActionStyle.Tonal -> FilledTonalButton(
            onClick = { onAction(action.event) },
            enabled = action.enabled,
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(label)
        }

        ActionStyle.Destructive -> OutlinedButton(
            onClick = { onAction(action.event) },
            enabled = action.enabled,
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.28f),
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text(label)
        }

        ActionStyle.Chip -> AssistChip(
            onClick = { onAction(action.event) },
            enabled = action.enabled,
            label = { Text(label) },
        )

        ActionStyle.SelectedChip -> InputChip(
            selected = true,
            onClick = { onAction(action.event) },
            enabled = action.enabled,
            label = { Text(label) },
        )

        ActionStyle.InlineSuggestion -> ExamplePill(
            text = label,
            onClick = { onAction(action.event) },
        )

        else -> OutlinedButton(
            onClick = { onAction(action.event) },
            enabled = action.enabled,
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(label)
        }
    }
}
