package tech.zhifu.app.myhub.carddetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.carddetail.components.CardDetailActions
import tech.zhifu.app.myhub.carddetail.components.CardDetailContent
import tech.zhifu.app.myhub.carddetail.components.CardDetailHeader
import tech.zhifu.app.myhub.carddetail.components.CardDetailMetadata
import tech.zhifu.app.myhub.carddetail.components.CardDetailNotes
import tech.zhifu.app.myhub.carddetail.components.CardDetailTags
import tech.zhifu.app.myhub.datastore.repository.ReactiveCardRepository
import tech.zhifu.app.myhub.ui.isCompact
import tech.zhifu.app.myhub.ui.isExpanded
import tech.zhifu.app.myhub.ui.swipeBackGesture
import tech.zhifu.app.myhub.ui.windowSizeClass

/**
 * 卡片详情页主界面
 *
 * @param cardId 卡片 ID
 * @param onNavigateBack 返回导航回调
 */
@Composable
fun CardDetailScreen(
    cardId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = rememberCardDetailViewModel(cardId)
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsState()
    val windowSize = windowSizeClass()

    when (val state = uiState) {
        is CardDetailUiState.Loading -> {
            // 加载状态
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading card...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        is CardDetailUiState.Content -> {
            // 内容状态
            when {
                windowSize.isCompact -> {
                    // 移动端：垂直布局
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .swipeBackGesture(onSwipeBack = onNavigateBack, enabled = true)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CardDetailHeader(
                            onNavigateBack = onNavigateBack,
                            modifier = Modifier.padding(16.dp)
                        )

                        CardDetailContent(
                            card = state.card,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 操作面板（移动端：垂直排列）
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CardDetailActions(
                                card = state.card,
                                isSharing = state.isSharing,
                                showDeleteConfirm = showDeleteConfirm,
                                onShare = { viewModel.shareCard() },
                                onEdit = { viewModel.editCard() },
                                onDelete = { viewModel.showDeleteConfirm() },
                                onConfirmDelete = { viewModel.confirmDelete() },
                                onCancelDelete = { viewModel.cancelDelete() },
                                onCopy = { viewModel.copyContent() }
                            )

                            CardDetailTags(
                                tags = state.card.tags,
                                onAddTag = { /* TODO: 实现添加标签对话框 */ },
                                onRemoveTag = { tag ->
                                    val newTags = state.card.tags.filter { it != tag }
                                    viewModel.updateTags(newTags)
                                }
                            )

                            CardDetailMetadata(card = state.card)

                            CardDetailNotes(
                                notes = "", // TODO: 从 Card 模型获取 notes 字段
                                onNotesChange = { viewModel.updateNotes(it) },
                                isSaving = state.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                windowSize.isExpanded -> {
                    // 桌面端：左右分栏布局
                    androidx.compose.foundation.layout.Row(
                        modifier = modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .swipeBackGesture(onSwipeBack = onNavigateBack, enabled = false) // 桌面端禁用滑动返回
                            .padding(24.dp)
                    ) {
                        // 左侧：卡片内容展示区（8/12 列）
                        Column(
                            modifier = Modifier
                                .weight(8f)
                                .padding(end = 16.dp)
                        ) {
                            CardDetailHeader(
                                onNavigateBack = onNavigateBack
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CardDetailContent(card = state.card)
                        }

                        // 右侧：操作面板（4/12 列）
                        // 在导航之下，和内容区对齐（从 Header 下方开始）
                        // 添加过渡进入动画
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(600, delayMillis = 300, easing = FastOutSlowInEasing)
                            ) + slideInHorizontally(
                                initialOffsetX = { it / 3 },
                                animationSpec = tween(600, delayMillis = 300, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            ) + slideOutHorizontally(
                                targetOffsetX = { it / 3 },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            ),
                            modifier = Modifier.weight(4f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 56.dp) // 与 Header 高度对齐
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardDetailActions(
                                    card = state.card,
                                    isSharing = state.isSharing,
                                    showDeleteConfirm = showDeleteConfirm,
                                    onShare = { viewModel.shareCard() },
                                    onEdit = { viewModel.editCard() },
                                    onDelete = { viewModel.showDeleteConfirm() },
                                    onConfirmDelete = { viewModel.confirmDelete() },
                                    onCancelDelete = { viewModel.cancelDelete() },
                                    onCopy = { viewModel.copyContent() }
                                )

                                CardDetailTags(
                                    tags = state.card.tags,
                                    onAddTag = { /* TODO: 实现添加标签对话框 */ },
                                    onRemoveTag = { tag ->
                                        val newTags = state.card.tags.filter { it != tag }
                                        viewModel.updateTags(newTags)
                                    }
                                )

                                CardDetailMetadata(card = state.card)

                                CardDetailNotes(
                                    notes = "", // TODO: 从 Card 模型获取 notes 字段
                                    onNotesChange = { viewModel.updateNotes(it) },
                                    isSaving = state.isSaving
                                )
                            }
                        }
                    }
                }

                else -> {
                    // Medium 布局（平板）
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .swipeBackGesture(onSwipeBack = onNavigateBack, enabled = true) // 平板也支持滑动返回
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        CardDetailHeader(onNavigateBack = onNavigateBack)
                        Spacer(modifier = Modifier.height(16.dp))
                        CardDetailContent(card = state.card)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 操作面板（平板：垂直排列）
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CardDetailActions(
                                card = state.card,
                                isSharing = state.isSharing,
                                showDeleteConfirm = showDeleteConfirm,
                                onShare = { viewModel.shareCard() },
                                onEdit = { viewModel.editCard() },
                                onDelete = { viewModel.showDeleteConfirm() },
                                onConfirmDelete = { viewModel.confirmDelete() },
                                onCancelDelete = { viewModel.cancelDelete() },
                                onCopy = { viewModel.copyContent() }
                            )

                            CardDetailTags(
                                tags = state.card.tags,
                                onAddTag = { /* TODO: 实现添加标签对话框 */ },
                                onRemoveTag = { tag ->
                                    val newTags = state.card.tags.filter { it != tag }
                                    viewModel.updateTags(newTags)
                                }
                            )

                            CardDetailMetadata(card = state.card)

                            CardDetailNotes(
                                notes = "", // TODO: 从 Card 模型获取 notes 字段
                                onNotesChange = { viewModel.updateNotes(it) },
                                isSaving = state.isSaving
                            )
                        }
                    }
                }
            }
        }

        is CardDetailUiState.Error -> {
            // 错误状态
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                if (state.retryable) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // TODO: 添加重试按钮
                }
            }
        }
    }

    // 删除确认对话框（在 when 表达式外部）
    if (showDeleteConfirm) {
        AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = {
                    Text(text = "Delete Card")
                },
                text = {
                    Text(text = "Are you sure you want to delete this card? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDelete() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = "Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDelete() }) {
                        Text(text = "Cancel")
                    }
                }
            )
    }
}

/**
 * 记住 CardDetailViewModel 实例
 * 使用 cardId 作为 key，确保相同 cardId 使用同一个 ViewModel 实例
 */
@Composable
private fun rememberCardDetailViewModel(cardId: String): CardDetailViewModel {
    val cardRepository: ReactiveCardRepository = koinInject()
    val coroutineScope: CoroutineScope = koinInject()

    return remember(cardId) {
        CardDetailViewModel(
            cardId = cardId,
            cardRepository = cardRepository,
            coroutineScope = coroutineScope
        )
    }
}
