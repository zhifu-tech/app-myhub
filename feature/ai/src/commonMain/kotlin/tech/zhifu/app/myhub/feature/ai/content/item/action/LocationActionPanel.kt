package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPayload
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_current
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_empty_placeholder
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_support_missing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_support_ready
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_title_missing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_location_title_ready

@Composable
fun LocationActionPanel(
    component: ActionComponentSchema,
    onAction: (ActionCommand) -> Unit,
) {
    val payload = component.payload as? ActionPayload.Location ?: return
    val hasLocation = payload.currentLocation.isNotBlank()
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_edit_location),
        title = if (hasLocation) {
            stringResource(Res.string.feature_ai_action_panel_location_title_ready)
        } else {
            stringResource(Res.string.feature_ai_action_panel_location_title_missing)
        },
        supporting = if (hasLocation) {
            stringResource(Res.string.feature_ai_action_panel_location_support_ready)
        } else {
            stringResource(Res.string.feature_ai_action_panel_location_support_missing)
        },
    ) {
        if (hasLocation) {
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
                        text = stringResource(Res.string.feature_ai_action_panel_location_current),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                    Text(
                        text = payload.currentLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(Res.string.feature_ai_action_panel_location_empty_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        ActionButtonsFlow(
            actions = component.actions.inline,
            onAction = onAction,
        )
        ActionButtonsFlow(
            actions = component.actions.secondary,
            onAction = onAction,
        )
    }
}
