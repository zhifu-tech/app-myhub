package tech.zhifu.app.myhub.ui.state.user

import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.ui.state.ViewModelState

interface UserState : ViewModelState {
    val userRepository: UserRepository
    val userStateFlow: StateFlow<User?>
}
