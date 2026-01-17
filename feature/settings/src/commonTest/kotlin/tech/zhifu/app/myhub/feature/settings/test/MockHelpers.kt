package tech.zhifu.app.myhub.feature.settings.test

import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.feature.settings.data.store.LocalSettingStore
import kotlin.time.Clock

/**
 * Mock LocalSettingStore 用于测试
 */
class MockLocalSettingStore : LocalSettingStore {
    private val storage = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? {
        return storage[key]
    }

    override suspend fun set(key: String, value: String) {
        storage[key] = value
    }

    override suspend fun remove(key: String) {
        storage.remove(key)
    }

    override suspend fun clear() {
        storage.clear()
    }
}

/**
 * Mock UserRepository 用于测试
 */
class MockUserRepository(
    private val user: User?
) : UserRepository {
    override suspend fun getCurrentUser(): User? = user
    override suspend fun updateUser(user: User): User {
        // Mock implementation - return the updated user
        return user
    }
}

/**
 * 创建测试用户
 */
fun createTestUser(
    id: String = "1",
    username: String = "test",
    preferences: tech.zhifu.app.myhub.datastore.model.UserPreferences? = null
): User {
    return User(
        id = id,
        username = username,
        preferences = preferences,
        createdAt = Clock.System.now()
    )
}

