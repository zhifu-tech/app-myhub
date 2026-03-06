package tech.zhifu.app.myhub.feature.dashboard.content.card

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.StoreKey
import tech.zhifu.app.myhub.datastore.repository.card.cards
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.logger.error

suspend fun DashboardViewModel.loadCards(
    userId: String,
    state: CardSectionState? = null,
    defaultPageSize: Int = 20
) = loadCards(
    userId = userId,
    pageIndex = state?.pageIndex?.let { it + 1 } ?: 1,
    pageSize = state?.pageSize ?: defaultPageSize,
)

@OptIn(ExperimentalStoreApi::class)
private suspend fun DashboardViewModel.loadCards(
    userId: String,
    pageIndex: Int,
    pageSize: Int,
): CardSectionState = runCatching {
    val storeData = cardRepository.getCards(
        userId = userId,
        page = pageIndex,
        size = pageSize,
        sort = StoreKey.Sort.NEWEST,
    )
    val state = (uiState as? DashboardUiState.ResultOutputCompleted)?.cardSectionState
        ?: CardSectionState()
    return state.copy(
        cards = (state.cards + storeData.cards).distinctBy { it.id },
        hasMore = storeData.cards.size == pageSize,
        isLoading = false,
        pageIndex = pageIndex,
        pageSize = pageSize,
    )
}.onFailure {
    logger.error(it) {
        "Failed to load cards from $userId: with pageIndex=$pageIndex, pageSize=$pageSize, as ${it.printStackTrace()}"
    }
}.getOrThrow()
