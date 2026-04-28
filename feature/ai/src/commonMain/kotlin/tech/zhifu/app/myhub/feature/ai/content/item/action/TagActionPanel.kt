package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPayload
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_custom_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_support
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_title_empty
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_tag_title_selected

@Composable
fun TagActionPanel(
    component: ActionComponentSchema,
    onAction: (ActionCommand) -> Unit,
) {
    val payload = component.payload as? ActionPayload.Tags ?: return
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_edit_tags),
        title = if (payload.selectedCount == 0) {
            stringResource(Res.string.feature_ai_action_panel_tag_title_empty)
        } else {
            stringResource(Res.string.feature_ai_action_panel_tag_title_selected)
        },
        supporting = stringResource(Res.string.feature_ai_action_panel_tag_support),
    ) {
        ActionButtonsFlow(
            actions = component.actions.inline,
            onAction = onAction,
        )
        HintPill(text = stringResource(Res.string.feature_ai_action_panel_tag_custom_hint))
        ActionButtonsFlow(
            actions = component.actions.secondary,
            onAction = onAction,
        )
    }
}
