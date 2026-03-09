package tech.zhifu.app.myhub.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

expect object LocalAppTheme {
    @get:Composable
    val current: Boolean

    @Composable
    infix fun provides(value: Boolean?): ProvidedValue<*>
}

