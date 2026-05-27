package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionText.text(): String =
    when (this) {
        is ActionText.Plain -> this.value
        is ActionText.Resource -> stringResource(
            resource = this.resource,
            formatArgs = this.formatArgs.toTypedArray(),
        )
    }
