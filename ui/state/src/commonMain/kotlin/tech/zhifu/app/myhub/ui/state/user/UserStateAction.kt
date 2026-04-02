package tech.zhifu.app.myhub.ui.state.user

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import tech.zhifu.app.myhub.datastore.model.domain.User

fun UserState.initUserStateFlow(): StateFlow<User?> =
    userRepository
        .userFlow()
        .stateIn(
            scope = viewModelScope(),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
