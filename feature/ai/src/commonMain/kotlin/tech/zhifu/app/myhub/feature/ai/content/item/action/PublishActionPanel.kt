package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionEvent
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponent
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_publish_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_publish_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_publish

@Composable
fun PublishActionPanel(
    component: ActionComponent,
    onAction: (ActionEvent) -> Unit,
) {
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_publish),
        title = stringResource(Res.string.feature_ai_action_panel_publish_title),
        supporting = stringResource(Res.string.feature_ai_action_panel_publish_support),
    ) {
        ActionButtonsColumn(
            actions = component.actions.primary,
            onAction = onAction,
        )
        ActionButtonsFlow(
            actions = component.actions.secondary,
            onAction = onAction,
        )
    }
}
