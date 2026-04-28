package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentKind
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun ActionComponents(
    viewModel: AIViewModel,
    draft: CaptureDraft,
    message: Message,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
) {
    ActionComponentsContent(
        components = message.actionComponents,
        draft = draft,
        mediaGenerationProgress = mediaGenerationProgress,
        onAction = viewModel::doAction,
    )
}

@Composable
fun ActionComponentsContent(
    components: List<ActionComponentSchema>,
    draft: CaptureDraft,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
    onAction: (ActionCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (components.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        components.forEach { component ->
            when (component.kind) {
                ActionComponentKind.REVIEW -> ReviewActionPanel(
                    component = component,
                    onAction = onAction,
                )

                ActionComponentKind.PUBLISH -> PublishActionPanel(
                    component = component,
                    onAction = onAction,
                )

                ActionComponentKind.MEDIA -> MediaActionPanel(
                    component = component,
                    draft = draft,
                    mediaGenerationProgress = mediaGenerationProgress,
                    onAction = onAction,
                )

                ActionComponentKind.TAGS -> TagActionPanel(
                    component = component,
                    onAction = onAction,
                )

                ActionComponentKind.INPUT -> InputActionPanel(
                    component = component,
                    onAction = onAction,
                )

                ActionComponentKind.LOCATION -> LocationActionPanel(
                    component = component,
                    onAction = onAction,
                )

                ActionComponentKind.QUICK_REPLY -> QuickReplyActionPanel(
                    component = component,
                    onAction = onAction,
                )
            }
        }
    }
}
