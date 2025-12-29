package tech.zhifu.app.myhub.settings.settings

import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.settings.data.impl.SettingImpl
import tech.zhifu.app.myhub.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.settings.data.store.StringSettingSerializer
import tech.zhifu.app.myhub.settings.domain.Setting
import tech.zhifu.app.myhub.settings.domain.SettingScope
import tech.zhifu.app.myhub.settings.domain.SettingsRepository

private const val LANGUAGE_SETTING_KEY = "language.code"

val SettingsRepository.languageSetting: Setting<String>?
    get() = get<String>(LANGUAGE_SETTING_KEY)

/**
 * 语言设置
 *
 * 作用域：USER（用户级设置）
 * 数据源优先级：用户偏好 > 本地存储 > 默认值（英语）
 */
class LanguageSetting(
    localStore: LocalSettingStore,
    userRepository: UserRepository?
) : Setting<String> {
    override val key = LANGUAGE_SETTING_KEY
    override val scope = SettingScope.USER

    override val defaultValue = "en"

    private val setting = SettingImpl(
        key = key,
        scope = scope,
        defaultValue = defaultValue,
        localStore = localStore,
        userRepository = userRepository,
        serializer = StringSettingSerializer(),
        userPreferenceExtractor = { prefs ->
            prefs.language.takeIf { it.isNotBlank() }
        },
        userPreferenceUpdater = { prefs, value ->
            prefs.copy(language = value)
        }
    )

    override fun observe() = setting.observe()
    override suspend fun get() = setting.get()
    override suspend fun set(value: String) = setting.set(value)
    override suspend fun reset() = setting.reset()
}

