package tech.zhifu.app.myhub.feature.settings.content.language

import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.settings.data.impl.SettingImpl
import tech.zhifu.app.myhub.feature.settings.data.store.StringSettingSerializer
import tech.zhifu.app.myhub.feature.settings.domain.Setting
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.language.AppLocale
import tech.zhifu.app.myhub.language.normalizeLanguageTag
import tech.zhifu.app.myhub.settings.LocalSettingStore

private const val LANGUAGE_SETTING_KEY = "language.code"

val SettingsRepository.languageSetting: Setting<String>
    get() = get(LANGUAGE_SETTING_KEY)
        ?: throw IllegalStateException("Language setting not found")


/**
 * 语言设置
 *
 * 作用域：USER（用户级设置）
 * 数据源优先级：用户偏好 > 本地存储 > 默认值
 */
class LanguageSetting(
    localStore: LocalSettingStore,
    userRepository: UserRepository?
) : Setting<String> {
    override val key = LANGUAGE_SETTING_KEY
    override val scope = SettingScope.USER
    override val defaultValue = AppLocale.DEFAULT

    private val setting = SettingImpl(
        key = key,
        scope = scope,
        defaultValue = defaultValue,
        localStore = localStore,
        userRepository = userRepository,
        serializer = StringSettingSerializer(),
        userPreferenceExtractor = { prefs ->
            prefs.language
                .takeIf { it.isNotBlank() }
                ?.let(::normalizeLanguageTag)
        },
        userPreferenceUpdater = { prefs, value ->
            val normalized = normalizeLanguageTag(value)
            prefs.takeIf {
                normalizeLanguageTag(it.language) != normalized
            }?.apply {
                userRepository?.updateUserPreferencesLanguage(
                    userId = prefs.userId,
                    language = normalized
                )
            }
        }
    )

    override fun observe() = setting.observe().map(::normalizeLanguageTag)
    override suspend fun get() = normalizeLanguageTag(setting.get())
    override suspend fun set(value: String) = setting.set(normalizeLanguageTag(value))
    override suspend fun reset() = setting.reset()
}
