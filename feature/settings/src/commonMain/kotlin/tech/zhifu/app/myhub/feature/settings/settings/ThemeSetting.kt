package tech.zhifu.app.myhub.feature.settings.settings

import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.feature.settings.data.impl.SettingImpl
import tech.zhifu.app.myhub.feature.settings.data.store.BooleanSettingSerializer
import tech.zhifu.app.myhub.feature.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.feature.settings.domain.Setting
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository

private const val THEME_SETTING_KEY = "theme.is_dark"

val SettingsRepository.themeSetting: Setting<Boolean>
    get() = get(THEME_SETTING_KEY)
        ?: throw IllegalStateException("Theme setting not found")

/**
 * 主题设置
 *
 * 作用域：USER（用户级设置）
 * 数据源优先级：用户偏好 > 本地存储 > 默认值（深色模式）
 */
internal class ThemeSetting(
    localStore: LocalSettingStore,
    userRepository: UserRepository?
) : Setting<Boolean> {
    override val key = THEME_SETTING_KEY
    override val scope = SettingScope.USER
    override val defaultValue = true

    private val setting = SettingImpl(
        key = key,
        scope = scope,
        defaultValue = defaultValue,
        localStore = localStore,
        userRepository = userRepository,
        serializer = BooleanSettingSerializer(),
        userPreferenceExtractor = { prefs ->
            when (prefs.theme.lowercase()) {
                "dark" -> true
                "light" -> false
                else -> null // 返回 null 会继续查找下一个数据源
            }
        },
        userPreferenceUpdater = { prefs, value ->
            val targetTheme = if (value) "dark" else "light"
            prefs.takeIf { it.theme != targetTheme }?.apply {
                userRepository?.updateUserPreferencesLanguage(prefs.userId, targetTheme)
            }
        }
    )

    override fun observe() = setting.observe()
    override suspend fun get() = setting.get()
    override suspend fun set(value: Boolean) = setting.set(value)
    override suspend fun reset() = setting.reset()
}

