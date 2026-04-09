package tech.zhifu.app.myhub.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.shareIn
import org.orbitmvi.orbit.ContainerHost
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

interface ViewModelSideEffect<SIDE_EFFECT : Any> {
    val sideEffect: SharedFlow<SIDE_EFFECT>
}

fun <STATE : Any, SIDE_EFFECT : Any, VM> VM.createSideEffectFlow(
): SharedFlow<SIDE_EFFECT>
    where VM : ViewModel,
          VM : ContainerHost<STATE, SIDE_EFFECT>,
          VM : ViewModelSideEffect<SIDE_EFFECT> {
    return container.sideEffectFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        replay = 0
    )
}

@Composable
fun <SIDE_EFFECT> SharedFlow<SIDE_EFFECT>.CollectPredicatedSharedSideEffect(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    predicate: (SIDE_EFFECT) -> Boolean = { true },
    onSideEffect: FlowCollector<SIDE_EFFECT>
) {
    logger.debug { "CollectPredicatedSharedSideEffect" + this.hashCode() }
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
