package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.feature.dashboard.content.card.CardSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.review.ReviewState
import tech.zhifu.app.myhub.ui.State

sealed class DashboardUiState(
    val state: State,
) {
    object InitGlobalPending : DashboardUiState(
        state = State.initGlobalLoading(
            module = State.Module.DASHBOARD
        )
    )

    data class ResultOutputCompleted(
        val source: String = "Unknown",
        val isRefreshing: Boolean = false,
        val reviewState: ReviewState? = null,
        val collectionSectionState: CollectionSectionState,
        val cardSectionState: CardSectionState,
    ) : DashboardUiState(
        state = State.resultOutputCompleted(
            module = State.Module.DASHBOARD
        )
    )

    data class ResultErrorDisabled(
        val message: String = "",
        val canRetry: Boolean = true,
    ) : DashboardUiState(
        state = State.resultErrorDisabled(
            module = State.Module.DASHBOARD
        )
    )
}
