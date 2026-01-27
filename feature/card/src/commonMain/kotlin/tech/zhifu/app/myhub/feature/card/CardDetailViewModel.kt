package tech.zhifu.app.myhub.feature.card

import kotlinx.coroutines.CoroutineScope
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
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.card.card

/**
 * 卡片详情页 ViewModel
 */
@OptIn(FlowPreview::class)
class CardDetailViewModel(
    private val cardId: String,
    private val cardRepository: CardRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<CardDetailUiState>(
        CardDetailUiState.Loading(cardId = cardId)
    )
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    private val notesInputFlow = MutableStateFlow<String>("")

    init {
        observeCard()

        notesInputFlow
            .debounce(500)
            .distinctUntilChanged()
            .onEach { notes -> saveNotes(notes) }
            .catch { e -> updateError("Failed to save notes: ${e.message}") }
            .launchIn(coroutineScope)
    }

    private fun observeCard() {
        cardRepository.streamCard(cardId, refresh = true)
            .onEach { response ->
                when (response) {
                    is StoreReadResponse.Loading -> {
                        // 保持 Loading 状态
                    }

                    is StoreReadResponse.Data -> {
                        val storeData = response.value
                        val card = storeData.card ?: return@onEach
                        _uiState.value = when (val current = _uiState.value) {
                            is CardDetailUiState.Loading ->
                                CardDetailUiState.Content(card = card)

                            is CardDetailUiState.Content ->
                                current.copy(card = card)

                            is CardDetailUiState.Error ->
                                CardDetailUiState.Content(card = card)
                        }
                    }

                    is StoreReadResponse.NoNewData -> {
                        if (_uiState.value is CardDetailUiState.Loading) {
                            _uiState.value = CardDetailUiState.Error(
                                message = "Card not found",
                                cardId = cardId,
                                retryable = true
                            )
                        }
                    }

                    is StoreReadResponse.Error -> {
                        val errorMessage = response.errorMessageOrNull() ?: "Unknown error"
                        _uiState.value = CardDetailUiState.Error(
                            message = errorMessage,
                            cardId = cardId,
                            retryable = true
                        )
                    }

                    is StoreReadResponse.Initial -> {
                        // 初始状态
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

    fun showDeleteConfirm() {
        _showDeleteConfirm.value = true
    }

    fun cancelDelete() {
        _showDeleteConfirm.value = false
    }

    fun confirmDelete() {
        coroutineScope.launch {
            try {
                cardRepository.clearCard(
                    cardId = cardId
                )
                _showDeleteConfirm.value = false
                _uiState.value = CardDetailUiState.Error(
                    message = "Card deleted",
                    cardId = cardId,
                    retryable = false
                )
            } catch (e: Exception) {
                updateError("Failed to delete card: ${e.message}")
                _showDeleteConfirm.value = false
            }
        }
    }

    fun shareCard() {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(isSharing = true)
            coroutineScope.launch {
                delay(1000)
                _uiState.value = currentState.copy(isSharing = false)
            }
        }
    }

    fun editCard() {
        // TODO: 导航到编辑页面
    }

    fun toggleFavorite() {
        // TODO: 实现收藏切换
    }

    fun updateTags(tags: List<String>) {
        // TODO: 实现标签更新
    }

    fun updateNotes(notes: String) {
        notesInputFlow.value = notes
    }

    private suspend fun saveNotes(notes: String) {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(isSaving = true)
            try {
                // TODO: 保存笔记
                _uiState.value = currentState.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    error = "Failed to save notes: ${e.message}"
                )
            }
        }
    }

    fun copyContent() {
        // TODO: 复制到剪贴板
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(error = null)
        }
    }

    private fun updateError(message: String) {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(error = message)
        }
    }
}
