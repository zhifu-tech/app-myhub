package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun MessageAIItem(
    message: Message,
    draft: CaptureDraft,
    mediaGenerationProgress: ProviderImageGenerationProgress?,
    selectedTags: List<String>,
    onAction: (String) -> Unit,
) {
    AssistantMessageItem(
        message = message,
        tone = AssistantTone.AI,
        draft = draft,
        mediaGenerationProgress = mediaGenerationProgress,
        selectedTags = selectedTags,
        onAction = onAction,
    )
}
