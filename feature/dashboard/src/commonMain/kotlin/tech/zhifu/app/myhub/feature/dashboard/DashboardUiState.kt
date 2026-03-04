package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.feature.dashboard.content.card.CardSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.review.ReviewState
import tech.zhifu.app.myhub.ui.UiContext
import tech.zhifu.app.myhub.ui.UiMode
import tech.zhifu.app.myhub.ui.UiModule
import tech.zhifu.app.myhub.ui.UiPhase

data class DashboardUiState(
    val state: DashboardState,
    val payload: DashboardPayload? = null,
)

enum class DashboardState(
    val module: UiModule = UiModule.DASHBOARD,
    val phase: UiPhase,
    val context: UiContext,
    val mode: UiMode,
) {
    DASHBOARD_INIT_GLOBAL_PENDING(
        phase = UiPhase.INIT,
        context = UiContext.GLOBAL,
        mode = UiMode.PENDING
    ),
    DASHBOARD_RESULT_ERROR_DISABLED(
        phase = UiPhase.RESULT,
        context = UiContext.ERROR,
        mode = UiMode.DISABLED
    ),
    DASHBOARD_RESULT_AUTH_EXPIRED(
        phase = UiPhase.RESULT,
        context = UiContext.AUTH,
        mode = UiMode.EXPIRED
    ),
    DASHBOARD_RESULT_OUTPUT_COMPLETED(
        phase = UiPhase.RESULT,
        context = UiContext.OUTPUT,
        mode = UiMode.COMPLETED
    )
}

sealed interface DashboardPayload {
    data class ResultErrorDisabledPayload(
        val message: String = "",
        val canRetry: Boolean = true,
    ) : DashboardPayload

    data class ResultOutputCompletedPayload(
        val isRefreshing: Boolean = false,
        val reviewState: ReviewState? = null,
        val collectionSectionState: CollectionSectionState,
        val cardSectionState: CardSectionState,
    ) : DashboardPayload
}

sealed class DashboardSideEffect {
    object NavigateToAuth : DashboardSideEffect()
    data class NavigateToCardDetail(val cardId: String) : DashboardSideEffect()
    object NavigateToCapture : DashboardSideEffect()
}

val DashboardUiState.resultCompletedPayload
    get() = payload as? DashboardPayload.ResultOutputCompletedPayload

val DashboardUiState.resultErrorPayload
    get() = payload as? DashboardPayload.ResultErrorDisabledPayload
