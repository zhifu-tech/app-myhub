package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun DashboardViewModel.navigateToOpenSourceLicenses() = intent {
    postSideEffect(DashboardSideEffect.NavigateToOpenSourceLicenses)
}

fun DashboardViewModel.navigateToSupport() = intent {
    postSideEffect(DashboardSideEffect.NavigateToSupport)
}

fun DashboardViewModel.navigateToAiCapture() = intent {
    postSideEffect(DashboardSideEffect.NavigateToAiCapture)
}
