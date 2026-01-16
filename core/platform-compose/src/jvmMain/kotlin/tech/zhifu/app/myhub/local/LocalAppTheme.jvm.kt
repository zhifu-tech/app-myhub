package tech.zhifu.app.myhub.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

actual object LocalAppTheme {
    private val LocalAppTheme = staticCompositionLocalOf { true }

    actual val current: Boolean
        @Composable get() = LocalAppTheme.current

    @Composable
    actual infix fun provides(value: Boolean?): ProvidedValue<*> {
        return LocalAppTheme.provides(value ?: true)
    }
}

