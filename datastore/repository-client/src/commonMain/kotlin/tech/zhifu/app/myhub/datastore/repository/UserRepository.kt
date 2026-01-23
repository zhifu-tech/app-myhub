package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface UserRepository {
    val syncUserChangeApplier: SyncChangeApplier
    val syncUserPreferencesChangeApplier: SyncChangeApplier

    suspend fun insertUser(user: User, needSync: Boolean = true)

    suspend fun hasUser(): Boolean

    suspend fun getUser(): User

    fun observeUser(): Flow<User>

    suspend fun getUserById(userId: String): User?

    suspend fun insertUserPreferences(preferences: UserPreferences, needSync: Boolean = true)

    suspend fun getUserPreferences(userId: String): UserPreferences?

    fun observeUserPreferences(userId: String): Flow<UserPreferences>
}
