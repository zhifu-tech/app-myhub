package tech.zhifu.app.myhub.local

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale
import tech.zhifu.app.myhub.language.normalizeLanguageTag

actual object LocalAppLocale {
    private var default: Locale? = null

    actual val current: String
        @Composable get() = normalizeLanguageTag(Locale.getDefault().toLanguageTag())

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (default == null) {
            default = Locale.getDefault()
        }

        val source = value ?: default!!.toLanguageTag()
        val normalizedTag = normalizeLanguageTag(source)
        val new = Locale.forLanguageTag(normalizedTag)

        // 同步 JVM 默认区域，影响后续的日期格式化等非 UI 逻辑
        Locale.setDefault(new)

        // 1. 获取当前配置
        val currentConfiguration = LocalConfiguration.current

        // 2. 创建一个全新的配置副本，避免修改原始实例
        val newConfig = Configuration(currentConfiguration).apply {
            setLocale(new)
        }

        // 3. 核心：通过 LocalConfiguration 提供新配置
        // Compose 的 stringResource 内部依赖 LocalConfiguration，
        // 提供 newConfig 会自动触发整个 UI 树使用新语言重新渲染，
        // 无需调用废弃的 resources.updateConfiguration。
        return LocalConfiguration.provides(newConfig)
    }
}
