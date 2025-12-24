package tech.zhifu.app.local

import androidx.compose.runtime.*

var customAppThemeIsDark by mutableStateOf(true)

expect object LocalAppTheme {
    @get:Composable
    val current: Boolean

    @Composable
    infix fun provides(value: Boolean?): ProvidedValue<*>
}
