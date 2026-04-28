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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.item.AssistantMessageItem
import tech.zhifu.app.myhub.feature.ai.content.item.MessageUserItem
import tech.zhifu.app.myhub.feature.ai.content.item.ProviderModeItem
import tech.zhifu.app.myhub.feature.ai.content.item.ReasoningCardItem
import tech.zhifu.app.myhub.feature.ai.content.item.ReasoningTraceCard
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun Content(
    viewModel: AIViewModel,
    contentPadding: PaddingValues,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { content ->
            ContentState(
                messages = content.context.messages,
                draft = content.context.draft,
                mediaGenerationProgress = content.context.mediaGenerationProgress,
            )
        }
    }
    val safeState = state ?: return
    ContentContent(
        messages = safeState.messages,
        contentPadding = contentPadding,
        providerModeItem = {
            ProviderModeItem(viewModel)
        },
        reasoningCardItem = {
            ReasoningCardItem(viewModel)
        },
        assistantMessageItem = { message ->
            AssistantMessageItem(
                viewModel = viewModel,
                draft = safeState.draft,
                mediaGenerationProgress = safeState.mediaGenerationProgress,
                message = message,
            )
        },
    )
}

@Composable
fun ContentContent(
    messages: List<Message>,
    contentPadding: PaddingValues,
    providerModeItem: @Composable BoxScope.() -> Unit,
    reasoningCardItem: @Composable BoxScope.() -> Unit,
    assistantMessageItem: @Composable BoxScope.(Message) -> Unit,
) {
    val listState = rememberLazyListState()
    var shouldStickToBottom by remember { mutableStateOf(true) }
    var didInitialScroll by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            if (lastVisibleIndex == null) {
                null
            } else {
                lastVisibleIndex >= (layoutInfo.totalItemsCount - 2).coerceAtLeast(0)
            }
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { shouldStickToBottom = it }
    }

    LaunchedEffect(messages.lastOrNull()?.id, messages.size) {
        if (messages.isEmpty()) return@LaunchedEffect
        val targetIndex = messages.size
        if (!didInitialScroll) {
            didInitialScroll = true
            listState.scrollToItem(index = targetIndex)
            return@LaunchedEffect
        }
        if (shouldStickToBottom) {
            listState.animateScrollToItem(index = targetIndex)
        }
    }
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
            Box(modifier = Modifier.fillMaxWidth(), content = providerModeItem)
        }
        item(key = "reasoning_card") {
            Box(modifier = Modifier.fillMaxWidth(), content = reasoningCardItem)
        }
        items(
            items = messages,
            key = { it.id },
            contentType = { "message" }
        ) { msg ->
            Box(modifier = Modifier.fillMaxWidth()) {
                when (msg.role) {
                    Message.Role.AI,
                    Message.Role.SYSTEM -> assistantMessageItem(msg)

                    Message.Role.USER -> MessageUserItem(msg)
                    Message.Role.THINKING -> ReasoningTraceCard(
                        text = msg.textRes?.let { res ->
                            stringResource(
                                resource = res,
                                formatArgs = msg.textArgs.toTypedArray(),
                            )
                        } ?: msg.text,
                        live = false,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        persistentKey = "reasoning-${msg.id}",
                    )
                }
            }
        }
    }
}

private data class ContentState(
    val messages: List<Message>,
    val draft: CaptureDraft,
    val mediaGenerationProgress: ProviderImageGenerationProgress?,
)
