package tech.zhifu.app.myhub.ui.state.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel

@Composable
fun <VH> VH.collectThemeState(): State<Theme>
    where VH : ViewModel,
          VH : ThemeState {
    return themeStateFlow
        .collectAsState(initial = Theme.System)
}

@Composable
fun <VH> VH.collectThemeDarkState(): Boolean
    where VH : ViewModel,
          VH : ThemeState {

    val theme by themeStateFlow.collectAsState()
    val systemDark = isSystemInDarkTheme()
    return when (theme) {
        Theme.Light -> false
        Theme.Dark -> true
        Theme.System -> systemDark
    }
}
