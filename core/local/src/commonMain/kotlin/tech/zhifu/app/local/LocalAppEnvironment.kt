package tech.zhifu.app.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key

@Composable
fun LocalAppEnvironment(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppTheme provides customAppThemeIsDark,
        LocalAppLocale provides customAppLocale
    ) {
        // 使用 key 同时监听主题和语言的变化，强制刷新资源
        key(customAppThemeIsDark, customAppLocale) {
            content()
        }
    }
}
