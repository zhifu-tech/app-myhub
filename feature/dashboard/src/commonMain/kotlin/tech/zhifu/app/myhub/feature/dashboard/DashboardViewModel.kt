@file:OptIn(ExperimentalStoreApi::class)

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
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
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
        initialState = DashboardUiState(
            state = DashboardState.DASHBOARD_INIT_GLOBAL_PENDING,
            payload = null,
        )
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

    private suspend fun initInternal() {
        logger.debug { "Init dashboard" }
        val userId = userRepository.getUserOrNull()?.id ?: run {
            navigateToAuth()
            return
        }
        intent {
            reduce {
                state.copy(
                    state = DashboardState.DASHBOARD_INIT_GLOBAL_PENDING,
                    payload = null,
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
                    state.copy(
                        state = DashboardState.DASHBOARD_RESULT_OUTPUT_COMPLETED,
                        payload = DashboardPayload.ResultOutputCompletedPayload(
                            collectionSectionState = collectionSectionState,
                            cardSectionState = cardSectionState,
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize dashboard" }
            intent {
                reduce {
                    state.copy(
                        state = DashboardState.DASHBOARD_RESULT_ERROR_DISABLED,
                        payload = DashboardPayload.ResultErrorDisabledPayload(
                            message = e.message ?: "初始化失败，请重试",
                            canRetry = true,
                        )
                    )
                }
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            logger.debug { "Retrying dashboard" }
            initInternal()
        }
    }

    fun refresh() {
        logger.debug { "Refreshing dashboard" }
        val payload = uiState.resultCompletedPayload ?: run {
            retry()
            return
        }
        if (payload.isRefreshing) return
        viewModelScope.launch {
            val userId = userRepository.getUserOrNull()?.id ?: run {
                navigateToAuth()
                return@launch
            }
            intent {
                reduce {
                    val currentPayload = state.resultCompletedPayload
                        ?: return@reduce state
                    state.copy(
                        payload = currentPayload.copy(
                            isRefreshing = true
                        )
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
                        val currentPayload = state.resultCompletedPayload ?: return@reduce state
                        state.copy(
                            payload = currentPayload.copy(
                                isRefreshing = false,
                                cardSectionState = cardSectionState,
                                collectionSectionState = collectionSectionState,
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to refresh dashboard" }
                intent {
                    reduce {
                        val currentPayload = state.resultCompletedPayload ?: return@reduce state
                        state.copy(
                            payload = currentPayload.copy(
                                isRefreshing = false
                            )
                        )
                    }
                }
            }
        }
    }

    fun navigateToAuth() = intent {
        postSideEffect(DashboardSideEffect.NavigateToAuth)
    }

    fun navigateToCardDetail(cardId: String) = intent {
        postSideEffect(DashboardSideEffect.NavigateToCardDetail(cardId))
    }

    fun navigateToCapture() = intent {
        postSideEffect(DashboardSideEffect.NavigateToCapture)
    }

    fun dismissFocusReview() = intent {
        reduce {
            state/*.copy(showFocusReview = false)*/
        }
    }

    fun startReview() {
        logger.debug { "Start review flow" }
        // TODO: 导航到复习页面
    }

    fun editCard(cardId: String) {
        logger.debug { "Edit card: $cardId" }
    }

    fun loadMoreCards() {
        logger.debug { "Dashboard load more cards" }

        uiState.resultCompletedPayload
            ?.takeUnless { it.isRefreshing }
            ?.cardSectionState
            ?.takeUnless { it.isLoading || !it.hasMore }
            ?: return

        viewModelScope.launch {
            val userId = userRepository.getUserOrNull()?.id ?: return@launch
            intent {
                reduce {
                    val payload = state.resultCompletedPayload ?: return@reduce state
                    state.copy(
                        payload = payload.copy(
                            cardSectionState = payload.cardSectionState.copy(
                                isLoading = true
                            )
                        )
                    )
                }
            }
            try {
                val newCardState = loadCards(
                    userId = userId,
                    state = uiState.resultCompletedPayload?.cardSectionState
                )
                intent {
                    reduce {
                        val currentPayload = state.resultCompletedPayload ?: return@reduce state
                        state.copy(
                            payload = currentPayload.copy(
                                cardSectionState = newCardState,
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load more cards" }
                intent {
                    reduce {
                        val currentPayload = state.resultCompletedPayload ?: return@reduce state
                        state.copy(
                            payload = currentPayload.copy(
                                cardSectionState = currentPayload.cardSectionState.copy(
                                    isLoading = false
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadMoreCollections() {
        logger.debug { "Dashboard load more collections" }

        uiState.resultCompletedPayload
            ?.takeUnless { it.isRefreshing }
            ?.collectionSectionState
            ?.takeUnless { it.isLoading || !it.hasMore }
            ?: return

        viewModelScope.launch {
            val userId = userRepository.getUserOrNull()?.id ?: run {
                navigateToAuth()
                return@launch
            }
            intent {
                reduce {
                    val currentPayload = state.resultCompletedPayload ?: return@reduce state
                    state.copy(
                        payload = currentPayload.copy(
                            collectionSectionState = currentPayload.collectionSectionState.copy(
                                isLoading = true
                            )
                        )
                    )
                }
            }
            try {
                val newCollectionState = loadCollections(
                    userId = userId,
                    state = uiState.resultCompletedPayload?.collectionSectionState
                )
                intent {
                    reduce {
                        val payload = state.resultCompletedPayload ?: return@reduce state
                        state.copy(
                            payload = payload.copy(
                                collectionSectionState = newCollectionState
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load more collections" }
                intent {
                    reduce {
                        val payload = state.resultCompletedPayload ?: return@reduce state
                        state.copy(
                            payload = payload.copy(
                                collectionSectionState = payload.collectionSectionState.copy(
                                    isLoading = false,
                                    errorMessage = ""
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}
