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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.orbitmvi.orbit.ContainerHost

@Composable
fun <STATE, SIDE_EFFECT, VM> VM.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<STATE>
    where STATE : Any,
          SIDE_EFFECT : Any,
          VM : ContainerHost<STATE, SIDE_EFFECT> {
    return container.refCountStateFlow
        .collectAsStateWithLifecycle(
            initialValue = container.refCountStateFlow.value,
            minActiveState = lifecycleState
        )
}

@Composable
fun <STATE, SIDE_EFFECT, VM, R> VM.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    selector: (STATE) -> R,
): State<R>
    where STATE : Any,
          SIDE_EFFECT : Any,
          VM : ViewModel,
          VM : ContainerHost<STATE, SIDE_EFFECT> {
    return container.refCountStateFlow
        .map(selector)
        .collectAsStateWithLifecycle(
            initialValue = selector(container.refCountStateFlow.value),
            minActiveState = lifecycleState
        )
}

@Composable
fun <STATE, SIDE_EFFECT, VM, R> VM.collectSharedSideEffect(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    predicate: (SIDE_EFFECT) -> Boolean = { true },
    onSideEffect: FlowCollector<SIDE_EFFECT>
) where
    STATE : Any,
    SIDE_EFFECT : Any,
    VM : ViewModel,
    VM : ContainerHost<STATE, SIDE_EFFECT>,
    R : SIDE_EFFECT {

    val sharedSideEffect: SharedFlow<SIDE_EFFECT> = remember(this) {
        container.refCountSideEffectFlow.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            replay = 0
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val collector by rememberUpdatedState(onSideEffect)

    LaunchedEffect(sharedSideEffect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            sharedSideEffect
                .filter(predicate)
                .collect(collector)
        }
    }
}
