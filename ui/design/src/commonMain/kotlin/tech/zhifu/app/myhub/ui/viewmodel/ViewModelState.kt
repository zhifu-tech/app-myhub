package tech.zhifu.app.myhub.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.ContainerHost

val <STATE : Any, SIDE_EFFECT : Any, VM : ContainerHost<STATE, SIDE_EFFECT>>
    VM.uiState: StateFlow<STATE>
    get() = container.stateFlow

@Composable
fun <STATE, R> StateFlow<STATE>.collectAsSelectedStateWithLifecycle(
    selector: (STATE) -> R,
): State<R> = this
    .map(selector)
    .distinctUntilChanged()
    .collectAsStateWithLifecycle(
        initialValue = selector(this.value),
    )
