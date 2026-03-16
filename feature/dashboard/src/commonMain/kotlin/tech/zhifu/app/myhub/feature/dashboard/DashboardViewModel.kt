package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.dashboard.content.card.CardSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.card.loadCards
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.collection.loadCollections
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.util.ViewModelContainerHost

class DashboardViewModel(
    val logger: Logger = logger("Dashboard"),
    internal val cardRepository: CardRepository,
    internal val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository,
) : ViewModelContainerHost<DashboardUiState, DashboardSideEffect>() {

    override val container = container<DashboardUiState, DashboardSideEffect>(
        // fixme 替换为很正的初始化状态
        initialState = DashboardUiState.Content(
            collectionSectionState = CollectionSectionState(),
            cardSectionState = CardSectionState(),
            searchState = SearchState(),
        )
    )

    // TODO FOLLOWING CODE SHOULD BE CHECK
    //
    fun retry() {
        viewModelScope.launch {
            logger.debug { "Retrying dashboard" }
            initInternal()
        }
    }

    fun refresh() {
        logger.debug { "Refreshing dashboard" }
        uiState.let { it as? DashboardUiState.Content }
            ?.takeUnless { it.isRefreshing }
            ?: return
        viewModelScope.launch {
            refreshInternal()
        }
    }

    fun dismissFocusReview() = intent {
        reduce {
            state/*.copy(showFocusReview = false)*/
        }
    }

    fun loadMoreCards() {
        logger.debug { "Dashboard load more cards" }
        uiState.let { it as? DashboardUiState.Content }
            ?.takeUnless { it.isRefreshing }
            ?.cardSectionState
            ?.takeUnless { it.isLoading || !it.hasMore }
            ?: return
        viewModelScope.launch {
            loadMoreCardsInternal()
        }
    }

    fun loadMoreCollections() {
        logger.debug { "Dashboard load more collections" }

        uiState.let { it as? DashboardUiState.Content }
            ?.takeUnless { it.isRefreshing }
            ?.collectionSectionState
            ?.takeUnless { it.isLoading || !it.hasMore }
            ?: return

        viewModelScope.launch {
            loadMoreCollectionsInternal()
        }
    }

    private suspend fun initInternal() {
        val userId = userRepository.getUserOrNull()?.id ?: run {
            navigateToAuth()
            return
        }
        intent {
            reduce {
                DashboardUiState.Loading
            }
        }
        runCatching {
            coroutineScope {
                val cardsDeferred = async {
                    loadCards(userId = userId)
                }
                val collectionsDeferred = async {
                    loadCollections(userId = userId)
                }
                cardsDeferred.await() to collectionsDeferred.await()
            }
        }.onSuccess { (cardSectionState, collectionSectionState) ->
            intent {
                reduce {
                    DashboardUiState.Content(
                        source = "init",
                        cardSectionState = cardSectionState,
                        collectionSectionState = collectionSectionState,
                        searchState = SearchState(),
                    )
                }
            }
        }.onFailure { e ->
            logger.error(e) { "Failed to initialize dashboard" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    DashboardUiState.Error(
                        message = e.message ?: "初始化失败，请重试",
                    )
                }
            }
        }
    }

    private suspend fun refreshInternal() {
        val userId = userRepository.getUserOrNull()?.id ?: run {
            navigateToAuth()
            return
        }
        intent {
            reduce {
                val state = state as? DashboardUiState.Content
                    ?: return@reduce state
                state.copy(
                    source = "refresh:pre",
                    isRefreshing = true
                )
            }
        }
        runCatching {
            coroutineScope {
                val cardsDeferred = async {
                    loadCards(userId = userId)
                }
                val collectionsDeferred = async {
                    loadCollections(userId = userId)
                }
                cardsDeferred.await() to collectionsDeferred.await()
            }
        }.onSuccess { (cardSectionState, collectionSectionState) ->
            intent {
                reduce {
                    val state = state as? DashboardUiState.Content
                        ?: return@reduce state
                    state.copy(
                        source = "refresh",
                        isRefreshing = false,
                        cardSectionState = cardSectionState,
                        collectionSectionState = collectionSectionState,
                    )
                }
            }
        }.onFailure { e ->
            logger.error(e) { "Failed to refresh dashboard" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    val state = state as? DashboardUiState.Content
                        ?: return@reduce state
                    state.copy(
                        source = "refresh:catch",
                        isRefreshing = false
                    )
                }
            }
        }
    }

    private suspend fun loadMoreCardsInternal() {
        val userId = userRepository.getUserOrNull()?.id ?: run {
            navigateToAuth()
            return
        }
        intent {
            reduce {
                val state = state as? DashboardUiState.Content
                    ?: return@reduce state
                state.copy(
                    source = "loadMoreCards:pre",
                    cardSectionState = state.cardSectionState.copy(
                        isLoading = true
                    )
                )
            }
        }
        runCatching {
            loadCards(
                userId = userId,
                state = (uiState as? DashboardUiState.Content)?.cardSectionState
            )
        }.onSuccess { newCardState ->
            intent {
                reduce {
                    val state = state as? DashboardUiState.Content
                        ?: return@reduce state
                    state.copy(
                        source = "loadMoreCards",
                        cardSectionState = newCardState,
                    )
                }
            }
        }.onFailure { e ->
            logger.error(e) { "Failed to load more cards" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    val state = state as? DashboardUiState.Content
                        ?: return@reduce state
                    state.copy(
                        source = "loadMoreCards:catch",
                        cardSectionState = state.cardSectionState.copy(
                            isLoading = false
                        )
                    )
                }
            }
        }
    }

    private suspend fun loadMoreCollectionsInternal() {
        val userId = userRepository.getUserOrNull()?.id ?: run {
            navigateToAuth()
            return
        }
        intent {
            reduce {
                val state = state as? DashboardUiState.Content
                    ?: return@reduce state
                state.copy(
                    source = "loadMoreCollections:pre",
                    collectionSectionState = state.collectionSectionState.copy(
                        isLoading = true
                    )
                )
            }
        }
        runCatching {
            loadCollections(
                userId = userId,
                state = (uiState as? DashboardUiState.Content)?.collectionSectionState
            )
        }.onSuccess { newCollectionState ->
            intent {
                reduce {
                    val state = state as? DashboardUiState.Content
                        ?: return@reduce state
                    state.copy(
                        source = "loadMoreCollections",
                        collectionSectionState = newCollectionState
                    )
                }
            }
        }.onFailure { e ->
            logger.error(e) { "Failed to load more collections" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    val state = state as? DashboardUiState.Content
                        ?: return@reduce state
                    state.copy(
                        source = "loadMoreCollections:catch",
                        collectionSectionState = state.collectionSectionState.copy(
                            isLoading = false
                        )
                    )
                }
            }
        }
    }

    private fun isUnauthorizedError(e: Throwable): Boolean = e.message?.let {
        it.contains("401") || it.contains("Unauthorized", ignoreCase = true)
    } ?: false
}
