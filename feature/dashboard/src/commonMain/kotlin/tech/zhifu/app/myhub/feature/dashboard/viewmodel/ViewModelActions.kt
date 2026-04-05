package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.ui.model.ContentCard

fun DashboardViewModel.navigateToSettings() = intent {
    postSideEffect(DashboardSideEffect.NavigateToSettings)
}

fun DashboardViewModel.navigateToAiCapture() = intent {
    postSideEffect(DashboardSideEffect.NavigateToAiCapture)
}

fun DashboardViewModel.selectedCard(
    card: ContentCard?
) = intent {
    val state = state as? DashboardUiState.Content
        ?: return@intent
    reduce {
        state.copy(selectedCard = card)
    }
}
