package tech.zhifu.app.myhub.ui.state.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun StateFlow<Layout>.collectAsLayoutAsListStateWithLifecycle(
): State<Boolean> =
    this.map { it.layoutAsList }
        .distinctUntilChanged()
        .collectAsStateWithLifecycle(
            initialValue = this.value.layoutAsList,
        )

@Composable
fun StateFlow<Layout>.collectAsSortAsDateStateWithLifecycle(
): State<Boolean> =
    this.map { it.sortAsDate }
        .distinctUntilChanged()
        .collectAsStateWithLifecycle(
            initialValue = this.value.sortAsDate,
        )
