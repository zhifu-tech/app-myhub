package tech.zhifu.app.myhub.ui.state.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun StateFlow<Theme>.collectAsDarkThemeStateWithLifecycle(): State<Boolean> {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return this
        .map {
            it.toDarkTheme(systemInDarkTheme = isSystemInDarkTheme)
        }
        .distinctUntilChanged()
        .collectAsStateWithLifecycle(
            initialValue = this.value.toDarkTheme(isSystemInDarkTheme),
        )
}

private fun Theme.toDarkTheme(
    systemInDarkTheme: Boolean
): Boolean = when (this) {
    Theme.Light -> false
    Theme.Dark -> true
    Theme.System -> systemInDarkTheme
}
