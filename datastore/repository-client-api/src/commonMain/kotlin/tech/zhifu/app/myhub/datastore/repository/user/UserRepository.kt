package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface UserRepository {

    // ==================== User 操作 ====================

    suspend fun insertUser(user: User, needSync: Boolean = true)

    suspend fun hasUser(): Boolean

    suspend fun getUser(): User

    suspend fun getUser(userId: String): UserStoreData?

    fun streamUser(userId: String, refresh: Boolean = false): Flow<StoreReadResponse<UserStoreData>>

    // ==================== UserPreferences 操作 ====================

    suspend fun insertUserPreferences(preferences: UserPreferences, needSync: Boolean = true)

    suspend fun getUserPreferences(): UserPreferences

    suspend fun getUserPreferences(userId: String): UserStoreData?

    fun streamUserPreferences(userId: String, refresh: Boolean = false): Flow<StoreReadResponse<UserStoreData>>

    suspend fun updateUserPreferencesTheme(userId: String, theme: String)

    suspend fun updateUserPreferencesLanguage(userId: String, language: String)
}
