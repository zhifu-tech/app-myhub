package tech.zhifu.app.myhub.ui.state.layout

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

fun LayoutState.initLayoutStateFlow(): StateFlow<Layout> =
    userPreferencesStateFlow
        .map { prefs: UserPreferences? ->
            Layout(
                layoutAsList = prefs?.layoutAsList ?: true,
                sortAsDate = prefs?.sortAsDate ?: true,
                sortAsName = prefs?.sortAsName ?: false,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope(),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Layout()
        )

fun LayoutState.updateLayoutAsList(
    layoutAsList: Boolean,
) = viewModelScope().launch {
    userRepository
        .updateUserPreferencesLayout(
            userId = userStateFlow.value?.id.orEmpty(),
            layoutAsList = layoutAsList
        )
}

fun LayoutState.updateSortAsDate(
    sortAsDate: Boolean
) = viewModelScope().launch {
    userRepository
        .updateUserPreferencesSort(
            userId = userStateFlow.value?.id.orEmpty(),
            sortAsDate = sortAsDate,
            sortAsName = sortAsDate.not()
        )
}
