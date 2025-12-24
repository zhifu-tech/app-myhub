package tech.zhifu.app.myhub.datastore.ui

import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Statistics

/**
 * 卡片列表UI状态
 */
data class CardListUiState(
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCardType: CardType? = null,
    val selectedTags: List<String> = emptyList(),
    val showFavoritesOnly: Boolean = false
)

/**
 * 卡片详情UI状态
 */
data class CardDetailUiState(
    val card: Card? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false
)

/**
 * Dashboard UI状态
 */
data class DashboardUiState(
    val statistics: Statistics = Statistics(),
    val recentCards: List<Card> = emptyList(),
    val favoriteCards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastSyncTime: Long? = null
)

/**
 * 搜索UI状态
 */
data class SearchUiState(
    val query: String = "",
    val results: List<Card> = emptyList(),
    val isSearching: Boolean = false,
    val hasResults: Boolean = false
)

