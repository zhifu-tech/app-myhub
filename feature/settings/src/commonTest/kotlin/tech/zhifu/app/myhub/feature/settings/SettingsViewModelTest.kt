package tech.zhifu.app.myhub.feature.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.test
import tech.zhifu.app.myhub.feature.settings.domain.Setting
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.language.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsViewModelTest {

    @Test
    fun `bootstrap success enters completed state`() = runTest {
        val repository = FakeSettingsRepository(
            themeSetting = FakeSetting(
                key = THEME_KEY,
                defaultValue = true,
                initialValue = false,
                scope = SettingScope.USER,
            ),
            languageSetting = FakeSetting(
                key = LANGUAGE_KEY,
                defaultValue = Language.English.code,
                initialValue = Language.Japanese.code,
                scope = SettingScope.USER,
            )
        )

        val viewModel = SettingsViewModel(repository)

        viewModel.test(this) {
            runOnCreate()
            val completed = awaitState() as SettingsUiState.ResultOutputCompleted
            assertEquals(false, completed.themeSettingState.isDarkMode)
            assertEquals(Language.Japanese, completed.languageSettingState.language)
        }
    }

    @Test
    fun `bootstrap failure enters error and retry recovers`() = runTest {
        val themeSetting = FakeSetting(
            key = THEME_KEY,
            defaultValue = true,
            initialValue = true,
            scope = SettingScope.USER,
        ).apply {
            getError = IllegalStateException("theme load failed")
        }
        val languageSetting = FakeSetting(
            key = LANGUAGE_KEY,
            defaultValue = Language.English.code,
            initialValue = Language.English.code,
            scope = SettingScope.USER,
        )
        val repository = FakeSettingsRepository(themeSetting, languageSetting)

        val viewModel = SettingsViewModel(repository)

        viewModel.test(this) {
            runOnCreate()

            val errorState = awaitState() as SettingsUiState.ResultErrorDisabled
            assertTrue(errorState.message.contains("theme load failed"))

            themeSetting.getError = null
            viewModel.retry()

            val loading = awaitState()
            assertTrue(loading is SettingsUiState.InitGlobalPending)

            val completed = awaitState()
            assertTrue(completed is SettingsUiState.ResultOutputCompleted)
        }
    }

    @Test
    fun `update language submits and updates state`() = runTest {
        val repository = defaultRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.test(this) {
            runOnCreate()
            awaitState() // completed after bootstrap

            viewModel.update(Language.TraditionalChinese)

            awaitState() // isSubmittingLanguage=true
            val updated = awaitState() as SettingsUiState.ResultOutputCompleted
            assertEquals(
                Language.TraditionalChinese,
                updated.languageSettingState.language
            )
            assertEquals(1, repository.languageSetting.setCalls)
        }
    }

    @Test
    fun `theme update failure keeps completed state with inline message`() = runTest {
        val repository = defaultRepository()
        repository.themeSetting.setError = IllegalStateException("theme set failed")

        val viewModel = SettingsViewModel(repository)

        viewModel.test(this) {
            runOnCreate()
            awaitState() // completed after bootstrap

            viewModel.updateTheme(true)
            awaitState() // isSubmittingTheme=true

            val failed = awaitState() as SettingsUiState.ResultOutputCompleted
            assertTrue(failed.inlineMessage.contains("theme set failed"))
            assertEquals(1, repository.themeSetting.setCalls)

            viewModel.clearError()
            val cleared = awaitState() as SettingsUiState.ResultOutputCompleted
            assertEquals("", cleared.inlineMessage)
        }
    }

    private fun defaultRepository(): FakeSettingsRepository {
        return FakeSettingsRepository(
            themeSetting = FakeSetting(
                key = THEME_KEY,
                defaultValue = true,
                initialValue = false,
                scope = SettingScope.USER,
            ),
            languageSetting = FakeSetting(
                key = LANGUAGE_KEY,
                defaultValue = Language.English.code,
                initialValue = Language.English.code,
                scope = SettingScope.USER,
            )
        )
    }

    private class FakeSettingsRepository(
        val themeSetting: FakeSetting<Boolean>,
        val languageSetting: FakeSetting<String>,
    ) : SettingsRepository {
        private val settings = mutableMapOf<String, Setting<*>>()

        init {
            register(themeSetting)
            register(languageSetting)
        }

        override fun <T> register(setting: Setting<T>) {
            settings[setting.key] = setting
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> get(key: String): Setting<T>? {
            return settings[key] as? Setting<T>
        }

        override fun getAll(): List<Setting<*>> = settings.values.toList()
    }

    private class FakeSetting<T>(
        override val key: String,
        override val defaultValue: T,
        private val initialValue: T,
        override val scope: SettingScope,
    ) : Setting<T> {
        private val state = MutableStateFlow(initialValue)

        var getError: Throwable? = null
        var setError: Throwable? = null
        var setCalls: Int = 0

        override fun observe(): Flow<T> = state.asStateFlow()

        override suspend fun get(): T {
            getError?.let { throw it }
            return state.value
        }

        override suspend fun set(value: T) {
            setCalls += 1
            setError?.let { throw it }
            state.value = value
        }

        override suspend fun reset() {
            state.value = defaultValue
        }
    }

    private companion object {
        const val THEME_KEY = "theme.is_dark"
        const val LANGUAGE_KEY = "language.code"
    }
}
