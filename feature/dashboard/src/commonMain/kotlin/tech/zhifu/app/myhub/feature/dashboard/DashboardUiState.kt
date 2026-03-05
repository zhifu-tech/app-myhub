package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.feature.dashboard.content.card.CardSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.review.ReviewState
import tech.zhifu.app.myhub.ui.State

sealed class DashboardUiState(
    val state: State,
) {
    object InitGlobalPending : DashboardUiState(
        state = DASHBOARD_INIT_GLOBAL_PENDING
    )

    data class ResultOutputCompleted(
        val isRefreshing: Boolean = false,
        val reviewState: ReviewState? = null,
        val collectionSectionState: CollectionSectionState,
        val cardSectionState: CardSectionState,
    ) : DashboardUiState(DASHBOARD_RESULT_OUTPUT_COMPLETED)

    data class ResultErrorDisabled(
        val message: String = "",
        val canRetry: Boolean = true,
    ) : DashboardUiState(DASHBOARD_RESULT_ERROR_DISABLED)

    companion object {
        val DASHBOARD_INIT_GLOBAL_PENDING = State(
            module = State.Module.DASHBOARD,
            phase = State.Phase.INIT,
            context = State.Context.GLOBAL,
            mode = State.Mode.PENDING,
        )
        val DASHBOARD_RESULT_ERROR_DISABLED = State(
            module = State.Module.DASHBOARD,
            phase = State.Phase.RESULT,
            context = State.Context.ERROR,
            mode = State.Mode.DISABLED,
        )
        val DASHBOARD_RESULT_OUTPUT_COMPLETED = State(
            module = State.Module.DASHBOARD,
            phase = State.Phase.RESULT,
            context = State.Context.OUTPUT,
            mode = State.Mode.COMPLETED,
        )
    }
}
