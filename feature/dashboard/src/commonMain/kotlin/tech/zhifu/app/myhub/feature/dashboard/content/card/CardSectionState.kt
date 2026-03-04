package tech.zhifu.app.myhub.feature.dashboard.content.card

import tech.zhifu.app.myhub.datastore.model.domain.Card

data class CardSectionState(
    val cards: List<Card> = emptyList(),
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val pageIndex: Int = 1,
    val pageSize: Int = 20,
)
