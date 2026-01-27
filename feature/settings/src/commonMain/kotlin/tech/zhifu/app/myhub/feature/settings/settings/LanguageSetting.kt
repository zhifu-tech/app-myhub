package tech.zhifu.app.myhub.feature.settings.settings

import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.settings.data.impl.SettingImpl
import tech.zhifu.app.myhub.feature.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.feature.settings.data.store.StringSettingSerializer
import tech.zhifu.app.myhub.feature.settings.domain.Setting
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository

private const val LANGUAGE_SETTING_KEY = "language.code"

val SettingsRepository.languageSetting: Setting<String>
    get() = get(LANGUAGE_SETTING_KEY)
        ?: throw IllegalStateException("Language setting not found")


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
            prefs.takeIf { it.language != value }?.apply {
                userRepository?.updateUserPreferencesLanguage(prefs.userId, value)
            }
        }
    )

    override fun observe() = setting.observe()
    override suspend fun get() = setting.get()
    override suspend fun set(value: String) = setting.set(value)
    override suspend fun reset() = setting.reset()
}

