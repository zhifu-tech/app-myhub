package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.user.preferences
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

suspend fun DashboardViewModel.observeUserPreferences() {
    userRepository.streamUser()
        .map { it?.id.orEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            userRepository.streamUserPreferences(userId)
                .map { response ->
                    response.requireData().preferences
                        ?: UserPreferences(userId)
                }
        }
        .distinctUntilChanged()
        .collect { userPreferences ->
            reduce<DashboardUiState.Content> {
                copy(
                    userPreferences = userPreferences,
                )
            }
        }
}

@Composable
fun <R> DashboardViewModel.collectUserPreferencesFieldState(
    selector: (UserPreferences) -> R
) = collectFieldAsState { uiState ->
    (uiState as? DashboardUiState.Content)
        ?.userPreferences?.let { selector(it) }
}

fun DashboardViewModel.updateUsePreferencesLayoutAsList(
    layoutAsList: Boolean
) = viewModelScope.launch {
    val userPreferences = (uiState as? DashboardUiState.Content)
        ?.userPreferences
        ?: return@launch
    if (userPreferences.layoutAsList == layoutAsList) {
        return@launch
    }
    userRepository.insertUserPreferences(
        preferences = userPreferences.copy(
            layoutAsList = layoutAsList
        )
    )
}

fun DashboardViewModel.updateUsePreferencesSortAsDate(
    sortAsDate: Boolean
) = viewModelScope.launch {
    val userPreferences = (uiState as? DashboardUiState.Content)
        ?.userPreferences
        ?: return@launch
    if (userPreferences.sortAsDate == sortAsDate) {
        return@launch
    }
    userRepository.insertUserPreferences(
        preferences = userPreferences.copy(
            sortAsDate = sortAsDate
        )
    )
}
