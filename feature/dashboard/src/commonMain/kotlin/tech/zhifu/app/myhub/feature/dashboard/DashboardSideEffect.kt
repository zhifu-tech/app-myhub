package tech.zhifu.app.myhub.feature.dashboard

sealed class DashboardSideEffect {
    object NavigateToAuth : DashboardSideEffect()

    data class NavigateToCardDetail(val cardId: String) : DashboardSideEffect()
    data class NavigateToCardEdit(val cardId: String) : DashboardSideEffect()
    object NavigateToCardList : DashboardSideEffect()

    data class NavigateToCollectionDetail(val collectionId: String) : DashboardSideEffect()
    object NavigateToCollectionList : DashboardSideEffect()

    object NavigateToCapture : DashboardSideEffect()

    object NavigateToReview : DashboardSideEffect()
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

fun DashboardViewModel.navigateToCollectionDetail(collectionId: String) = intent {
    postSideEffect(DashboardSideEffect.NavigateToCollectionDetail(collectionId))
}

fun DashboardViewModel.navigateToCollectionList() = intent {
    postSideEffect(DashboardSideEffect.NavigateToCollectionList)
}

fun DashboardViewModel.navigateToCardList() = intent {
    postSideEffect(DashboardSideEffect.NavigateToCardList)
}

fun DashboardViewModel.navigateToReview() = intent {
    postSideEffect(DashboardSideEffect.NavigateToReview)
}
