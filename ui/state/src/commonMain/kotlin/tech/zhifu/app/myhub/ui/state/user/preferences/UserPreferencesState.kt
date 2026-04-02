package tech.zhifu.app.myhub.ui.state.user.preferences

import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.ui.state.user.UserState

interface UserPreferencesState : UserState {
    val userPreferencesStateFlow: StateFlow<UserPreferences?>
}
