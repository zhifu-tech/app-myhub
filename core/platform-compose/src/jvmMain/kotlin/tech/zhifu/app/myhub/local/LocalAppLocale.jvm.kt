package tech.zhifu.app.myhub.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import tech.zhifu.app.myhub.language.normalizeLanguageTag
import java.util.*

actual object LocalAppLocale {
    private var default: Locale? = null
    private val LocalAppLocale = staticCompositionLocalOf {
        normalizeLanguageTag(Locale.getDefault().toLanguageTag())
    }

    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (default == null) {
            default = Locale.getDefault()
        }
        val source = value ?: default!!.toLanguageTag()
        val normalizedTag = normalizeLanguageTag(source)
        val new = Locale.forLanguageTag(normalizedTag)
        Locale.setDefault(new)
        return LocalAppLocale.provides(normalizedTag)
    }
}
