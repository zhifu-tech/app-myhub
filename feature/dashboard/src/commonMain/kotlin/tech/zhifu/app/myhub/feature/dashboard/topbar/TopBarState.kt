package tech.zhifu.app.myhub.feature.dashboard.topbar

import androidx.compose.runtime.Immutable

@Immutable
data class TopBarState(
    val isRefreshing: Boolean = false,
    val reviewCardsCount: Int = 0,
    val showReviewEntrance: Boolean = false,
)
