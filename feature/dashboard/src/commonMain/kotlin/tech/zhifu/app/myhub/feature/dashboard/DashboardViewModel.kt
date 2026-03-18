package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.observeUserPreferences
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.util.ViewModelContainerHost

class DashboardViewModel(
    val logger: Logger = logger("Dashboard"),
    internal val userRepository: UserRepository,
) : ViewModelContainerHost<DashboardUiState, DashboardSideEffect>() {

    override val container = container<DashboardUiState, DashboardSideEffect>(
        initialState = DashboardUiState.Error()
    )

    init {
        viewModelScope.launch {
            observeUserPreferences()
        }
    }

    fun retry() {
        viewModelScope.launch {
            initInternal()
        }
    }

    private suspend fun initInternal() {
    }
}
