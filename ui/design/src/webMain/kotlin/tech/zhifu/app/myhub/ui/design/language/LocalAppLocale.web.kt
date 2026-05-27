package tech.zhifu.app.myhub.ui.design.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale

external object window {
    var __customLocale: String?
}

actual object LocalAppLocale {
    private val LocalAppLocale = staticCompositionLocalOf { Locale.current.toString() }
    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val normalizedTag = value?.replace('_', '-') ?: Locale.current.toString()
        window.__customLocale = normalizedTag
        return LocalAppLocale.provides(normalizedTag)
    }
}
