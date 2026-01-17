package tech.zhifu.app.myhub.feature.settings.data.resolver

import kotlinx.coroutines.test.runTest
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.feature.settings.data.store.BooleanSettingSerializer
import tech.zhifu.app.myhub.feature.settings.data.store.StringSettingSerializer
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope
import tech.zhifu.app.myhub.feature.settings.test.MockLocalSettingStore
import tech.zhifu.app.myhub.feature.settings.test.MockUserRepository
import tech.zhifu.app.myhub.feature.settings.test.createTestUser
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 设置值解析器测试
 */
class SettingValueResolverTest {

    @Test
    fun `test USER scope - resolve from user preference`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val mockUserRepository = MockUserRepository(
            user = createTestUser(
                preferences = UserPreferences(theme = "dark", language = "zh")
            )
        )
        val resolver = SettingValueResolver(
            key = "theme.is_dark",
            scope = SettingScope.USER,
            defaultValue = false,
            localStore = mockLocalStore,
            userRepository = mockUserRepository,
            serializer = BooleanSettingSerializer(),
            userPreferenceExtractor = { prefs ->
                prefs.theme == "dark"
            }
        )

        // When
        val result = resolver.resolve()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `test USER scope - resolve from local store when user preference not available`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        mockLocalStore.set("theme.is_dark", "true")
        val mockUserRepository = MockUserRepository(user = null)
        val resolver = SettingValueResolver(
            key = "theme.is_dark",
            scope = SettingScope.USER,
            defaultValue = false,
            localStore = mockLocalStore,
            userRepository = mockUserRepository,
            serializer = BooleanSettingSerializer(),
            userPreferenceExtractor = { null }
        )

        // When
        val result = resolver.resolve()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `test USER scope - resolve default value when no source available`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val mockUserRepository = MockUserRepository(user = null)
        val resolver = SettingValueResolver(
            key = "theme.is_dark",
            scope = SettingScope.USER,
            defaultValue = false,
            localStore = mockLocalStore,
            userRepository = mockUserRepository,
            serializer = BooleanSettingSerializer(),
            userPreferenceExtractor = { null }
        )

        // When
        val result = resolver.resolve()

        // Then
        assertEquals(false, result)
    }

    @Test
    fun `test APP scope - resolve from local store`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        mockLocalStore.set("app.setting", "test_value")
        val resolver = SettingValueResolver(
            key = "app.setting",
            scope = SettingScope.APP,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = StringSettingSerializer()
        )

        // When
        val result = resolver.resolve()

        // Then
        assertEquals("test_value", result)
    }

    @Test
    fun `test APP scope - resolve default value when local store empty`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val resolver = SettingValueResolver(
            key = "app.setting",
            scope = SettingScope.APP,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = StringSettingSerializer()
        )

        // When
        val result = resolver.resolve()

        // Then
        assertEquals("default", result)
    }

    @Test
    fun `test SESSION scope - always return default value`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        mockLocalStore.set("session.setting", "stored_value")
        val resolver = SettingValueResolver(
            key = "session.setting",
            scope = SettingScope.SESSION,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = StringSettingSerializer()
        )

        // When
        val result = resolver.resolve()

        // Then
        assertEquals("default", result)
    }

    @Test
    fun `test saveToLocal - USER scope`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val resolver = SettingValueResolver(
            key = "theme.is_dark",
            scope = SettingScope.USER,
            defaultValue = false,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        resolver.saveToLocal(true)

        // Then
        assertEquals("true", mockLocalStore.get("theme.is_dark"))
    }

    @Test
    fun `test saveToLocal - APP scope`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val resolver = SettingValueResolver(
            key = "app.setting",
            scope = SettingScope.APP,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = StringSettingSerializer()
        )

        // When
        resolver.saveToLocal("test_value")

        // Then
        assertEquals("test_value", mockLocalStore.get("app.setting"))
    }

    @Test
    fun `test saveToLocal - SESSION scope does not persist`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val resolver = SettingValueResolver(
            key = "session.setting",
            scope = SettingScope.SESSION,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = StringSettingSerializer()
        )

        // When
        resolver.saveToLocal("test_value")

        // Then
        assertEquals(null, mockLocalStore.get("session.setting"))
    }
}


