package tech.zhifu.app.myhub.feature.dashboard.content.collection

import org.mobilenativefoundation.store.core5.StoreKey
import tech.zhifu.app.myhub.datastore.repository.collection.collections
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.logger.error

suspend fun DashboardViewModel.loadCollections(
    userId: String,
    state: CollectionSectionState? = null,
    defaultPageSize: Int = 10
) = loadCollections(
    userId = userId,
    pageIndex = state?.pageIndex ?: 1,
    pageSize = state?.pageSize ?: defaultPageSize,
)

internal suspend fun DashboardViewModel.loadCollections(
    userId: String,
    pageIndex: Int,
    pageSize: Int,
): CollectionSectionState = runCatching {
    val storeData = collectionRepository.getCollections(
        userId = userId,
        page = pageIndex,
        size = pageSize,
        sort = StoreKey.Sort.NEWEST,
    )
    val state = (uiState as? DashboardUiState.ResultOutputCompleted)?.collectionSectionState
        ?: CollectionSectionState()
    return state.copy(
        collections = (storeData.collections + state.collections).distinctBy { it.id },
        hasMore = storeData.collections.size == pageSize,
        isLoading = false,
        pageIndex = pageIndex,
        pageSize = pageSize,
    )
}.onFailure {
    logger.error(it) {
        "Failed to load ${userId}'s collections, with pageIndex=$pageIndex, pageSize=$pageSize, as ${it.printStackTrace()}"
    }
}.getOrThrow()
