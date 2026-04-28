package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_complete_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_complete_title

@Composable
fun QuickReplyActionPanel(
    component: ActionComponentSchema,
    onAction: (ActionCommand) -> Unit,
) {
    ActionSupportPanel(
        title = stringResource(Res.string.feature_ai_action_panel_complete_title),
        supporting = stringResource(Res.string.feature_ai_action_panel_complete_support),
    ) {
        ActionButtonsColumn(
            actions = component.actions.primary,
            onAction = onAction,
        )
    }
}
