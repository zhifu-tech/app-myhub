package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPayload
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_input_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_current_value
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_input_title

@Composable
fun InputActionPanel(
    component: ActionComponentSchema,
    onAction: (ActionCommand) -> Unit,
) {
    val payload = component.payload as? ActionPayload.Input ?: return
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_input_hint),
        title = when (payload.field) {
            Field.SUMMARY -> stringResource(Res.string.feature_ai_action_panel_input_summary)
            Field.LOCATION -> stringResource(Res.string.feature_ai_action_panel_input_location)
            else -> stringResource(Res.string.feature_ai_action_panel_input_title)
        },
        supporting = stringResource(Res.string.feature_ai_action_panel_input_support),
    ) {
        payload.currentValue
            .trim()
            .takeIf { it.isNotBlank() }
            ?.let { value ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.feature_ai_action_panel_current_value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        ActionButtonsFlow(
            actions = component.actions.secondary,
            onAction = onAction,
        )
    }
}
