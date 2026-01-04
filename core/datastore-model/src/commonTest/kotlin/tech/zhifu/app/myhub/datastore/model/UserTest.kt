package tech.zhifu.app.myhub.datastore.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * User 数据模型单元测试
 */
class UserTest {

    @Test
    fun `test User creation with required fields`() {
        // Given
        val now = Clock.System.now()

        // When
        val user = User(
            id = "user-1",
            username = "testuser",
            createdAt = now
        )

        // Then
        assertEquals("user-1", user.id)
        assertEquals("testuser", user.username)
        assertNull(user.email)
        assertNull(user.displayName)
        assertNull(user.avatarUrl)
        assertNull(user.preferences)
    }

    @Test
    fun `test User creation with all fields`() {
        // Given
        val now = Clock.System.now()
        val preferences = UserPreferences(
            theme = "dark",
            language = "zh-CN",
            defaultCardType = CardType.QUOTE,
            autoSync = true,
            syncInterval = 7200000L
        )

        // When
        val user = User(
            id = "user-1",
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.jpg",
            createdAt = now,
            preferences = preferences
        )

        // Then
        assertEquals("user-1", user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.displayName)
        assertEquals("https://example.com/avatar.jpg", user.avatarUrl)
        assertNotNull(user.preferences)
        assertEquals("dark", user.preferences.theme)
        assertEquals("zh-CN", user.preferences.language)
        assertEquals(CardType.QUOTE, user.preferences.defaultCardType)
    }

    @Test
    fun `test UserPreferences default values`() {
        // When
        val preferences = UserPreferences()

        // Then
        assertEquals("dark", preferences.theme)
        assertEquals("en", preferences.language)
        assertNull(preferences.defaultCardType)
        assertTrue(preferences.autoSync)
        assertEquals(3600000L, preferences.syncInterval)
    }

    @Test
    fun `test UserPreferences with all fields`() {
        // When
        val preferences = UserPreferences(
            theme = "light",
            language = "ja",
            defaultCardType = CardType.CODE,
            autoSync = false,
            syncInterval = 1800000L
        )

        // Then
        assertEquals("light", preferences.theme)
        assertEquals("ja", preferences.language)
        assertEquals(CardType.CODE, preferences.defaultCardType)
        assertFalse(preferences.autoSync)
        assertEquals(1800000L, preferences.syncInterval)
    }
}

