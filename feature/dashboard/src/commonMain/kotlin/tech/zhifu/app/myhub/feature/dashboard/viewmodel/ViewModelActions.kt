package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun DashboardViewModel.navigateToSettings() = intent {
    postSideEffect(DashboardSideEffect.NavigateToSettings)
}

fun DashboardViewModel.navigateToAiCapture() = intent {
    postSideEffect(DashboardSideEffect.NavigateToAiCapture)
}
