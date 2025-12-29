package tech.zhifu.app.myhub.settings.settings

import kotlinx.coroutines.test.runTest
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.settings.test.MockLocalSettingStore
import tech.zhifu.app.myhub.settings.test.MockUserRepository
import tech.zhifu.app.myhub.settings.test.createTestUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * 语言设置测试
 */
class LanguageSettingTest {

    @Test
    fun `test default value is en`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = LanguageSetting(mockLocalStore, null)

        // When
        val result = setting.get()

        // Then
        assertEquals("en", result)
    }

    @Test
    fun `test get from user preference`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val mockUserRepository = MockUserRepository(
            user = createTestUser(preferences = UserPreferences(language = "zh"))
        )
        val setting = LanguageSetting(mockLocalStore, mockUserRepository)

        // When
        val result = setting.get()

        // Then
        assertEquals("zh", result)
    }

    @Test
    fun `test get from local store when user preference not available`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        mockLocalStore.set("language.code", "ja")
        val mockUserRepository = MockUserRepository(user = null)
        val setting = LanguageSetting(mockLocalStore, mockUserRepository)

        // When
        val result = setting.get()

        // Then
        assertEquals("ja", result)
    }

    @Test
    fun `test get returns default when user preference is blank`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val mockUserRepository = MockUserRepository(
            user = createTestUser(preferences = UserPreferences(language = ""))
        )
        val setting = LanguageSetting(mockLocalStore, mockUserRepository)

        // When
        val result = setting.get()

        // Then
        assertEquals("en", result) // Should fall back to default
    }

    @Test
    fun `test set updates user preference`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        var updatedUser: User? = null
        val mockUserRepository = object : UserRepository {
            override suspend fun getCurrentUser(): User? = createTestUser(
                preferences = UserPreferences()
            )

            override suspend fun updateUser(user: User): User {
                updatedUser = user
                return user
            }
        }
        val setting = LanguageSetting(mockLocalStore, mockUserRepository)

        // When
        setting.set("zh")

        // Then
        assertNotNull(updatedUser)
        assertEquals("zh", updatedUser.preferences?.language)
    }

    @Test
    fun `test set saves to local store`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = LanguageSetting(mockLocalStore, null)

        // When
        setting.set("ja")

        // Then
        assertEquals("ja", mockLocalStore.get("language.code"))
        assertEquals("ja", setting.get())
    }

    @Test
    fun `test reset sets to default value`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = LanguageSetting(mockLocalStore, null)
        setting.set("zh")

        // When
        setting.reset()

        // Then
        assertEquals("en", setting.get())
    }

    @Test
    fun `test observe returns flow`() = runTest {
        // Given
        val mockLocalStore = MockLocalSettingStore()
        val setting = LanguageSetting(mockLocalStore, null)

        // When
        val flow = setting.observe()
        val value = flow.value

        // Then
        assertEquals("en", value)
    }
}

