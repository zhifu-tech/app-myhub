package tech.zhifu.app.myhub.feature.dashboard

sealed class DashboardSideEffect {

    object ResetSearch : DashboardSideEffect()

    object NavigateToOpenSourceLicenses : DashboardSideEffect()

    object NavigateToSupport : DashboardSideEffect()
}
