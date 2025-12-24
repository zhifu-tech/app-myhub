package tech.zhifu.app.local

import androidx.compose.runtime.*

actual object LocalAppTheme {
    private val LocalAppTheme = staticCompositionLocalOf { true }

    actual val current: Boolean
        @Composable get() = LocalAppTheme.current

    @Composable
    actual infix fun provides(value: Boolean?): ProvidedValue<*> {
        return LocalAppTheme.provides(value ?: true)
    }
}
