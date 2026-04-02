package tech.zhifu.app.myhub.feature.dashboard

sealed class DashboardSideEffect {

    object ResetSearch : DashboardSideEffect()

    object NavigateToOpenSourceLicenses : DashboardSideEffect()

    object NavigateToSupport : DashboardSideEffect()

    object NavigateToSettings : DashboardSideEffect()

    object NavigateToAiCapture : DashboardSideEffect()

    data class ShowSnack(val message: String) : DashboardSideEffect()
}
