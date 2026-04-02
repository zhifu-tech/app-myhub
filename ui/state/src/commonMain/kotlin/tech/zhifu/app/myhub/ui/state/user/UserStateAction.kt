package tech.zhifu.app.myhub.ui.state.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import tech.zhifu.app.myhub.datastore.model.domain.User

fun <VH> VH.createUserStateFlow(): StateFlow<User?>
    where VH : ViewModel,
          VH : UserState {
    return userRepository
        .userFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
