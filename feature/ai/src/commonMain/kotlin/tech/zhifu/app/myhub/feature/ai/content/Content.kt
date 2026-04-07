package tech.zhifu.app.myhub.feature.ai.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.item.ActionSectionItem
import tech.zhifu.app.myhub.feature.ai.content.item.MessageBubbleItem
import tech.zhifu.app.myhub.feature.ai.content.item.ProviderModeItem
import tech.zhifu.app.myhub.feature.ai.content.item.ThinkingCardItem
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun Content(
    viewModel: AIViewModel,
    contentPadding: PaddingValues,
) {
    val messages by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.messages.orEmpty()
    }
    ContentContent(
        messages = messages,
        contentPadding = contentPadding,
        providerModeItem = {
            ProviderModeItem(viewModel)
        },
        thinkingCardItem = {
            ThinkingCardItem(viewModel)
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
    providerModeItem: @Composable LazyItemScope.() -> Unit,
    thinkingCardItem: @Composable LazyItemScope.() -> Unit,
    actionSectionItem: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(color = MaterialTheme.colorScheme.surfaceVariant),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = contentPadding,
    ) {
        item { providerModeItem() }
        item { thinkingCardItem() }
        items(
            items = messages,
            key = { it.id },
            contentType = { "message" }
        ) { msg ->
            MessageBubbleItem(message = msg)
        }
        item {
            actionSectionItem()
        }
    }
}

