package tech.zhifu.app.myhub.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.orbitmvi.orbit.ContainerHost

val <STATE : Any, SIDE_EFFECT : Any, VM : ContainerHost<STATE, SIDE_EFFECT>>
    VM.uiState: StateFlow<STATE>
    get() = container.stateFlow

val <STATE : Any, SIDE_EFFECT : Any, VM> VM.sideEffect: SharedFlow<SIDE_EFFECT>
    where VM : ContainerHost<STATE, SIDE_EFFECT>,
          VM : ViewModel
    @Composable
    get() = remember(this) {
        container.sideEffectFlow.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            replay = 0
        )
    }

@Composable
fun <STATE, R> StateFlow<STATE>.collectAsSelectedStateWithLifecycle(
    selector: (STATE) -> R,
): State<R> = this
    .map(selector)
    .distinctUntilChanged()
    .collectAsStateWithLifecycle(
        initialValue = selector(this.value),
    )

@Composable
fun <SIDE_EFFECT> SharedFlow<SIDE_EFFECT>.CollectPredicatedSharedSideEffect(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    predicate: (SIDE_EFFECT) -> Boolean = { true },
    onSideEffect: FlowCollector<SIDE_EFFECT>
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val collector by rememberUpdatedState(onSideEffect)

    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            this@CollectPredicatedSharedSideEffect
                .filter(predicate)
                .collect(collector)
        }
    }
}
