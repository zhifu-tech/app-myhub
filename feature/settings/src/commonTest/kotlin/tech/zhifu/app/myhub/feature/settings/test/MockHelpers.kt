package tech.zhifu.app.myhub.feature.settings.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserStoreData
import tech.zhifu.app.myhub.settings.LocalSettingStore

/**
 * Mock LocalSettingStore 用于测试
 */
class MockLocalSettingStore : LocalSettingStore {
    private val storage = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? {
        return storage[key]
    }

    override fun getSync(key: String): String? {
        return storage[key]
    }

    override suspend fun set(key: String, value: String) {
        storage[key] = value
    }

    override fun setSync(key: String, value: String) {
        storage[key] = value
    }

    override suspend fun remove(key: String) {
        storage.remove(key)
    }

    override fun removeSync(key: String) {
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
    private var user: User? = null,
    private var preferences: UserPreferences? = null
) : UserRepository {

    override suspend fun insertUser(user: User, needSync: Boolean) {
        this.user = user
    }

    override suspend fun getUser(): User {
        return user ?: throw IllegalStateException("User not found")
    }

    override suspend fun getUserOrNull(): User? = user

    override suspend fun getUser(userId: String): UserStoreData? {
        val current = user ?: return null
        return if (current.id == userId) UserStoreData.UserData(current) else null
    }

    override fun streamUser(): Flow<User?> = emptyFlow()

    override fun streamUser(
        userId: String,
        refresh: Boolean
    ): Flow<StoreReadResponse<UserStoreData>> = emptyFlow()

    override suspend fun insertUserPreferences(preferences: UserPreferences, needSync: Boolean) {
        this.preferences = preferences
    }

    override suspend fun getUserPreferences(): UserPreferences {
        return preferences ?: throw IllegalStateException("UserPreferences not found")
    }

    override suspend fun getUserPreferences(userId: String): UserStoreData? {
        val current = preferences ?: return null
        return if (current.userId == userId) UserStoreData.PreferencesData(current) else null
    }

    override fun streamUserPreferences(
        userId: String,
        refresh: Boolean
    ): Flow<StoreReadResponse<UserStoreData>> = emptyFlow()

    override suspend fun updateUserPreferencesTheme(userId: String, theme: String) {
        val current = preferences ?: return
        if (current.userId != userId) return
        preferences = current.copy(theme = theme)
    }

    override suspend fun updateUserPreferencesLanguage(userId: String, language: String) {
        val current = preferences ?: return
        if (current.userId != userId) return
        preferences = current.copy(language = language)
    }

    override suspend fun updateUserPreferencesLayoutAsList(userId: String, layoutAsList: Boolean) {
        val current = preferences ?: return
        if (current.userId != userId) return
        preferences = current.copy(layoutAsList = layoutAsList)
    }

    override suspend fun updateUserPreferencesSortAsDate(userId: String, sortAsDate: Boolean) {
        val current = preferences ?: return
        if (current.userId != userId) return
        preferences = current.copy(sortAsDate = sortAsDate)
    }
}
