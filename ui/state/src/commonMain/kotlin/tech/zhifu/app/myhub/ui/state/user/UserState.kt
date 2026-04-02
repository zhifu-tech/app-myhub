package tech.zhifu.app.myhub.ui.state.user

import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

interface UserState {
    val userRepository: UserRepository
    val userStateFlow: StateFlow<User?>
}
