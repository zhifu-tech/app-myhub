package tech.zhifu.app.myhub.ui.state.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun <VM> VM.collectLayoutAsList(): State<Boolean>
    where VM : ViewModel,
          VM : LayoutState {
    return layoutStateFlow
        .map { it.layoutAsList }
        .distinctUntilChanged()
        .collectAsState(initial = true)
}

@Composable
fun <VM> VM.collectSortAsDate(): State<Boolean>
    where VM : ViewModel,
          VM : LayoutState {
    return layoutStateFlow
        .map { it.sortAsDate }
        .distinctUntilChanged()
        .collectAsState(initial = true)
}
