package tech.zhifu.app.myhub.feature.dashboard

sealed class DashboardSideEffect {

    object ResetSearch : DashboardSideEffect()

    object NavigateToSettings : DashboardSideEffect()

    object NavigateToAiCapture : DashboardSideEffect()

    data class ShowSnack(val message: String) : DashboardSideEffect()
}
