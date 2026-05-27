package tech.zhifu.app.myhub.feature.dashboard.content.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun StateFlow<String>.collectAsSearchingStateWithLifecycle(
): State<Boolean> = this
    .map { it.isNotEmpty() }
    .distinctUntilChanged()
    .collectAsStateWithLifecycle(
        initialValue = this.value.isNotEmpty(),
    )
