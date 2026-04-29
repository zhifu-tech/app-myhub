package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentKind
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponent
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun ActionComponents(
    viewModel: AIViewModel,
    message: Message,
) {
    ActionComponentsContent(
        components = message.actionComponents,
        componentFactory = { component ->
            when (component.kind) {
                ActionComponentKind.REVIEW -> ReviewActionPanel(
                    component = component,
                    onAction = viewModel::doAction,
                )

                ActionComponentKind.PUBLISH -> PublishActionPanel(
                    component = component,
                    onAction = viewModel::doAction,
                )

                ActionComponentKind.MEDIA -> MediaActionPanel(
                    component = component,
                    viewModel = viewModel,
                )

                ActionComponentKind.TAGS -> TagActionPanel(
                    component = component,
                    onAction = viewModel::doAction,
                )

                ActionComponentKind.INPUT -> InputActionPanel(
                    component = component,
                    onAction = viewModel::doAction,
                )

                ActionComponentKind.LOCATION -> LocationActionPanel(
                    component = component,
                    onAction = viewModel::doAction,
                )

                ActionComponentKind.QUICK_REPLY -> QuickReplyActionPanel(
                    component = component,
                    onAction = viewModel::doAction,
                )
            }
        }
    )
}

@Composable
fun ActionComponentsContent(
    components: List<ActionComponent>,
    modifier: Modifier = Modifier,
    componentFactory: @Composable (ActionComponent) -> Unit,
) {
    if (components.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        components.forEach { component ->
            componentFactory(component)
        }
    }
}
