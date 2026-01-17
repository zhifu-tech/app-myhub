package tech.zhifu.app.myhub.feature.settings.data.impl

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.feature.settings.data.store.BooleanSettingSerializer
import tech.zhifu.app.myhub.feature.settings.data.store.StringSettingSerializer
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope
import tech.zhifu.app.myhub.feature.settings.test.MockLocalSettingStore
import tech.zhifu.app.myhub.feature.settings.test.MockUserRepository
import tech.zhifu.app.myhub.feature.settings.test.createTestUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 设置项实现测试
 */
class SettingImplTest {

    @Test
    fun `test get returns default value when no value is set`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        val result = setting.get()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `test get returns value from local store`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        mockLocalStore.set("test.setting", "false")
        val setting = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        val result = setting.get()

        // Then
        assertEquals(false, result)
    }

    @Test
    fun `test set saves value to local store`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        setting.set(false)

        // Then
        assertEquals("false", mockLocalStore.get("test.setting"))
        assertEquals(false, setting.get())
    }

    @Test
    fun `test observe returns flow with current value`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        val flow = setting.observe()
        val initialValue = flow.first()

        // Then
        assertEquals(true, initialValue)
    }

    @Test
    fun `test observe updates when value changes`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )

        // When
        val flow = setting.observe()
        val initialValue = flow.first()
        setting.set(false)
        // Wait a bit for the flow to update
        kotlinx.coroutines.delay(100)
        val updatedValue = flow.first()

        // Then
        assertEquals(true, initialValue)
        assertEquals(false, updatedValue)
    }

    @Test
    fun `test reset sets value to default`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = SettingImpl(
            key = "test.setting",
            scope = SettingScope.APP,
            defaultValue = true,
            localStore = mockLocalStore,
            userRepository = null,
            serializer = BooleanSettingSerializer()
        )
        setting.set(false)

        // When
        setting.reset()

        // Then
        assertEquals(true, setting.get())
        assertEquals("true", mockLocalStore.get("test.setting"))
    }

    @Test
    fun `test USER scope - set updates user repository`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val mockUserRepository = MockUserRepository(
            user = createTestUser(preferences = UserPreferences())
        )
        var updatedUser: User? = null
        val setting = SettingImpl(
            key = "theme.is_dark",
            scope = SettingScope.USER,
            defaultValue = false,
            localStore = mockLocalStore,
            userRepository = object : UserRepository {
                override suspend fun getCurrentUser(): User? = createTestUser(
                    preferences = UserPreferences()
                )

                override suspend fun updateUser(user: User): User {
                    updatedUser = user
                    return user
                }
            },
            serializer = BooleanSettingSerializer(),
            userPreferenceExtractor = { prefs ->
                prefs.theme == "dark"
            },
            userPreferenceUpdater = { prefs, value ->
                prefs.copy(theme = if (value) "dark" else "light")
            }
        )

        // When
        setting.set(true)

        // Then
        kotlin.test.assertNotNull(updatedUser)
        assertEquals("dark", updatedUser.preferences?.theme)
        assertEquals("true", mockLocalStore.get("theme.is_dark"))
    }

    @Test
    fun `test USER scope - get prioritizes user preference over local store`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        mockLocalStore.set("theme.is_dark", "false")
        val mockUserRepository = MockUserRepository(
            user = createTestUser(preferences = UserPreferences(theme = "dark"))
        )
        val setting = SettingImpl(
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
        val result = setting.get()

        // Then
        assertTrue(result) // Should be true from user preference, not false from local store
    }

    @Test
    fun `test SESSION scope - set does not persist`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = SettingImpl(
            key = "session.setting",
            scope = SettingScope.SESSION,
            defaultValue = "default",
            localStore = mockLocalStore,
            userRepository = null,
            serializer = StringSettingSerializer()
        )

        // When
        setting.set("test_value")

        // Then
        // SESSION scope settings update the StateFlow but get() resolves from resolver
        // which always returns default for SESSION scope
        // So we check the StateFlow value instead
        val flowValue = setting.observe().value
        assertEquals("test_value", flowValue) // StateFlow should have the updated value
        assertEquals(null, mockLocalStore.get("session.setting")) // Should not persist
        // Note: get() will return default because resolver.resolve() for SESSION always returns default
        assertEquals("default", setting.get()) // get() resolves from resolver, which returns default for SESSION
    }
}

