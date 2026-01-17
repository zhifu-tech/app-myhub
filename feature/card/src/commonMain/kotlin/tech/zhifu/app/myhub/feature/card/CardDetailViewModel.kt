package tech.zhifu.app.myhub.feature.card

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.repository.ReactiveCardRepository

/**
 * 卡片详情页 ViewModel
 * 管理详情页的状态和业务逻辑
 *
 * @param cardId 卡片 ID（作为构造参数，确保生命周期正确）
 * @param cardRepository 卡片仓库
 * @param coroutineScope 协程作用域（由 Koin 注入）
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CardDetailViewModel(
    private val cardId: String,
    private val cardRepository: ReactiveCardRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<CardDetailUiState>(
        CardDetailUiState.Loading(cardId = cardId)
    )
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    // 删除确认对话框状态
    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    // 笔记输入流（用于防抖保存）
    private val notesInputFlow = MutableStateFlow<String>("")

    init {
        // 初始化时开始观察卡片数据
        observeCard()

        // 笔记防抖保存（500ms debounce + distinctUntilChanged）
        notesInputFlow
            .debounce(500)
            .distinctUntilChanged()
            .onEach { notes ->
                saveNotes(notes)
            }
            .catch { e ->
                // 处理保存错误
                updateError("Failed to save notes: ${e.message}")
            }
            .launchIn(coroutineScope)
    }

    /**
     * 观察卡片数据变化（响应式）
     *
     * 关键设计：
     * - 当 card 为 null 时，明确处理为"卡片不存在或已删除"的错误状态
     * - 避免使用 `card ?: return@onEach` 吞掉删除语义
     */
    private fun observeCard() {
        cardRepository.observeCard(cardId)
            .onEach { card ->
                if (card == null) {
                    // 卡片不存在或已被删除，明确转换为错误状态
                    _uiState.value = CardDetailUiState.Error(
                        message = "Card not found or deleted",
                        cardId = cardId,
                        retryable = false  // 已删除的卡片不可重试
                    )
                } else {
                    // 卡片存在，更新状态
                    _uiState.value = when (val current = _uiState.value) {
                        is CardDetailUiState.Loading ->
                            CardDetailUiState.Content(card = card)
                        is CardDetailUiState.Content ->
                            current.copy(card = card)
                        is CardDetailUiState.Error ->
                            CardDetailUiState.Content(card = card)
                    }
                }
            }
            .catch { e ->
                _uiState.value = CardDetailUiState.Error(
                    message = e.message ?: "Unknown error",
                    cardId = cardId,
                    retryable = true
                )
            }
            .launchIn(coroutineScope)
    }

    /**
     * 显示删除确认对话框
     */
    fun showDeleteConfirm() {
        _showDeleteConfirm.value = true
    }

    /**
     * 取消删除
     */
    fun cancelDelete() {
        _showDeleteConfirm.value = false
    }

    /**
     * 确认删除卡片
     */
    fun confirmDelete() {
        coroutineScope.launch {
            try {
                cardRepository.deleteCard(cardId)
                _showDeleteConfirm.value = false
                // 删除后，observeCard 会收到 null，自动转换为错误状态
            } catch (e: Exception) {
                updateError("Failed to delete card: ${e.message}")
                _showDeleteConfirm.value = false
            }
        }
    }

    /**
     * 分享卡片
     */
    fun shareCard() {
        // TODO: 实现分享逻辑（未来扩展）
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(isSharing = true)
            // 模拟分享操作
            coroutineScope.launch {
                delay(1000)
                _uiState.value = currentState.copy(isSharing = false)
            }
        }
    }

    /**
     * 编辑卡片
     */
    fun editCard() {
        // TODO: 导航到编辑页面或显示编辑对话框
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite() {
        coroutineScope.launch {
            try {
                cardRepository.toggleFavorite(cardId)
                // 状态会自动更新（通过 observeCard）
            } catch (e: Exception) {
                updateError("Failed to toggle favorite: ${e.message}")
            }
        }
    }

    /**
     * 更新标签
     */
    fun updateTags(tags: List<String>) {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            coroutineScope.launch {
                try {
                    val updatedCard = currentState.card.copy(tags = tags)
                    cardRepository.updateCard(updatedCard)
                    // 状态会自动更新（通过 observeCard）
                } catch (e: Exception) {
                    updateError("Failed to update tags: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新个人笔记（UI 层调用，内部会防抖保存）
     *
     * 数据流设计原则：
     * - UI 层：立即更新 ViewModel State（用户看到即时反馈）
     * - ViewModel State：是 UI 展示的"真源"（Single Source of Truth for UI）
     * - Repository：是最终一致性存储（通过防抖流异步保存）
     *
     * 这样设计的好处：
     * - 用户体验：输入即时反馈，无延迟感
     * - 性能：防抖减少数据库写入频率
     * - 一致性：UI 始终以 ViewModel State 为准，不受 Repository 写入时机影响
     */
    fun updateNotes(notes: String) {
        notesInputFlow.value = notes
        // UI 层立即更新显示，实际保存通过防抖流处理
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            // 假设 Card 有 notes 字段，或使用 metadata
            // 这里暂时只更新 UI 状态，实际保存由防抖流处理
            // TODO: 根据实际 Card 模型调整
        }
    }

    /**
     * 实际保存笔记到 Repository（由防抖流调用）
     *
     * 注意：此方法由防抖流异步调用，不影响 UI 的即时显示
     */
    private suspend fun saveNotes(notes: String) {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(isSaving = true)
            try {
                // TODO: 根据实际 Card 模型更新笔记字段
                // 目前 Card 模型可能没有 notes 字段，需要确认
                val updatedCard = currentState.card.copy(
                    // 如果 Card 有 notes 字段，在这里更新
                    // 或者使用 metadata 存储
                )
                cardRepository.updateCard(updatedCard)
                _uiState.value = currentState.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    error = "Failed to save notes: ${e.message}"
                )
            }
        }
    }

    /**
     * 复制卡片内容
     */
    fun copyContent() {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            // TODO: 实现复制到剪贴板功能（需要平台特定实现）
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(error = null)
        }
    }

    /**
     * 更新错误状态
     */
    private fun updateError(message: String) {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(error = message)
        }
    }

}
