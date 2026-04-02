package tech.zhifu.app.myhub.ui.state.user.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.ui.state.user.UserState

fun <VH> VH.createUserPreferencesStatFlow(): StateFlow<UserPreferences?>
    where VH : ViewModel,
          VH : UserState,
          VH : UserPreferencesState {
    return userStateFlow
        .filterNotNull()
        .distinctUntilChangedBy(keySelector = User::id)
        .flatMapLatest { user ->
            userRepository.userPreferencesFlow(userId = user.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
