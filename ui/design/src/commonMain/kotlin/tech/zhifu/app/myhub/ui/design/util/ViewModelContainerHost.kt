package tech.zhifu.app.myhub.ui.design.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.orbitmvi.orbit.ContainerHost

abstract class ViewModelContainerHost<STATE : Any, SIDE_EFFECT : Any>
    : ContainerHost<STATE, SIDE_EFFECT>, ViewModel() {

    val uiState: STATE
        get() = container.refCountStateFlow.value

    val sharedSideEffect: Flow<SIDE_EFFECT> by lazy {
        container.refCountSideEffectFlow.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            replay = 0
        )
    }

    inline fun <reified T : STATE> reduce(
        crossinline reducer: T.() -> T
    ) = intent {
        (state as? T)?.let {
            reduce { it.reducer() }
        }
    }

    @Composable
    fun <R> collectFieldAsState(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        selector: (STATE) -> R,
    ): State<R> = container.refCountStateFlow
        .map(selector)
        .distinctUntilChanged()
        .collectAsStateWithLifecycle(
            initialValue = selector(uiState),
            minActiveState = lifecycleState
        )

    @Composable
    fun collectSharedSideEffect(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        sideEffect: (suspend (sideEffect: SIDE_EFFECT) -> Unit)
    ) {
        val sideEffectFlow = sharedSideEffect
        val lifecycleOwner = LocalLifecycleOwner.current
        val callback by rememberUpdatedState(newValue = sideEffect)

        LaunchedEffect(sideEffectFlow, lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
                sideEffectFlow.collect { callback(it) }
            }
        }
    }

    fun postSideEffect(effect: SIDE_EFFECT) = intent {
        postSideEffect(effect)
    }
}




