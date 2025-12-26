package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.impl.UserRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * UserRepository 测试（服务端）
 */
class UserRepositoryTest {

    @Test
    fun `test get current user when not exists`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val repository = UserRepositoryImpl(localDataSource)

        // When
        val result = repository.getCurrentUser()

        // Then
        assertNull(result)
    }

    @Test
    fun `test update user`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val repository = UserRepositoryImpl(localDataSource)
        val user = createTestUser("1", "testuser")

        // When
        val result = repository.updateUser(user)

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("testuser", result.username)

        val retrievedUser = repository.getCurrentUser()
        assertNotNull(retrievedUser)
        assertEquals("1", retrievedUser.id)
        assertEquals("testuser", retrievedUser.username)
    }

    @Test
    fun `test update user with preferences`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val repository = UserRepositoryImpl(localDataSource)
        val preferences = UserPreferences(
            theme = "light",
            language = "zh",
            defaultCardType = CardType.QUOTE,
            autoSync = true,
            syncInterval = 3600000L
        )
        val user = createTestUser("1", "testuser", preferences = preferences)

        // When
        val result = repository.updateUser(user)

        // Then
        assertNotNull(result)
        assertNotNull(result.preferences)
        assertEquals("light", result.preferences?.theme)
        assertEquals("zh", result.preferences?.language)
        assertEquals(CardType.QUOTE, result.preferences?.defaultCardType)
        assertEquals(true, result.preferences?.autoSync)

        val retrievedUser = repository.getCurrentUser()
        assertNotNull(retrievedUser)
        assertNotNull(retrievedUser.preferences)
        assertEquals("light", retrievedUser.preferences?.theme)
    }

    @Test
    fun `test update user multiple times`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val repository = UserRepositoryImpl(localDataSource)
        val user1 = createTestUser("1", "user1", displayName = "User One")

        // When
        repository.updateUser(user1)
        val firstResult = repository.getCurrentUser()

        val user2 = createTestUser("1", "user2", displayName = "User Two")
        repository.updateUser(user2)
        val secondResult = repository.getCurrentUser()

        // Then
        assertNotNull(firstResult)
        assertEquals("user1", firstResult.username)
        assertEquals("User One", firstResult.displayName)

        assertNotNull(secondResult)
        assertEquals("user2", secondResult.username)
        assertEquals("User Two", secondResult.displayName)
    }

    @Test
    fun `test initialize default user if needed`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val repository = UserRepositoryImpl(localDataSource)

        // When - 第一次调用，应该创建默认用户
        repository.initializeDefaultUserIfNeeded()
        val firstUser = repository.getCurrentUser()

        // 再次调用，不应该重复创建
        repository.initializeDefaultUserIfNeeded()
        val secondUser = repository.getCurrentUser()

        // Then
        assertNotNull(firstUser)
        assertEquals("user-1", firstUser.id)
        assertEquals("default", firstUser.username)
        assertEquals("Default User", firstUser.displayName)

        assertNotNull(secondUser)
        assertEquals("user-1", secondUser.id) // 应该是同一个用户
    }

    @Test
    fun `test initialize default user when user exists`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val repository = UserRepositoryImpl(localDataSource)
        val existingUser = createTestUser("existing-1", "existing")
        repository.updateUser(existingUser)

        // When
        repository.initializeDefaultUserIfNeeded()
        val result = repository.getCurrentUser()

        // Then - 应该保持现有用户，不创建默认用户
        assertNotNull(result)
        assertEquals("existing-1", result.id)
        assertEquals("existing", result.username)
    }

    @Test
    fun `test user with avatar`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val repository = UserRepositoryImpl(localDataSource)
        val user = createTestUser(
            "1",
            "testuser",
            avatarUrl = "https://example.com/avatar.png"
        )

        // When
        repository.updateUser(user)
        val result = repository.getCurrentUser()

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

