package tech.zhifu.app.myhub.ui.state.layout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

fun <VH> VH.createLayoutStateFlow(): StateFlow<Layout>
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : LayoutState {
    return userPreferencesStateFlow
        .map { prefs: UserPreferences ->
            Layout(
                layoutAsList = prefs.layoutAsList,
                sortAsDate = prefs.sortAsDate,
                sortAsName = prefs.sortAsName,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Layout()
        )
}

fun <VH> VH.updateLayoutAsList(layoutAsList: Boolean)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : LayoutState {
    val userId = userPreferencesStateFlow.value.userId
    viewModelScope.launch {
        userRepository
            .updateUserPreferencesLayout(
                userId = userId,
                layoutAsList = layoutAsList
            )
    }
}

fun <VH> VH.updateSortAsDate(sortAsDate: Boolean)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : LayoutState {
    val userId = userPreferencesStateFlow.value.userId
    viewModelScope.launch {
        userRepository
            .updateUserPreferencesSort(
                userId = userId,
                sortAsDate = sortAsDate,
                sortAsName = sortAsDate.not()
            )
    }
}
