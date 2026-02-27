package tech.zhifu.app.myhub.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import tech.zhifu.app.myhub.language.normalizeLanguageTag

external object window {
    var __customLocale: String?
}

actual object LocalAppLocale {
    private val LocalAppLocale = staticCompositionLocalOf {
        normalizeLanguageTag(Locale.current.toString())
    }
    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val normalized = value?.let(::normalizeLanguageTag)
        window.__customLocale = normalized
        return LocalAppLocale.provides(normalized ?: normalizeLanguageTag(Locale.current.toString()))
    }
}
