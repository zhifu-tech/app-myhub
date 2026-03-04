package tech.zhifu.app.myhub.feature.dashboard


fun DashboardViewModel.handleException(
    e: Exception,
) {
    val isUnauthorizedError = e.message.let {
        it != null && (it.contains("401") || it.contains("Unauthorized", ignoreCase = true))
    }
    if (isUnauthorizedError) {
        intent {
            reduce {
                state.copy(
                    state = DashboardState.DASHBOARD_RESULT_AUTH_EXPIRED,
                    payload = null,
                )
            }
        }
        return
    }

}
