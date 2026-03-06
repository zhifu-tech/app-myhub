
package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.auth.api.session.AuthSessionCoordinator
import tech.zhifu.app.myhub.feature.dashboard.content.card.loadCards
import tech.zhifu.app.myhub.feature.dashboard.content.collection.loadCollections
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger

class DashboardViewModel(
    val logger: Logger = logger("Dashboard"),
    internal val cardRepository: CardRepository,
    internal val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository,
    private val authSessionCoordinator: AuthSessionCoordinator,
) : ContainerHost<DashboardUiState, DashboardSideEffect>, ViewModel() {

    override val container: Container<DashboardUiState, DashboardSideEffect> = container(
        initialState = DashboardUiState.InitGlobalPending
    ) {
        initInternal()
    }
    val uiState: DashboardUiState
        get() = container.stateFlow.value

    @Composable
    fun <R> collectFieldAsState(
        selector: (DashboardUiState) -> R,
    ): State<R> = container.stateFlow.map(selector)
        .distinctUntilChanged()
        .collectAsState(initial = selector(uiState))

    fun retry() {
        viewModelScope.launch {
            logger.debug { "Retrying dashboard" }
            initInternal()
        }
    }

    fun refresh() {
        logger.debug { "Refreshing dashboard" }
        uiState.let { it as? DashboardUiState.ResultOutputCompleted }
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
        uiState.let { it as? DashboardUiState.ResultOutputCompleted }
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

        uiState.let { it as? DashboardUiState.ResultOutputCompleted }
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
                DashboardUiState.InitGlobalPending
            }
        }
        try {
            val (cardSectionState, collectionSectionState) = coroutineScope {
                val cardsDeferred = async {
                    loadCards(userId = userId)
                }
                val collectionsDeferred = async {
                    loadCollections(userId = userId)
                }
                cardsDeferred.await() to collectionsDeferred.await()
            }
            intent {
                reduce {
                    DashboardUiState.ResultOutputCompleted(
                        source = "init",
                        cardSectionState = cardSectionState,
                        collectionSectionState = collectionSectionState,
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize dashboard" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    DashboardUiState.ResultErrorDisabled(
                        message = e.message ?: "初始化失败，请重试",
                        canRetry = true,
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
                val state = state as? DashboardUiState.ResultOutputCompleted
                    ?: return@reduce state
                state.copy(
                    source = "refresh:pre",
                    isRefreshing = true
                )
            }
        }
        try {
            val (cardSectionState, collectionSectionState) = coroutineScope {
                val cardsDeferred = async {
                    loadCards(userId = userId)
                }
                val collectionsDeferred = async {
                    loadCollections(userId = userId)
                }
                cardsDeferred.await() to collectionsDeferred.await()
            }

            intent {
                reduce {
                    val state = state as? DashboardUiState.ResultOutputCompleted
                        ?: return@reduce state
                    state.copy(
                        source = "refresh",
                        isRefreshing = false,
                        cardSectionState = cardSectionState,
                        collectionSectionState = collectionSectionState,
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to refresh dashboard" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    val state = state as? DashboardUiState.ResultOutputCompleted
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
                val state = state as? DashboardUiState.ResultOutputCompleted
                    ?: return@reduce state
                state.copy(
                    source = "loadMoreCards:pre",
                    cardSectionState = state.cardSectionState.copy(
                        isLoading = true
                    )
                )
            }
        }
        try {
            val newCardState = loadCards(
                userId = userId,
                state = (uiState as? DashboardUiState.ResultOutputCompleted)?.cardSectionState
            )
            intent {
                reduce {
                    val state = state as? DashboardUiState.ResultOutputCompleted
                        ?: return@reduce state
                    state.copy(
                        source = "loadMoreCards",
                        cardSectionState = newCardState,
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load more cards" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    val state = state as? DashboardUiState.ResultOutputCompleted
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
                val state = state as? DashboardUiState.ResultOutputCompleted
                    ?: return@reduce state
                state.copy(
                    source = "loadMoreCollections:pre",
                    collectionSectionState = state.collectionSectionState.copy(
                        isLoading = true
                    )
                )
            }
        }
        try {
            val newCollectionState = loadCollections(
                userId = userId,
                state = (uiState as? DashboardUiState.ResultOutputCompleted)?.collectionSectionState
            )
            intent {
                reduce {
                    val state = state as? DashboardUiState.ResultOutputCompleted
                        ?: return@reduce state
                    state.copy(
                        source = "loadMoreCollections",
                        collectionSectionState = newCollectionState
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load more collections" }
            intent {
                if (isUnauthorizedError(e)) {
                    navigateToAuth()
                }
                reduce {
                    val state = state as? DashboardUiState.ResultOutputCompleted
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

    private fun isUnauthorizedError(e: Exception): Boolean = e.message?.let {
        it.contains("401") || it.contains("Unauthorized", ignoreCase = true)
    } ?: false
}
