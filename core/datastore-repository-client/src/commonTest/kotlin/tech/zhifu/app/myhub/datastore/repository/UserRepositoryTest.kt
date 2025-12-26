package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteUserDataSourceStub
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.impl.UserRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * UserRepository 测试
 */
class UserRepositoryTest {

    @Test
    fun `test update user`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val remoteDataSource = RemoteUserDataSourceStub()
        val repository = UserRepositoryImpl(localDataSource, remoteDataSource)
        val user = createTestUser("1", "testuser")

        // When
        val result = repository.updateUser(user)

        // Then
        assertNotNull(result)
        val flow = repository.observeCurrentUser()
        val retrievedUser = flow.first()
        assertNotNull(retrievedUser)
        assertEquals("1", retrievedUser.id)
        assertEquals("testuser", retrievedUser.username)
    }

    @Test
    fun `test update preferences`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val remoteDataSource = RemoteUserDataSourceStub()
        val repository = UserRepositoryImpl(localDataSource, remoteDataSource)
        val user = createTestUser("1", "testuser")
        repository.updateUser(user)

        // When
        val preferences = UserPreferences(
            theme = "light",
            language = "zh",
            defaultCardType = CardType.QUOTE,
            autoSync = false,
            syncInterval = 7200000L
        )
        val result = repository.updatePreferences(preferences)

        // Then
        assertNotNull(result)
        val retrievedPreferences = repository.getPreferences()
        assertNotNull(retrievedPreferences)
        assertEquals("light", retrievedPreferences.theme)
        assertEquals("zh", retrievedPreferences.language)
        assertEquals(CardType.QUOTE, retrievedPreferences.defaultCardType)
    }

    @Test
    fun `test get preferences when no user`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val remoteDataSource = RemoteUserDataSourceStub()
        val repository = UserRepositoryImpl(localDataSource, remoteDataSource)

        // When
        val result = repository.getPreferences()

        // Then
        assertNull(result)
    }

    @Test
    fun `test update preferences when no user fails`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val remoteDataSource = RemoteUserDataSourceStub()
        val repository = UserRepositoryImpl(localDataSource, remoteDataSource)
        val preferences = UserPreferences()

        // When & Then - 应该抛出异常
        try {
            repository.updatePreferences(preferences)
            // 如果没有抛出异常，测试失败
            assertTrue(false, "Expected exception when updating preferences without user")
        } catch (e: IllegalStateException) {
            // 期望的行为：抛出异常
            assertTrue(true)
        }
    }

    @Test
    fun `test logout`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val remoteDataSource = RemoteUserDataSourceStub()
        val repository = UserRepositoryImpl(localDataSource, remoteDataSource)
        val user = createTestUser("1", "testuser")
        repository.updateUser(user)

        // When
        repository.logout()
        val flow = repository.observeCurrentUser()
        val retrievedUser = flow.first()

        // Then
        assertNull(retrievedUser)
    }

    @Test
    fun `test get current user flow`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalUserDataSourceImpl(database)
        val remoteDataSource = RemoteUserDataSourceStub()
        val repository = UserRepositoryImpl(localDataSource, remoteDataSource)
        val user1 = createTestUser("1", "user1")

        // When
        val flow = repository.observeCurrentUser()
        repository.updateUser(user1)
        val firstResult = flow.first()

        val user2 = createTestUser("1", "user2")
        repository.updateUser(user2)
        val secondResult = flow.first()

        // Then
        assertNotNull(firstResult)
        assertEquals("user1", firstResult.username)
        assertNotNull(secondResult)
        assertEquals("user2", secondResult.username)
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

