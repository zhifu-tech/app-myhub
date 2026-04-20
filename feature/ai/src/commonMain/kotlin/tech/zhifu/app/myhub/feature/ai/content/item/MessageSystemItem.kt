package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun MessageSystemItem(
    message: Message,
    draft: CaptureDraft,
    selectedTags: List<String>,
    onAction: (String) -> Unit,
) {
    AssistantMessageItem(
        message = message,
        tone = AssistantTone.SYSTEM,
        draft = draft,
        selectedTags = selectedTags,
        onAction = onAction,
    )
}
