package tech.zhifu.app.myhub.util

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

fun Modifier.tapToClearFocus(): Modifier = composed {
    // Used to close keyboard.
    val focusManager = LocalFocusManager.current
    pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }
}

@Composable
@OptIn(FlowPreview::class)
fun rememberKeyboardOpenState(): State<Boolean> {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val keyboardOpenState = remember { mutableStateOf(false) }

    LaunchedEffect(density, imeInsets) {
        val openPx = with(density) { 120.dp.roundToPx() }
        val closePx = with(density) { 48.dp.roundToPx() }
        var current = keyboardOpenState.value

        snapshotFlow { imeInsets.getBottom(density) }
            .debounce(60L)
            .collect { bottomPx ->
                val next = when {
                    bottomPx >= openPx -> true
                    bottomPx <= closePx -> false
                    else -> current
                }
                if (next != current) {
                    current = next
                    keyboardOpenState.value = next
                }
            }
    }

    return keyboardOpenState
}
