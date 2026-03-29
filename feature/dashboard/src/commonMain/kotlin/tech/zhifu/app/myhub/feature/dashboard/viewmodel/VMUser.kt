package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel


@Composable
fun DashboardViewModel.collectUserPreferencesFieldLayoutAsList() =
    collectFieldAsState {
        (it as? DashboardUiState.Content)
            ?.userPreferences?.layoutAsList ?: true
    }

@Composable
fun DashboardViewModel.collectUserPreferencesFieldSortAsDate() =
    collectFieldAsState {
        (it as? DashboardUiState.Content)
            ?.userPreferences?.sortAsDate ?: true
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
