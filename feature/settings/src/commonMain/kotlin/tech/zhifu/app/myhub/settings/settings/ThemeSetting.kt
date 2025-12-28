package tech.zhifu.app.myhub.settings.settings

import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.settings.data.impl.SettingImpl
import tech.zhifu.app.myhub.settings.data.store.BooleanSettingSerializer
import tech.zhifu.app.myhub.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.settings.domain.Setting
import tech.zhifu.app.myhub.settings.domain.SettingScope

/**
 * 主题设置
 *
 * 作用域：USER（用户级设置）
 * 数据源优先级：用户偏好 > 本地存储 > 默认值（深色模式）
 */
class ThemeSetting(
    localStore: LocalSettingStore,
    userRepository: UserRepository?
) : Setting<Boolean> {
    override val key = "theme.is_dark"
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
            prefs.copy(theme = if (value) "dark" else "light")
        }
    )

    override fun observe() = setting.observe()
    override suspend fun get() = setting.get()
    override suspend fun set(value: Boolean) = setting.set(value)
    override suspend fun reset() = setting.reset()
}

