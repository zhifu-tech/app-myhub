package tech.zhifu.app.myhub.ui.state.layout

import kotlinx.coroutines.flow.StateFlow

interface LayoutState {
    val layoutStateFlow: StateFlow<Layout>
}

data class Layout(
    val layoutAsList: Boolean = true,
    val sortAsDate: Boolean = true,
    val sortAsName: Boolean = false,
)
