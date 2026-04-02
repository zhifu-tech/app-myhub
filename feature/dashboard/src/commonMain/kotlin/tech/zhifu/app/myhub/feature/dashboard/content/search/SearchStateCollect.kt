package tech.zhifu.app.myhub.feature.dashboard.content.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun SearchState.collectSearchingState(): State<Boolean> {
    return searchStateFlow
        .map { it.isNotEmpty() }
        .distinctUntilChanged()
        .collectAsState(initial = false)
}
