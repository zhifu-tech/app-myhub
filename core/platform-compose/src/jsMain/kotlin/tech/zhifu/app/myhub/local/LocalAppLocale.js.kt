package tech.zhifu.app.myhub.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale

external object window {
    var __customLocale: String?
}

actual object LocalAppLocale {
    private val LocalAppLocale = staticCompositionLocalOf { Locale.current }
    actual val current: String
        @Composable get() = LocalAppLocale.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        value?.let {
            println("MYHUB: LocalAppLocale.provides: $value")
        }
        window.__customLocale = value?.replace('_', '-')
        println("MYHUB: window.__customLocale: $window.__customLocale")
        println("MYHUB: Locale.current: ${Locale.current}")
        println("MYHUB: LocalAppLocale.current: ${LocalAppLocale.current}")
        return LocalAppLocale.provides(Locale.current)
    }
}

