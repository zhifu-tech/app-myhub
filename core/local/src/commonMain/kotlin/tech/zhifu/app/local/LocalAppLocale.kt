package tech.zhifu.app.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html#locale
var customAppLocale by mutableStateOf<String?>(null)

expect object LocalAppLocale {
    @get:Composable
    val current: String

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
