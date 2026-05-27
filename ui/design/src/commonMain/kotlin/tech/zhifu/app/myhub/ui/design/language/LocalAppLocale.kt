package tech.zhifu.app.myhub.ui.design.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

// https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html#locale
expect object LocalAppLocale {
    @get:Composable
    val current: String

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

