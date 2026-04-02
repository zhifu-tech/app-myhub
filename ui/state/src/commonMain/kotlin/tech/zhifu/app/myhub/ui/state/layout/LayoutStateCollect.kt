package tech.zhifu.app.myhub.ui.state.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun LayoutState.collectLayoutAsList(): State<Boolean> =
    layoutStateFlow
        .map { it.layoutAsList }
        .distinctUntilChanged()
        .collectAsState(initial = true)

@Composable
fun LayoutState.collectSortAsDate(): State<Boolean> =
    layoutStateFlow
        .map { it.sortAsDate }
        .distinctUntilChanged()
        .collectAsState(initial = true)
