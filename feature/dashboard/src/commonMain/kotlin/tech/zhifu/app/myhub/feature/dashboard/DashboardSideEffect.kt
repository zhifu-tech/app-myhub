package tech.zhifu.app.myhub.feature.dashboard

sealed class DashboardSideEffect {
    object NavigateToAuth : DashboardSideEffect()
    data class NavigateToCardDetail(val cardId: String) : DashboardSideEffect()
    data class NavigateToCardEdit(val cardId: String) : DashboardSideEffect()
    object NavigateToCapture : DashboardSideEffect()
}

fun DashboardViewModel.navigateToAuth() = intent {
    postSideEffect(DashboardSideEffect.NavigateToAuth)
}

fun DashboardViewModel.navigateToCardDetail(cardId: String) = intent {
    postSideEffect(DashboardSideEffect.NavigateToCardDetail(cardId))
}

fun DashboardViewModel.navigateToCapture() = intent {
    postSideEffect(DashboardSideEffect.NavigateToCapture)
}

fun DashboardViewModel.navigateToCardEdit(cardId: String) = intent {
    postSideEffect(DashboardSideEffect.NavigateToCardEdit(cardId))
}
