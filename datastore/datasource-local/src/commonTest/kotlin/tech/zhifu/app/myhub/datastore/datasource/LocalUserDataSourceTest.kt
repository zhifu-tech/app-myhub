package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * LocalUserDataSource 测试
 */
class LocalUserDataSourceTest {

    @Test
    fun `test save and get user`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalUserDataSourceImpl(database)
        val user = createTestUser("1", "testuser")

        // When
        dataSource.saveUser(user)
        val result = dataSource.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("testuser", result.username)
    }

    @Test
    fun `test save user with preferences`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalUserDataSourceImpl(database)
        val preferences = UserPreferences(
            theme = "light",
            language = "zh",
            defaultCardType = CardType.QUOTE,
            autoSync = true,
            syncInterval = 7200000L
        )
        val user = createTestUser("1", "testuser", preferences = preferences)

        // When
        dataSource.saveUser(user)
        val result = dataSource.getCurrentUser()

        // Then
        assertNotNull(result)
        assertNotNull(result.preferences)
        assertEquals("light", result.preferences!!.theme)
        assertEquals("zh", result.preferences!!.language)
        assertEquals(CardType.QUOTE, result.preferences!!.defaultCardType)
    }

    @Test
    fun `test update user`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalUserDataSourceImpl(database)
        val user = createTestUser("1", "testuser")
        dataSource.saveUser(user)

        // When
        val updatedUser = user.copy(displayName = "Updated Name", email = "updated@example.com")
        dataSource.saveUser(updatedUser)
        val result = dataSource.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals("Updated Name", result.displayName)
        assertEquals("updated@example.com", result.email)
    }

    @Test
    fun `test clear user`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalUserDataSourceImpl(database)
        val user = createTestUser("1", "testuser")
        dataSource.saveUser(user)

        // When
        dataSource.clearUser()
        val result = dataSource.getCurrentUser()

        // Then
        assertNull(result)
    }

    @Test
    fun `test observe user flow`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalUserDataSourceImpl(database)
        val user1 = createTestUser("1", "user1")

        // When
        val flow = dataSource.observeUser()
        dataSource.saveUser(user1)
        val firstResult = flow.first()

        val user2 = createTestUser("1", "user2")
        dataSource.saveUser(user2)
        val secondResult = flow.first()

        // Then
        assertNotNull(firstResult)
        assertEquals("user1", firstResult.username)
        assertNotNull(secondResult)
        assertEquals("user2", secondResult.username)
    }

    @Test
    fun `test user with avatar`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalUserDataSourceImpl(database)
        val user = createTestUser("1", "testuser", avatarUrl = "https://example.com/avatar.png")

        // When
        dataSource.saveUser(user)
        val result = dataSource.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals("https://example.com/avatar.png", result.avatarUrl)
    }

    /**
     * 创建测试用的用户
     */
    private fun createTestUser(
        id: String,
        username: String,
        email: String? = null,
        displayName: String? = null,
        avatarUrl: String? = null,
        preferences: UserPreferences? = null
    ): User {
        return User(
            id = id,
            username = username,
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = Clock.System.now(),
            preferences = preferences
        )
    }
}

