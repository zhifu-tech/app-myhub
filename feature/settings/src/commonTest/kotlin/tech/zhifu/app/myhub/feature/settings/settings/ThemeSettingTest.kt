package tech.zhifu.app.myhub.feature.settings.settings

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSetting
import tech.zhifu.app.myhub.feature.settings.test.MockLocalSettingStore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 主题设置测试
 */
class ThemeSettingTest {

    @Test
    fun `test default value is true`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = ThemeSetting(mockLocalStore, null)

        // When
        val result = setting.get()

        // Then
        assertEquals(true, result)
    }

//    @Test
//    fun `test get from user preference - dark theme`() = runTest {
//        // Given
//        val mockLocalStore = MockLocalSettingStore()
//        val mockUserRepository = MockUserRepository(
//            user = createTestUser(preferences = UserPreferences(theme = "dark"))
//        )
//        val setting = ThemeSetting(mockLocalStore, mockUserRepository)
//
//        // When
//        val result = setting.get()
//
//        // Then
//        assertTrue(result)
//    }
//
//    @Test
//    fun `test get from user preference - light theme`() = runTest {
//        // Given
//        val mockLocalStore = MockLocalSettingStore()
//        val mockUserRepository = MockUserRepository(
//            user = createTestUser(preferences = UserPreferences(theme = "light"))
//        )
//        val setting = ThemeSetting(mockLocalStore, mockUserRepository)
//
//        // When
//        val result = setting.get()
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `test get from local store when user preference not available`() = runTest {
//        // Given
//        val mockLocalStore = MockLocalSettingStore()
//        mockLocalStore.set("theme.is_dark", "false")
//        val mockUserRepository = MockUserRepository(user = null)
//        val setting = ThemeSetting(mockLocalStore, mockUserRepository)
//
//        // When
//        val result = setting.get()
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `test set updates user preference`() = runTest {
//        // Given
//        val mockLocalStore = MockLocalSettingStore()
//        var updatedUser: User? = null
//        val mockUserRepository = object : UserRepository {
//            override suspend fun getCurrentUser(): User? = createTestUser(
//                preferences = UserPreferences()
//            )
//
//            override suspend fun updateUser(user: User): User {
//                updatedUser = user
//                return user
//            }
//        }
//        val setting = ThemeSetting(mockLocalStore, mockUserRepository)
//
//        // When
//        setting.set(true)
//
//        // Then
//        assertNotNull(updatedUser)
//        assertEquals("dark", updatedUser.preferences?.theme)
//    }
//
//    @Test
//    fun `test set false updates user preference to light`() = runTest {
//        // Given
//        val mockLocalStore = MockLocalSettingStore()
//        var updatedUser: User? = null
//        val mockUserRepository = object : UserRepository {
//            override suspend fun getCurrentUser(): User? = createTestUser(
//                preferences = UserPreferences(theme = "dark")
//            )
//
//            override suspend fun updateUser(user: User): User {
//                updatedUser = user
//                return user
//            }
//        }
//        val setting = ThemeSetting(mockLocalStore, mockUserRepository)
//
//        // When
//        setting.set(false)
//
//        // Then
//        assertNotNull(updatedUser)
//        assertEquals("light", updatedUser.preferences?.theme)
//    }

    @Test
    fun `test reset sets to default value`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = ThemeSetting(mockLocalStore, null)
        setting.set(false)

        // When
        setting.reset()

        // Then
        assertEquals(true, setting.get())
    }

    @Test
    fun `test observe returns flow`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = ThemeSetting(mockLocalStore, null)

        // When
        val flow = setting.observe()
        val value = flow.first()

        // Then
        assertEquals(true, value)
    }
}
