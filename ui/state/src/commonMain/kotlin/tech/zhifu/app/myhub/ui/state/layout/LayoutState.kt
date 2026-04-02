package tech.zhifu.app.myhub.ui.state.layout

import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

interface LayoutState : UserPreferencesState {
    val layoutStateFlow: StateFlow<Layout>
}

data class Layout(
    val layoutAsList: Boolean = true,
    val sortAsDate: Boolean = true,
    val sortAsName: Boolean = false,
)
