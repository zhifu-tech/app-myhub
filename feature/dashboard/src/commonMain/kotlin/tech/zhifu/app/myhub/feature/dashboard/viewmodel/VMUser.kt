package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

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
