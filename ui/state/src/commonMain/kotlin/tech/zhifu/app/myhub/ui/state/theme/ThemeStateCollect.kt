package tech.zhifu.app.myhub.ui.state.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.map

@Composable
fun ThemeState.collectDarkThemeState(): State<Boolean> {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return themeStateFlow
        .map { state ->
            when (state) {
                Theme.Light -> false
                Theme.Dark -> true
                else -> isSystemInDarkTheme
            }
        }
        .collectAsState(initial = isSystemInDarkTheme)
}
