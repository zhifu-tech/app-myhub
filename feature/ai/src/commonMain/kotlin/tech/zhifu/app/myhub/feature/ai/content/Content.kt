package tech.zhifu.app.myhub.feature.ai.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.item.ActionSectionItem
import tech.zhifu.app.myhub.feature.ai.content.item.MessageAIItem
import tech.zhifu.app.myhub.feature.ai.content.item.MessageSystemItem
import tech.zhifu.app.myhub.feature.ai.content.item.MessageThinkingItem
import tech.zhifu.app.myhub.feature.ai.content.item.MessageUserItem
import tech.zhifu.app.myhub.feature.ai.content.item.ProviderModeItem
import tech.zhifu.app.myhub.feature.ai.content.item.ReasoningCardItem
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun Content(
    viewModel: AIViewModel,
    contentPadding: PaddingValues,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.messages
    }
    val safeState = state ?: return
    logger.debug { "Content: message.size = ${safeState.size}" }
    ContentContent(
        messages = safeState,
        contentPadding = contentPadding,
        providerModeItem = {
            ProviderModeItem(viewModel)
        },
        reasoningCardItem = {
            ReasoningCardItem(viewModel)
        },
        uploadedMediaInlineItem = {
//            UploadedMediaInlineItem(viewModel)
        },
        actionSectionItem = {
            ActionSectionItem(viewModel)
        }
    )
}

@Composable
fun ContentContent(
    messages: List<Message>,
    contentPadding: PaddingValues,
    providerModeItem: @Composable BoxScope.() -> Unit,
    reasoningCardItem: @Composable BoxScope.() -> Unit,
    uploadedMediaInlineItem: @Composable BoxScope.() -> Unit,
    actionSectionItem: @Composable BoxScope.() -> Unit,
) {
    val listState = rememberLazyListState()
//    LaunchedEffect(messages.lastOrNull()?.id, messages.size) {
//        val lastIndex = messages.size + 2 + if (mediaAssets.isNotEmpty() && mediaManageEnabled) 1 else 0
//        listState.animateScrollToItem(index = lastIndex)
//    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface,
                    )
                )
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentPadding,
    ) {
        item(key = "provider_mode") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                content = providerModeItem,
            )
        }
        items(
            items = messages,
            key = { it.id },
            contentType = { "message" }
        ) { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
            ) {
                when (msg.role) {
                    Message.Role.AI -> MessageAIItem(msg)
                    Message.Role.USER -> MessageUserItem(msg)
                    Message.Role.SYSTEM -> MessageSystemItem(msg)
                    Message.Role.THINKING -> MessageThinkingItem(msg)
                }
            }
        }
        item(key = "reasoning_card") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                content = reasoningCardItem,
            )
        }
        item(key = "uploaded_media_inline") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                content = uploadedMediaInlineItem,
            )
        }
        item(key = "action_section") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                content = actionSectionItem,
            )
        }
    }
}
